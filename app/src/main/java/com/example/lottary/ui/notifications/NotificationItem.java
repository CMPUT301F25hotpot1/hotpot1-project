package com.example.lottary.ui.notifications;

public class NotificationItem {
    public final String id;
    public final String eventId;
    public final String targetGroup;
    public final String type;
    public final String message;
    public final long   sentAtMs;
    public final String eventTitle;
    public final String organizerId; // 用于按组织者静音

    public NotificationItem(String id, String eventId, String targetGroup,
                            String type, String message, long sentAtMs) {
        this(id, eventId, targetGroup, type, message, sentAtMs, "", "");
    }

    public NotificationItem(String id, String eventId, String targetGroup,
                            String type, String message, long sentAtMs,
                            String eventTitle, String organizerId) {
        this.id = id == null ? "" : id;
        this.eventId = eventId == null ? "" : eventId;
        this.targetGroup = targetGroup == null ? "" : targetGroup;
        this.type = type == null ? "" : type;
        this.message = message == null ? "" : message;
        this.sentAtMs = sentAtMs;
        this.eventTitle = eventTitle == null ? "" : eventTitle;
        this.organizerId = organizerId == null ? "" : organizerId;
    }
}

