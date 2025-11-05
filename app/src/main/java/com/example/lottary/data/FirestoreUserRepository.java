package com.example.lottary.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
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

public class FirestoreUserRepository {

    private static FirestoreUserRepository INSTANCE;

    public static FirestoreUserRepository get() {
        if (INSTANCE == null) INSTANCE = new FirestoreUserRepository();
        return INSTANCE;
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference users = db.collection("users");

    // ---------- writes ----------
    public Task<Void> createUser(String deviceID, Map<String, Object> fields) {
        ensureArrays(fields, "notifyPrefs");
        if (!fields.containsKey("createdAt")) fields.put("createdAt", Timestamp.now());
        return users.document(deviceID).set(fields);
    }

    public Task<Void> updateUser(@NonNull String deviceID, Map<String, Object> fields) {
        return users.document(deviceID).set(fields, SetOptions.merge());
    }

    public Task<Void> deleteUser(@NonNull String deviceID) {;
        return users.document(deviceID).delete();
    }

    public DocumentReference hasUser(@NonNull String deviceID) {
        return users.document(deviceID);
    }


    // ---------- listeners --------
    public interface UserListener { void onChanged(@NonNull List<User> items); }
    public interface DocListener    { void onChanged(DocumentSnapshot doc); }

    public ListenerRegistration listenUser(@NonNull String userId, @NonNull DocListener l) {
        return users.document(userId)
                .addSnapshotListener((snap, err) -> { if (snap != null) l.onChanged(snap); });
    }

    // ---------- mapping ----------
    private List<User> mapList(QuerySnapshot snap) {
        if (snap == null || snap.isEmpty()) return Collections.emptyList();
        List<User> list = new ArrayList<>();
        for (DocumentSnapshot d : snap.getDocuments()) list.add(map(d));
        return list;
    }

    private User map(DocumentSnapshot d) {
        String deviceID = safe(d.getString("userDeviceID"));
        String name = safe(d.getString("name"));
        String email = safe(d.getString("email"));
        String phone_num = safe(d.getString("phoneNumber"));

        return new User(
                deviceID, name, email, phone_num
        );
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static void ensureArrays(Map<String, Object> map, String... keys) {
        for (String k : keys) if (!(map.get(k) instanceof List)) map.put(k, new ArrayList<String>());
    }
}


