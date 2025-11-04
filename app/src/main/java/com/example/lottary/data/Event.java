package com.example.lottary.data;


public class Event {
    private final String id;
    private final String title;
    private final String city;
    private final String venue;
    private final String prettyStartTime;
    private final boolean full;

    private final long startTimeMs;         // Firestore: startTime
    private final long registerStartMs;     // Firestore: registerStart
    private final long registerEndMs;       // Firestore: registerEnd
    private final boolean geolocationEnabled; // Firestore: geolocationEnabled
    private final String type;              // Firestore: type（可空/空串）

    public Event(
            String id, String title, String city, String venue,
            String prettyStartTime, boolean full,
            long startTimeMs, long registerStartMs, long registerEndMs,
            boolean geolocationEnabled, String type
    ) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.city = city == null ? "" : city;
        this.venue = venue == null ? "" : venue;
        this.prettyStartTime = prettyStartTime == null ? "" : prettyStartTime;
        this.full = full;
        this.startTimeMs = startTimeMs;
        this.registerStartMs = registerStartMs;
        this.registerEndMs = registerEndMs;
        this.geolocationEnabled = geolocationEnabled;
        this.type = type == null ? "" : type;
    }


    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCity() { return city; }
    public String getVenue() { return venue; }
    public String getPrettyStartTime() { return prettyStartTime; }
    public boolean isFull() { return full; }

    public long getStartTimeMs() { return startTimeMs; }
    public long getRegisterStartMs() { return registerStartMs; }
    public long getRegisterEndMs() { return registerEndMs; }
    public boolean isGeolocationEnabled() { return geolocationEnabled; }
    public String getType() { return type; }


    public String getPrettyTime() { return prettyStartTime; }
}


