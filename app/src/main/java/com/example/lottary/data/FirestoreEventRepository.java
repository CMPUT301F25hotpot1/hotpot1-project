
package com.example.lottary.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FirestoreEventRepository {

    private static FirestoreEventRepository INSTANCE;

    public static FirestoreEventRepository get() {
        if (INSTANCE == null) INSTANCE = new FirestoreEventRepository();
        return INSTANCE;
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference events = db.collection("events");

    // ---------- listeners ----------
    public interface EventsListener { void onChanged(@NonNull List<Event> items); }
    public interface DocListener    { void onChanged(DocumentSnapshot doc); }

    public ListenerRegistration listenCreatedByDevice(
            @NonNull String deviceId, @NonNull EventsListener l) {
        return events.whereEqualTo("creatorDeviceId", deviceId)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) { l.onChanged(Collections.emptyList()); return; }
                    l.onChanged(mapList(snap));
                });
    }

    public ListenerRegistration listenRecentCreated(@NonNull EventsListener l) {
        return events.orderBy("createdAt").limit(50)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) { l.onChanged(Collections.emptyList()); return; }
                    l.onChanged(mapList(snap));
                });
    }

    public ListenerRegistration listenJoined(@NonNull String deviceId, @NonNull EventsListener l) {
        return events.whereArrayContains("waitingList", deviceId)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) { l.onChanged(Collections.emptyList()); return; }
                    l.onChanged(mapList(snap));
                });
    }

    public ListenerRegistration listenEvent(@NonNull String eventId, @NonNull DocListener l) {
        return events.document(eventId)
                .addSnapshotListener((snap, err) -> { if (snap != null) l.onChanged(snap); });
    }

    // ---------- writes ----------
    public Task<DocumentReference> createEvent(Map<String, Object> fields) {
        ensureArrays(fields, "waitingList", "chosen", "signedUp", "cancelled");
        if (!fields.containsKey("createdAt")) fields.put("createdAt", Timestamp.now());
        return events.add(fields);
    }

    public Task<Void> updateEvent(@NonNull String eventId, Map<String, Object> fields) {
        return events.document(eventId).set(fields, SetOptions.merge());
    }

    /**
     * 仅抽签（原实现，保留兼容）
     */
    public Task<Void> drawWinners(@NonNull String eventId, int maxToDraw) {
        DocumentReference ref = events.document(eventId);
        return db.runTransaction(tr -> {
            DocumentSnapshot d = tr.get(ref);
            if (!d.exists()) return null;

            List<String> waiting = strList(d.get("waitingList"));
            List<String> chosen  = strList(d.get("chosen"));
            List<String> signed  = strList(d.get("signedUp"));
            List<String> cancel  = strList(d.get("cancelled"));

            Number capNum = (Number) d.get("capacity");
            int capacity = capNum == null ? 0 : capNum.intValue();

            int remaining = LotterySampler.capacityRemaining(capacity, signed.size());
            int toDraw = maxToDraw <= 0 ? remaining : Math.min(remaining, maxToDraw);

            Set<String> taken = new HashSet<>();
            taken.addAll(chosen); taken.addAll(signed); taken.addAll(cancel);

            List<String> winners = LotterySampler.sampleWinners(
                    waiting, taken, toDraw, System.currentTimeMillis());

            if (!winners.isEmpty()) {
                List<String> newChosen = new ArrayList<>(chosen);
                newChosen.addAll(winners);
                tr.update(ref, "chosen", newChosen);
            }
            return null;
        });
    }

    /**
     * 方案C：抽签 + 自动发送“已选中”通知到 notifications 集合（type="selected"）
     * - message 可自定义；为 null/空时会给一个默认消息
     * - organizerId 和 eventTitle 会从 event 文档里自动读取（字段：creatorDeviceId / title）
     */
    public Task<Void> drawWinnersAndNotify(@NonNull String eventId, String message) {
        DocumentReference ref = events.document(eventId);

        // 先事务：抽签并更新 chosen，返回需要发通知的 winners + 元信息
        return db.runTransaction(tr -> {
            DocumentSnapshot d = tr.get(ref);
            if (!d.exists()) return new DrawResult(); // 空

            List<String> waiting = strList(d.get("waitingList"));
            List<String> chosen  = strList(d.get("chosen"));
            List<String> signed  = strList(d.get("signedUp"));
            List<String> cancel  = strList(d.get("cancelled"));

            Number capNum = (Number) d.get("capacity");
            int capacity = capNum == null ? 0 : capNum.intValue();

            int remaining = LotterySampler.capacityRemaining(capacity, signed.size());
            int toDraw = Math.max(0, remaining);

            Set<String> taken = new HashSet<>();
            taken.addAll(chosen); taken.addAll(signed); taken.addAll(cancel);

            List<String> winners = LotterySampler.sampleWinners(
                    waiting, taken, toDraw, System.currentTimeMillis());

            if (!winners.isEmpty()) {
                List<String> newChosen = new ArrayList<>(chosen);
                newChosen.addAll(winners);
                tr.update(ref, "chosen", newChosen);
            }

            DrawResult res = new DrawResult();
            res.winners = winners;
            res.organizerId = str(d.get("creatorDeviceId"));
            res.eventTitle  = str(d.get("title"));
            res.eventId     = eventId;
            return res;
        }).continueWithTask(t -> {
            if (!t.isSuccessful()) {
                Exception e = t.getException();
                return Tasks.forException(e == null ? new RuntimeException("Transaction failed") : e);
            }

            DrawResult r = t.getResult();
            if (r == null || r.winners == null || r.winners.isEmpty()) {
                return Tasks.forResult(null);
            }

            String finalMsg = (message == null || message.trim().isEmpty())
                    ? "Congratulations! You are selected. Please sign up to secure your spot."
                    : message.trim();

            WriteBatch batch = db.batch();
            CollectionReference notifs = db.collection("notifications");

            for (String rid : r.winners) {
                Map<String, Object> doc = new HashMap<>();
                doc.put("recipientId", rid);
                doc.put("eventId", r.eventId);
                doc.put("eventTitle", r.eventTitle);
                doc.put("organizerId", r.organizerId);
                doc.put("type", "selected");
                doc.put("message", finalMsg);
                doc.put("sentAt", Timestamp.now());
                batch.set(notifs.document(), doc);
            }
            return batch.commit();
        });
    }

    // ---------- helpers ----------
    @SuppressWarnings("unchecked")
    private static List<String> strList(Object o) {
        if (o instanceof List<?>) {
            List<String> out = new ArrayList<>();
            for (Object e : (List<?>) o) if (e != null) out.add(e.toString());
            return out;
        }
        return new ArrayList<>();
    }

    private static String str(Object o){ return o == null ? "" : o.toString(); }

    // user accepts invitation (moves into signedUp, removes from others)
    public Task<Void> signUp(@NonNull String eventId, @NonNull String deviceId) {
        DocumentReference ref = events.document(eventId);
        return db.runTransaction(tr -> {
            DocumentSnapshot d = tr.get(ref);
            if (!d.exists()) return null;

            List<String> waiting = strList(d.get("waitingList"));
            List<String> chosen  = strList(d.get("chosen"));
            List<String> signed  = strList(d.get("signedUp"));
            List<String> cancel  = strList(d.get("cancelled"));

            boolean changed = false;
            if (!signed.contains(deviceId)) { signed.add(deviceId); changed = true; }
            if (chosen.remove(deviceId))     changed = true;
            if (waiting.remove(deviceId))    changed = true;
            if (cancel.remove(deviceId))     changed = true;

            Map<String, Object> updates = new HashMap<>();
            updates.put("signedUp", signed);
            updates.put("chosen", chosen);
            updates.put("waitingList", waiting);
            updates.put("cancelled", cancel);

            Number capN = (Number) d.get("capacity");
            int cap = capN == null ? 0 : capN.intValue();
            boolean full = cap > 0 && signed.size() >= cap;
            updates.put("full", full);

            if (changed) tr.update(ref, updates);
            return null;
        });
    }

    // user declines invitation (moves into cancelled)
    public Task<Void> decline(@NonNull String eventId, @NonNull String deviceId) {
        DocumentReference ref = events.document(eventId);
        return db.runTransaction(tr -> {
            DocumentSnapshot d = tr.get(ref);
            if (!d.exists()) return null;

            List<String> chosen = strList(d.get("chosen"));
            List<String> signed = strList(d.get("signedUp"));
            List<String> cancel = strList(d.get("cancelled"));

            boolean changed = false;
            if (chosen.remove(deviceId)) changed = true;
            if (signed.remove(deviceId)) changed = true;
            if (!cancel.contains(deviceId)) { cancel.add(deviceId); changed = true; }

            Map<String, Object> updates = new HashMap<>();
            updates.put("chosen", chosen);
            updates.put("signedUp", signed);
            updates.put("cancelled", cancel);

            Number capN = (Number) d.get("capacity");
            int cap = capN == null ? 0 : capN.intValue();
            boolean full = cap > 0 && signed.size() >= cap;
            updates.put("full", full);

            if (changed) tr.update(ref, updates);
            return null;
        });
    }

    // waiting list ops
    public Task<Void> joinWaitingList(@NonNull String eventId, @NonNull String deviceId) {
        DocumentReference ref = events.document(eventId);
        return db.runTransaction(tr -> {
            DocumentSnapshot d = tr.get(ref);
            if (!d.exists()) return null;

            List<String> waiting = strList(d.get("waitingList"));
            List<String> chosen  = strList(d.get("chosen"));
            List<String> signed  = strList(d.get("signedUp"));
            List<String> cancel  = strList(d.get("cancelled"));

            boolean changed = false;
            if (!waiting.contains(deviceId)) { waiting.add(deviceId); changed = true; }
            if (chosen.remove(deviceId)) changed = true;
            if (signed.remove(deviceId)) changed = true;
            if (cancel.remove(deviceId)) changed = true;

            if (changed) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("waitingList", waiting);
                updates.put("chosen", chosen);
                updates.put("signedUp", signed);
                updates.put("cancelled", cancel);
                tr.update(ref, updates);
            }
            return null;
        });
    }

    public Task<Void> leaveWaitingList(@NonNull String eventId, @NonNull String deviceId) {
        DocumentReference ref = events.document(eventId);
        return db.runTransaction(tr -> {
            DocumentSnapshot d = tr.get(ref);
            if (!d.exists()) return null;

            List<String> waiting = strList(d.get("waitingList"));
            if (waiting.remove(deviceId)) {
                tr.update(ref, "waitingList", waiting);
            }
            return null;
        });
    }

    // ---------- CSV helper ----------
    public static String buildCsvFromEvent(@NonNull DocumentSnapshot d) {
        StringBuilder sb = new StringBuilder();
        sb.append("status,entrantId\n");
        appendRows(sb, "chosen", d.get("chosen"));
        appendRows(sb, "signedUp", d.get("signedUp"));
        appendRows(sb, "cancelled", d.get("cancelled"));
        return sb.toString();
    }

    private static void appendRows(StringBuilder sb, String status, Object arr) {
        if (!(arr instanceof List)) return;
        for (Object o : (List<?>) arr) sb.append(status).append(",").append(o).append("\n");
    }

    // ---------- mapping ----------
    private List<Event> mapList(QuerySnapshot snap) {
        if (snap == null || snap.isEmpty()) return Collections.emptyList();
        List<Event> list = new ArrayList<>();
        for (DocumentSnapshot d : snap.getDocuments()) list.add(map(d));
        return list;
    }

    private Event map(DocumentSnapshot d) {
        String id    = d.getId();
        String title = safe(d.getString("title"));
        String city  = safe(d.getString("city"));
        String venue = safe(d.getString("venue"));
        boolean full = Boolean.TRUE.equals(d.getBoolean("full"));

        Timestamp tsStart = d.getTimestamp("startTime");
        long startMs = tsStart == null ? 0L : tsStart.toDate().getTime();
        String pretty = tsStart == null ? "" :
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(tsStart.toDate());

        Timestamp tsRegStart = d.getTimestamp("registerStart");
        Timestamp tsRegEnd   = d.getTimestamp("registerEnd");
        long regStartMs = tsRegStart == null ? 0L : tsRegStart.toDate().getTime();
        long regEndMs   = tsRegEnd   == null ? 0L : tsRegEnd.toDate().getTime();

        boolean geo = Boolean.TRUE.equals(d.getBoolean("geolocationEnabled"));
        String type = safe(d.getString("type"));

        return new Event(
                id, title, city, venue, pretty, full,
                startMs, regStartMs, regEndMs, geo, type
        );
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static void ensureArrays(Map<String, Object> map, String... keys) {
        for (String k : keys) if (!(map.get(k) instanceof List)) map.put(k, new ArrayList<String>());
    }

    // transaction result carrier for drawWinnersAndNotify
    private static class DrawResult {
        List<String> winners = new ArrayList<>();
        String organizerId = "";
        String eventTitle  = "";
        String eventId     = "";
    }
}
