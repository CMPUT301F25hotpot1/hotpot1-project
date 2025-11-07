package com.example.lottary.data;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreAdminRepository {

    private static FirestoreAdminRepository INSTANCE;

    public static FirestoreAdminRepository get() {
        if (INSTANCE == null) INSTANCE = new FirestoreAdminRepository();
        return INSTANCE;
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface EventsCallback {
        void onLoaded(List<Event> events);
    }

    // ✅ 从 Firestore 获取所有 events
    public void loadAllEvents(@NonNull EventsCallback callback) {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onLoaded(new ArrayList<>());
                        return;
                    }

                    QuerySnapshot snap = task.getResult();
                    List<Event> list = new ArrayList<>();

                    if (snap != null && !snap.isEmpty()) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            list.add(new Event(
                                    d.getId(),
                                    d.getString("title"),
                                    d.getString("city"),
                                    d.getString("venue"),
                                    "", // formatted time (not needed)
                                    Boolean.TRUE.equals(d.getBoolean("full")),
                                    0, 0, 0,
                                    Boolean.TRUE.equals(d.getBoolean("geolocationEnabled")),
                                    d.getString("type")
                            ));
                        }
                    }

                    callback.onLoaded(list);
                });
    }

    // ✅ 删除 Firestore 的 event
    public void deleteEventById(String id) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(id)
                .delete();
    }

    public void deleteEvent(String eventId) {
        db.collection("events").document(eventId).delete();
    }
}
