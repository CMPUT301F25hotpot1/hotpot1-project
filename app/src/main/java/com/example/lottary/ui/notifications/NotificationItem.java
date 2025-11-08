/*
 * NotificationItem.java
 *
 * Model class for representing a single notification record in the app.
 * Used by NotificationsActivity and NotificationsAdapter to render the
 * notification inbox for an entrant device.
 *
 * Outstanding issues:
 * - This class is intentionally minimal and does not enforce a fixed
 *   enum for type/targetGroup; values are validated at higher layers.
 */

package com.example.lottary.ui.notifications;

/**
 * Immutable value object representing a notification displayed in the
 * notifications inbox.
 *
 * Instances are typically constructed from a Firestore document and
 * rendered by {@link NotificationsAdapter} within {@link NotificationsActivity}.
 * All nullable inputs are normalized to empty strings to simplify UI rendering.
 */
public class NotificationItem {

    public final String id;

    public final String eventId;

    public final String targetGroup;

    public final String type;

    public final String message;

    public final long sentAtMs;

    public final String eventTitle;

    public final String organizerId;

    /**
     * Creates a notification with basic fields.
     *
     * @param id          document id of this notification
     * @param eventId     id of the related event (may be {@code null})
     * @param targetGroup logical target group label
     * @param type        notification type
     * @param message     message body
     * @param sentAtMs    time the notification was sent, in epoch millis
     */
    public NotificationItem(String id, String eventId, String targetGroup,
                            String type, String message, long sentAtMs) {
        this(id, eventId, targetGroup, type, message, sentAtMs, "", "");
    }

    /**
     * Creates a notification with additional display metadata.
     *
     * @param id           document id of this notification
     * @param eventId      id of the related event (may be {@code null})
     * @param targetGroup  logical target group label
     * @param type         notification type
     * @param message      message body
     * @param sentAtMs     time the notification was sent, in epoch millis
     * @param eventTitle   title of the related event (may be {@code null})
     * @param organizerId  organizer id for opt-out filtering (may be {@code null})
     */
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
