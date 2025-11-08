package com.example.lottary.data;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
/**
 * FirestoreNotificationRepository
 *
 * Purpose:
 * Handles Firestore access for retrieving user-specific notification logs.
 * Provides simple one-time queries for all notifications received by a given user.
 *
 * Role / Pattern:
 * Implements a singleton repository focused on Firestore “notifications” collection.
 * Responsible for mapping Firestore documents into NotificationLog model objects
 * and delivering the result via callback to the UI layer.
 *
 * Outstanding Issues / Notes:
 * - Query uses field name “recipientID”; ensure it matches actual Firestore schema
 *   (case-sensitive mismatch will return no results).
 * - Fetch is one-time only (no real-time listener implemented).
 * - No pagination or error handling for large result sets.
 * - Timestamps are assumed to be stored as Firestore Timestamp objects.
 */

public class FirestoreNotificationRepository {

    private static FirestoreNotificationRepository INSTANCE;

    public static FirestoreNotificationRepository get() {
        if (INSTANCE == null) INSTANCE = new FirestoreNotificationRepository();
        return INSTANCE;
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface LogsListener {
        void onChanged(List<NotificationLog> list);
    }

    public void getLogsForUser(String uid, @NonNull LogsListener callback) {
        db.collection("notifications")
                .whereEqualTo("recipientID", uid)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(snap -> callback.onChanged(mapList(snap)));
    }

    private List<NotificationLog> mapList(QuerySnapshot snap) {
        List<NotificationLog> list = new ArrayList<>();
        for (DocumentSnapshot d : snap.getDocuments()) list.add(map(d));
        return list;
    }

    private NotificationLog map(DocumentSnapshot d) {
        return new NotificationLog(
                d.getId(),
                d.getString("title"),
                d.getString("recipientName"),
                d.getString("message"),
                d.getTimestamp("timestamp")
        );
    }
}
