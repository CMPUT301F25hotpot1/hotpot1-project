package com.example.lottary.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminRepository 负责：
 * - 从 Firestore 加载 events（通过 FirestoreEventRepository）
 * - 从 Firestore 加载 notifications 集合
 * - 删除 event
 * - 提供 LiveData 给 Admin UI 页面
 */
public class AdminRepository {

    private static AdminRepository INSTANCE;

    public static AdminRepository get() {
        if (INSTANCE == null) INSTANCE = new AdminRepository();
        return INSTANCE;
    }

    // ✅ Firestore Event Repository（保持你的不变）
    private final FirestoreEventRepository firestore = FirestoreEventRepository.get();

    // ================================
    // ✅ Events LiveData
    // ================================
    private final MutableLiveData<List<Event>> eventsLive = new MutableLiveData<>(new ArrayList<>());
    private ListenerRegistration eventsListener;

    public LiveData<List<Event>> events() {
        return eventsLive;
    }

    /**
     * ✅ 从 Firestore 真实加载 events（用于 AdminEventsActivity）
     */
    public void loadEventsFromFirestore() {

        // 如果已有监听先移除
        if (eventsListener != null) eventsListener.remove();

        // ✅ 监听 Firestore events 集合（按 createdAt 排序）
        eventsListener = FirebaseFirestore.getInstance()
                .collection("events")
                .orderBy("createdAt")
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) {
                        eventsLive.postValue(new ArrayList<>());
                        return;
                    }

                    List<Event> list = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        list.add(firestoreEventMap(d));
                    }

                    eventsLive.postValue(list);
                });
    }

    /**
     * ✅ 删除 event（Admin 点击删除事件按钮）
     */
    public void removeEvent(Event event) {
        if (event == null) return;

        // Firestore 删除
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(event.getId())
                .delete();

        // ✅ 本地 LiveData 同步删除（UI 会立即刷新）
        List<Event> list = new ArrayList<>(eventsLive.getValue());
        list.remove(event);
        eventsLive.postValue(list);
    }

    /**
     * ✅ 把 DocumentSnapshot 映射成 Event（保持和 FirestoreEventRepository 的 map 完全一致）
     */
    private Event firestoreEventMap(DocumentSnapshot d) {

        String id    = d.getId();
        String title = safe(d.getString("title"));
        String city  = safe(d.getString("city"));
        String venue = safe(d.getString("venue"));
        boolean full = Boolean.TRUE.equals(d.getBoolean("full"));

        com.google.firebase.Timestamp tsStart = d.getTimestamp("startTime");
        long startMs = tsStart == null ? 0L : tsStart.toDate().getTime();

        com.google.firebase.Timestamp tsRegStart = d.getTimestamp("registerStart");
        com.google.firebase.Timestamp tsRegEnd   = d.getTimestamp("registerEnd");

        long regStartMs = tsRegStart == null ? 0L : tsRegStart.toDate().getTime();
        long regEndMs   = tsRegEnd   == null ? 0L : tsRegEnd.toDate().getTime();

        boolean geo = Boolean.TRUE.equals(d.getBoolean("geolocationEnabled"));
        String type = safe(d.getString("type"));

        return new Event(
                id, title, city, venue, "", full,
                startMs, regStartMs, regEndMs, geo, type
        );
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }


    // ==========================================
    // ✅ Notifications (AdminNotificationsActivity)
    // ==========================================
    private final MutableLiveData<List<String>> noticesLive = new MutableLiveData<>(new ArrayList<>());
    private ListenerRegistration noticesListener;

    public LiveData<List<String>> notices() {
        return noticesLive;
    }

    /**
     * ✅ 加载 notifications 表
     */
    public void loadNotices() {

        // 先移除旧监听
        if (noticesListener != null) noticesListener.remove();

        // ✅ 监听 Firestore notifications 集合
        noticesListener = FirebaseFirestore.getInstance()
                .collection("notifications")
                .orderBy("sentAt")
                .addSnapshotListener((snap, err) -> {

                    if (err != null || snap == null) {
                        noticesLive.postValue(new ArrayList<>());
                        return;
                    }

                    List<String> list = new ArrayList<>();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String msg = d.getString("message");
                        if (msg != null) list.add(msg);
                    }

                    noticesLive.postValue(list);
                });
    }
}
