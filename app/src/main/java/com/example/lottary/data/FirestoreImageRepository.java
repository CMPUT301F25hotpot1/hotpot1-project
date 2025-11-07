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

/**
 * 优先监听 Firestore 的 "images"（url/title/createdAt）;
 * 若集合为空，自动回退到 Firebase Storage 的 "images/" 目录。
 */
public class FirestoreImageRepository {

    public interface ImagesListener {
        void onChanged(@Nullable List<Image> images, @Nullable Exception error);
    }

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                if (value != null && !value.isEmpty()) {
                    List<Image> out = new ArrayList<>();
                    for (DocumentSnapshot d : value.getDocuments()) {
                        Image img = d.toObject(Image.class);
                        if (img != null) {
                            img.setId(d.getId());
                            if (img.getCreatedAt() == null) img.setCreatedAt(Timestamp.now());
                            out.add(img);
                        }
                    }
                    listener.onChanged(out, null);
                } else {
                    // Firestore 没数据 → 回退到 Storage
                    fallbackStorage(listener);
                }
            }
        });

        return new ListenerHandle(reg);
    }

    /** 回退：从 Firebase Storage 的 images/ 目录读取文件下载链接 */
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

                    Tasks.whenAllComplete(tasks).addOnCompleteListener(done ->
                            listener.onChanged(result, null)
                    ).addOnFailureListener(e ->
                            listener.onChanged(null, e)
                    );
                })
                .addOnFailureListener(e -> listener.onChanged(null, e));
    }

    /** 监听句柄 */
    public static class ListenerHandle {
        private final ListenerRegistration reg;
        public ListenerHandle(@NonNull ListenerRegistration r) { this.reg = r; }
        public void remove() { if (reg != null) reg.remove(); }
    }

    // ----------------------------------------------------------------------
    // ------------------------- ✨ 新增：删除相关 API -------------------------
    // ----------------------------------------------------------------------

    /** 删除回调 */
    public interface DeleteCallback { void onComplete(@Nullable Exception error); }

    /**
     * 删除 Firestore 中的一条图片文档。
     * 适用于“方案A：仅 Firestore 记录外链 URL”的情况。
     */
    public void deleteByFirestoreId(@NonNull String imageDocId, @NonNull DeleteCallback cb) {
        db.collection("images").document(imageDocId)
                .delete()
                .addOnSuccessListener(unused -> cb.onComplete(null))
                .addOnFailureListener(cb::onComplete);
    }

    /**
     * 删除 Storage 中的文件（images/{fileName}）。
     * 适用于“方案B：使用 Firebase Storage 存文件”的情况，
     * 例如回退列表里我们把 item.getName() 当成 id 展示时，可以用它来删除。
     */
    public void deleteByStorageName(@NonNull String fileName, @NonNull DeleteCallback cb) {
        FirebaseStorage.getInstance()
                .getReference()
                .child("images")
                .child(fileName)
                .delete()
                .addOnSuccessListener(unused -> cb.onComplete(null))
                .addOnFailureListener(cb::onComplete);
    }

    /**
     * 通过下载 URL 删除 Storage 文件（如果你只拿得到 URL）。
     * 注意：要求这个 URL 指向的就是你项目下的可删除资源。
     */
    public void deleteByStorageUrl(@NonNull String downloadUrl, @NonNull DeleteCallback cb) {
        try {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl);
            ref.delete()
                    .addOnSuccessListener(unused -> cb.onComplete(null))
                    .addOnFailureListener(cb::onComplete);
        } catch (Exception e) {
            cb.onComplete(e);
        }
    }
}