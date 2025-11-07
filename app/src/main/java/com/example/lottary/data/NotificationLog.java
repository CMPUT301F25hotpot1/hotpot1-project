package com.example.lottary.data;

import com.google.firebase.Timestamp;

public class NotificationLog {

    private final String id;
    private final String title;
    private final String recipientName;
    private final String message;
    private final Timestamp timestamp;

    public NotificationLog(String id, String title, String recipientName, String message, Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.recipientName = recipientName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getRecipientName() { return recipientName; }
    public String getMessage() { return message; }
    public Timestamp getTimestamp() { return timestamp; }

    public String getPrettyTime() {
        return timestamp.toDate().toString();
    }
}
