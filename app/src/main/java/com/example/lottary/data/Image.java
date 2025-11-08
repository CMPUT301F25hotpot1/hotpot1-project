/**
 * Model representing an image record stored in Firestore or Firebase Storage.
 * Supports construction from Firestore documents, safe value access, copying,
 * and helpers for sorting, equality, and serialization.
 */
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

    // ---------------- Getters / Setters ----------------
    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setUrl(String url) { this.url = url; }
    public void setTitle(String title) { this.title = title; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // ---------------- Convenience constructors ----------------
    public Image(String id, String url, String title, Timestamp createdAt) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.createdAt = createdAt;
    }

    public Image(String id, String url, String title) {
        this(id, url, title, null);
    }

    // ---------------- Safe accessors ----------------
    public boolean hasValidId() { return id != null && !id.isEmpty(); }
    public boolean hasUrl() { return url != null && !url.isEmpty(); }
    public String safeTitle() { return title == null ? "" : title; }
    public long createdAtSeconds() { return createdAt == null ? 0L : createdAt.getSeconds(); }

    // ---------------- Copy helpers ----------------
    public Image copy() { return new Image(id, url, title, createdAt); }

    /** Returns a new copy with a different id */
    public Image withId(String newId) { return new Image(newId, url, title, createdAt); }

    // ---------------- Firestore interop ----------------
    /** Build Image from Firestore document (using doc ID as id) */
    public static Image fromDoc(DocumentSnapshot doc) {
        if (doc == null) return null;

        Image img = new Image();
        img.id = doc.getId();
        Object u = doc.get("url");
        Object t = doc.get("title");
        Object c = doc.get("createdAt");

        img.url = (u == null) ? null : String.valueOf(u);
        img.title = (t == null) ? null : String.valueOf(t);
        img.createdAt = (c instanceof Timestamp) ? (Timestamp) c : null;

        return img;
    }

    /** Convert to Firestore-friendly writable Map (id excluded) */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        if (url != null) m.put("url", url);
        if (title != null) m.put("title", title);
        if (createdAt != null) m.put("createdAt", createdAt);
        return m;
    }

    /** Returns an unmodifiable read-only map view */
    public Map<String, Object> asReadOnlyMap() {
        return Collections.unmodifiableMap(toMap());
    }

    // ---------------- Sorting helpers ----------------
    /** Sort by createdAt DESC (latest first) */
    public static final Comparator<Image> BY_CREATED_DESC =
            (a, b) -> Long.compare(
                    b == null ? 0L : b.createdAtSeconds(),
                    a == null ? 0L : a.createdAtSeconds()
            );

    /** Sort by title ASC (case-insensitive, empty titles last) */
    public static final Comparator<Image> BY_TITLE_ASC = (a, b) -> {
        String at = (a == null) ? "" : a.safeTitle();
        String bt = (b == null) ? "" : b.safeTitle();
        if (at.isEmpty() && bt.isEmpty()) return 0;
        if (at.isEmpty()) return 1;
        if (bt.isEmpty()) return -1;
        return at.compareToIgnoreCase(bt);
    };

    // ---------------- Equality / hash / debug ----------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Image)) return false;
        Image other = (Image) o;

        // Only compare by id if valid; otherwise fallback to object identity
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
