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
 *  - recipientId : device/user id   (== ANDROID_ID used in NotificationsActivity)
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

    /**
     * Loads all notifications for the given recipient id.
     *
     * IMPORTANT:
     * 这里的查询逻辑和 NotificationsActivity.startListening() 保持一致，
     * 只用 whereEqualTo("recipientId", uid) + limit(200)，不再使用 orderBy("sentAt")，
     * 这样就不需要 Firestore 的复合索引，也能拿到和 User 端相同的结果。
     */
    public void getLogsForUser(String uid, @NonNull LogsListener callback) {
        db.collection("notifications")
                .whereEqualTo("recipientId", uid)
                .limit(200)
                .get()
                .addOnSuccessListener(snap -> callback.onChanged(mapList(snap)));
    }

    private List<NotificationLog> mapList(QuerySnapshot snap) {
        List<NotificationLog> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot d : snap.getDocuments()) {
            list.add(map(d));
        }
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
