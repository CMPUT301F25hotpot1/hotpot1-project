package com.example.lottary.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin-only Firestore operations.
 * DOES NOT modify FirestoreEventRepository — safe for user side.
 */
public class FirestoreAdminRepository {

    private static FirestoreAdminRepository INSTANCE;

    public static FirestoreAdminRepository get() {
        if (INSTANCE == null) INSTANCE = new FirestoreAdminRepository();
        return INSTANCE;
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ✅ admin notices log collection
    private final CollectionReference notices = db.collection("admin_notices");

    // ✅ DELETE event document (Admin ONLY)
    public Task<Void> deleteEventById(@NonNull String eventId) {
        return db.collection("events")
                .document(eventId)
                .delete();
    }

    // ✅ Push admin log
    public Task<Void> pushNotice(@NonNull String msg) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("message", msg);
        doc.put("timestamp", Timestamp.now());
        return notices.document().set(doc);
    }
}
