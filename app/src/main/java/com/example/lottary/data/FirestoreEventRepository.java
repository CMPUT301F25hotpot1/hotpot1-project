package com.example.lottary.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.text.DateFormat;
import java.util.*;

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
    public interface DocListener { void onChanged(DocumentSnapshot doc); }


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

    public Task<Void> drawWinners(@NonNull String eventId, int maxToDraw) {
        DocumentReference ref = events.document(eventId);
        return db.runTransaction(tr -> {
            DocumentSnapshot d = tr.get(ref);
            if (!d.exists()) return null;

            List<String> waiting = (List<String>) d.get("waitingList"); if (waiting == null) waiting = new ArrayList<>();
            List<String> chosen  = (List<String>) d.get("chosen");      if (chosen == null)  chosen  = new ArrayList<>();
            List<String> signed  = (List<String>) d.get("signedUp");    if (signed == null)  signed  = new ArrayList<>();
            List<String> cancel  = (List<String>) d.get("cancelled");   if (cancel == null)  cancel  = new ArrayList<>();
            Number capNum = (Number) d.get("capacity");
            int capacity = capNum == null ? 0 : capNum.intValue();

            int remaining = LotterySampler.capacityRemaining(capacity, signed.size());
            int toDraw = maxToDraw <= 0 ? remaining : Math.min(remaining, maxToDraw);

            Set<String> taken = new HashSet<>();
            taken.addAll(chosen); taken.addAll(signed); taken.addAll(cancel);

            List<String> winners = LotterySampler.sampleWinners(waiting, taken, toDraw, System.currentTimeMillis());
            if (!winners.isEmpty()) {
                List<String> newChosen = new ArrayList<>(chosen);
                newChosen.addAll(winners);
                tr.update(ref, "chosen", newChosen);
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
        String id = d.getId();
        String title = safe(d.getString("title"));
        String city = safe(d.getString("city"));
        String venue = safe(d.getString("venue"));
        boolean full = Boolean.TRUE.equals(d.getBoolean("full"));

        Timestamp ts = d.getTimestamp("startTime");
        String pretty = ts == null ? "" :
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(ts.toDate());
        return new Event(id, title, city, venue, pretty, full);
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static void ensureArrays(Map<String, Object> map, String... keys) {
        for (String k : keys) if (!(map.get(k) instanceof List)) map.put(k, new ArrayList<String>());
    }
}
