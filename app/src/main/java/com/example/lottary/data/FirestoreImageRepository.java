/**
 * Repository for loading, listening, and deleting image data.
 * Uses Firestore as the primary source and falls back to Firebase Storage when empty.
 * Includes helper APIs for deleting images by Firestore id, Storage name, or download URL.
 */
package com.example.lottary.data;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FirestoreImageRepository {

    /** Callback for realtime image updates */
    public interface ImagesListener {
        void onChanged(@Nullable List<Image> images, @Nullable Exception error);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Listen for latest images; fallback to Storage if Firestore empty */
    public ListenerHandle listenLatest(ImagesListener listener) {
        Query q = db.collection("images")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(200);

        ListenerRegistration reg = q.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    listener.onChanged(null, error);
                    return;
                }

                // Firestore has data
                if (value != null && !value.isEmpty()) {
                    List<Image> out = new ArrayList<>();
                    for (DocumentSnapshot d : value.getDocuments()) {
                        Image img = d.toObject(Image.class);
                        if (img != null) {
                            img.setId(d.getId());
                            // Ensure createdAt exists
                            if (img.getCreatedAt() == null) img.setCreatedAt(Timestamp.now());
                            out.add(img);
                        }
                    }
                    listener.onChanged(out, null);
                } else {
                    // No Firestore data → try Storage
                    fallbackStorage(listener);
                }
            }
        });

        return new ListenerHandle(reg);
    }

    /** Storage fallback: list files under images/ directory */
    private void fallbackStorage(ImagesListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference dir = storage.getReference().child("images");

        dir.listAll()
                .addOnSuccessListener((ListResult list) -> {
                    List<StorageReference> items = list.getItems();
                    if (items.isEmpty()) {
                        listener.onChanged(new ArrayList<>(), null);
                        return;
                    }

                    List<Image> result = new ArrayList<>();
                    List<Task<Uri>> tasks = new ArrayList<>();

                    // Fetch download URLs for each file
                    for (StorageReference item : items) {
                        Task<Uri> t = item.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    Image img = new Image();
                                    img.setId(item.getName());
                                    img.setUrl(uri.toString());
                                    img.setTitle(item.getName());
                                    img.setCreatedAt(Timestamp.now());
                                    synchronized (result) { result.add(img); }
                                });

                        tasks.add(t);
                    }

                    // Wait for all download URL tasks to complete
                    Tasks.whenAllComplete(tasks)
                            .addOnCompleteListener(done -> listener.onChanged(result, null))
                            .addOnFailureListener(e -> listener.onChanged(null, e));
                })
                .addOnFailureListener(e -> listener.onChanged(null, e));
    }

    /** Wrapper for unregistering Firestore listener */
    public static class ListenerHandle {
        private final ListenerRegistration reg;
        public ListenerHandle(@NonNull ListenerRegistration r) { this.reg = r; }
        public void remove() { if (reg != null) reg.remove(); }
    }

    // ----------------------------------------------------------------------
    // Deletion APIs
    // ----------------------------------------------------------------------

    /** Callback for delete operations */
    public interface DeleteCallback { void onComplete(@Nullable Exception error); }

    /** Delete Firestore document by id */
    public void deleteByFirestoreId(@NonNull String imageDocId, @NonNull DeleteCallback cb) {
        db.collection("images").document(imageDocId)
                .delete()
                .addOnSuccessListener(unused -> cb.onComplete(null))
                .addOnFailureListener(cb::onComplete);
    }

    /** Delete Storage file using file name under images/ */
    public void deleteByStorageName(@NonNull String fileName, @NonNull DeleteCallback cb) {
        FirebaseStorage.getInstance()
                .getReference()
                .child("images")
                .child(fileName)
                .delete()
                .addOnSuccessListener(unused -> cb.onComplete(null))
                .addOnFailureListener(cb::onComplete);
    }

    /** Delete Storage file using download URL */
    public void deleteByStorageUrl(@NonNull String downloadUrl, @NonNull DeleteCallback cb) {
        try {
            StorageReference ref =
                    FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl);

            ref.delete()
                    .addOnSuccessListener(unused -> cb.onComplete(null))
                    .addOnFailureListener(cb::onComplete);

        } catch (Exception e) {
            cb.onComplete(e);
        }
    }
}
