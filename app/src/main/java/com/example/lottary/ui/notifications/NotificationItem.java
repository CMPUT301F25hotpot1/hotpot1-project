package com.example.lottary.ui.notifications;

public class NotificationItem {
    public final String id;
    public final String eventId;
    public final String targetGroup;
    public final String type;
    public final String message;
    public final long   sentAtMs;
    public final String eventTitle;

    public NotificationItem(String id, String eventId, String targetGroup,
                            String type, String message, long sentAtMs) {
        this(id, eventId, targetGroup, type, message, sentAtMs, "");
    }

    public NotificationItem(String id, String eventId, String targetGroup,
                            String type, String message, long sentAtMs, String eventTitle) {
        this.id = id;
        this.eventId = eventId;
        this.targetGroup = targetGroup;
        this.type = type;
        this.message = message;
        this.sentAtMs = sentAtMs;
        this.eventTitle = eventTitle;
    }
}
