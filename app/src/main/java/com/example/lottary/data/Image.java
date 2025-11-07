package com.example.lottary.data;

import com.google.firebase.Timestamp;

public class Image {
    private String id;
    private String url;
    private String title;
    private Timestamp createdAt;

    public Image() {}

    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setUrl(String url) { this.url = url; }
    public void setTitle(String title) { this.title = title; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
