package com.example.lottary.data;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Image implements Serializable {
    private String id;
    private String url;
    private String title;
    private Timestamp createdAt;

    public Image() {}

    // ====== 原有 getter / setter（未改动）======
    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setUrl(String url) { this.url = url; }
    public void setTitle(String title) { this.title = title; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // ====== 新增：便捷构造 ======
    public Image(String id, String url, String title, Timestamp createdAt) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.createdAt = createdAt;
    }

    public Image(String id, String url, String title) {
        this(id, url, title, null);
    }

    // ====== 新增：安全取值/判断 ======
    public boolean hasValidId() { return id != null && !id.isEmpty(); }
    public boolean hasUrl() { return url != null && !url.isEmpty(); }
    public String safeTitle() { return title == null ? "" : title; }
    public long createdAtSeconds() { return createdAt == null ? 0L : createdAt.getSeconds(); }

    // ====== 新增：拷贝/派生 ======
    public Image copy() { return new Image(id, url, title, createdAt); }

    /** 返回“带新 id 的副本”，不改动当前对象 */
    public Image withId(String newId) { return new Image(newId, url, title, createdAt); }

    // ====== 新增：Firestore 互操作 ======
    /** 从 DocumentSnapshot 构建 Image；doc.getId() 作为 id */
    public static Image fromDoc(DocumentSnapshot doc) {
        if (doc == null) return null;
        Image img = new Image();
        img.id = doc.getId();
        Object u = doc.get("url");
        Object t = doc.get("title");
        Object c = doc.get("createdAt");
        img.url = u == null ? null : String.valueOf(u);
        img.title = t == null ? null : String.valueOf(t);
        img.createdAt = (c instanceof Timestamp) ? (Timestamp) c : null;
        return img;
    }

    /** 转换为 Firestore 可写 Map（不包含 id；id 由 document(id) 决定） */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        if (url != null) m.put("url", url);
        if (title != null) m.put("title", title);
        if (createdAt != null) m.put("createdAt", createdAt);
        return m;
    }

    /** 只读 Map 视图（便于安全传递） */
    public Map<String, Object> asReadOnlyMap() {
        return Collections.unmodifiableMap(toMap());
    }

    // ====== 新增：排序帮助器 ======
    /** 按创建时间倒序（最新在前）；若为空按 0 处理 */
    public static final Comparator<Image> BY_CREATED_DESC =
            (a, b) -> Long.compare(b == null ? 0L : b.createdAtSeconds(),
                    a == null ? 0L : a.createdAtSeconds());

    /** 按标题字典序，忽略大小写，空字符串排后 */
    public static final Comparator<Image> BY_TITLE_ASC = (a, b) -> {
        String at = a == null ? "" : a.safeTitle();
        String bt = b == null ? "" : b.safeTitle();
        if (at.isEmpty() && bt.isEmpty()) return 0;
        if (at.isEmpty()) return 1;
        if (bt.isEmpty()) return -1;
        return at.compareToIgnoreCase(bt);
    };

    // ====== 新增：equals/hashCode/toString（基于 id；id 为空时退化为父类实现）======
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Image)) return false;
        Image other = (Image) o;
        if (!hasValidId() || !other.hasValidId()) return super.equals(o);
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return hasValidId() ? id.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "Image{id='" + id + "', title='" + title + "', url='" + url
                + "', createdAt=" + (createdAt == null ? "null" : createdAt.toDate()) + "}";
    }
}