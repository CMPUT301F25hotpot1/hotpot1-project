package com.example.lottary.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * FirestoreUserRepository
 *
 * Purpose:
 * Repository for managing user-related data stored in Firestore.
 * Supports creating, updating, deleting, retrieving, and listening
 * to user documents in real time.
 *
 * Role / Pattern:
 * Implements the Repository pattern to abstract Firestore operations
 * for the “users” collection. Provides unified data access and mapping
 * between Firestore snapshots and User model objects.
 *
 * Outstanding Issues / Notes:
 * - User lookup and search rely on simple name field range queries;
 *   lacks indexing or case-insensitive handling.
 * - No error callback provided for failed Firestore operations.
 * - Uses client-side mapping; schema changes in Firestore may break parsing.
 * - Device ID is used as the document key — assumes one user per device.
 */

public class FirestoreUserRepository {

    private static FirestoreUserRepository INSTANCE;

    public static FirestoreUserRepository get() {
        if (INSTANCE == null) INSTANCE = new FirestoreUserRepository();
        return INSTANCE;
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference users = db.collection("users");

    public Task<Void> createUser(String deviceID, Map<String, Object> fields) {
        if (!fields.containsKey("createdAt"))
            fields.put("createdAt", Timestamp.now());
        return users.document(deviceID).set(fields);
    }

    public Task<Void> updateUser(@NonNull String deviceID, Map<String, Object> fields) {
        return users.document(deviceID).set(fields, SetOptions.merge());
    }

    public Task<Void> deleteUser(@NonNull String deviceID) {
        return users.document(deviceID).delete();
    }

    public DocumentReference hasUser(@NonNull String deviceID) {
        return users.document(deviceID);
    }

    public interface UsersListener { void onChanged(@NonNull List<User> items); }
    public interface DocListener    { void onChanged(DocumentSnapshot doc); }

    public ListenerRegistration listenRecentCreated(@NonNull UsersListener l) {
        return users.orderBy("createdAt").limit(50)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) {
                        l.onChanged(Collections.emptyList());
                        return;
                    }
                    l.onChanged(mapList(snap));
                });
    }

    public ListenerRegistration listenUser(@NonNull String deviceID, @NonNull DocListener l) {
        return users.document(deviceID)
                .addSnapshotListener((snap, err) -> {
                    if (snap != null)
                        l.onChanged(snap);
                    else
                        Log.i("EmptyDocument", "Failed with: ", err);
                });
    }

    private List<User> mapList(QuerySnapshot snap) {
        if (snap == null || snap.isEmpty()) return Collections.emptyList();
        List<User> list = new ArrayList<>();
        for (DocumentSnapshot d : snap.getDocuments()) list.add(map(d));
        return list;
    }

    private User map(DocumentSnapshot d) {
        String deviceID = safe(d.getString("userDeviceId"));
        String name     = safe(d.getString("name"));
        String email    = safe(d.getString("email"));
        String phone    = safe(d.getString("phoneNumber"));

        return new User(
                name,
                email,
                phone,
                deviceID
        );
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public void getAllUsers(@NonNull UsersListener callback) {
        users.get().addOnSuccessListener(snap -> {
            if (snap == null) {
                callback.onChanged(Collections.emptyList());
                return;
            }
            callback.onChanged(mapList(snap));
        });
    }

    public void searchUsers(@NonNull String keyword, @NonNull UsersListener callback) {
        users.whereGreaterThanOrEqualTo("name", keyword)
                .whereLessThanOrEqualTo("name", keyword + "\uf8ff")
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap == null) {
                        callback.onChanged(Collections.emptyList());
                        return;
                    }
                    callback.onChanged(mapList(snap));
                });
    }

    public void removeUser(@NonNull String userID) {
        deleteUser(userID);
    }
}
