package com.example.lottary.data;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * FirestoreNotificationRepository
 *
 * Purpose:
 *  Reads notification log entries from the "notifications" collection.
 *
 * Expected document fields (new schema written by FirestoreEventRepository):
 *  - recipientId : device/user id
 *  - eventId     : related event id
 *  - eventTitle  : event title to display
 *  - organizerId : who sent it
 *  - type        : "selected" / "not_selected" / ...
 *  - message     : body text
 *  - sentAt      : Timestamp
 *
 * Legacy compatibility:
 *  - title/timestamp fields are still read as fallback for older docs.
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
                .whereEqualTo("recipientId", uid)
                .orderBy("sentAt")
                .get()
                .addOnSuccessListener(snap -> callback.onChanged(mapList(snap)));
    }

    private List<NotificationLog> mapList(QuerySnapshot snap) {
        List<NotificationLog> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot d : snap.getDocuments()) list.add(map(d));
        return list;
    }

    private NotificationLog map(DocumentSnapshot d) {
        String id = d.getId();

        String title = d.getString("eventTitle");
        if (title == null || title.isEmpty()) {
            title = d.getString("title"); // fallback for older docs
        }

        String recipientName = d.getString("recipientName");
        String message = d.getString("message");

        Timestamp ts = d.getTimestamp("sentAt");
        if (ts == null) {
            ts = d.getTimestamp("timestamp"); // fallback for older docs
        }

        return new NotificationLog(
                id,
                title,
                recipientName,
                message,
                ts
        );
    }
}

