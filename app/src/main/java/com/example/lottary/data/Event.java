package com.example.lottary.data;

public class Event {

    private final String id;
    private final String title;
    private final String city;
    private final String province;   // ✅ 新增
    private final String venue;
    private final String prettyStartTime;
    private final boolean full;

    private final long startTimeMs;
    private final long registerStartMs;
    private final long registerEndMs;
    private final boolean geolocationEnabled;
    private final String type;

    private final String imageUrl;   // ✅ 新增
    private final String status;

    // ✅ 原来构造函数（兼容旧代码）
    public Event(
            String id, String title, String city, String venue,
            String prettyStartTime, boolean full,
            long startTimeMs, long registerStartMs, long registerEndMs,
            boolean geolocationEnabled, String type
    ) {
        this(id, title, city, "", venue, prettyStartTime, full,
                startTimeMs, registerStartMs, registerEndMs,
                geolocationEnabled, type, full ? "Full" : "Open", null);
    }

    // ✅ 完整构造函数（推荐使用）
    public Event(
            String id, String title, String city, String province, String venue,
            String prettyStartTime, boolean full,
            long startTimeMs, long registerStartMs, long registerEndMs,
            boolean geolocationEnabled, String type, String status, String imageUrl
    ) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.city = city == null ? "" : city;
        this.province = province == null ? "" : province;  // ✅
        this.venue = venue == null ? "" : venue;
        this.prettyStartTime = prettyStartTime == null ? "" : prettyStartTime;
        this.full = full;

        this.startTimeMs = startTimeMs;
        this.registerStartMs = registerStartMs;
        this.registerEndMs = registerEndMs;
        this.geolocationEnabled = geolocationEnabled;
        this.type = type == null ? "" : type;
        this.status = status == null ? "" : status;
        this.imageUrl = imageUrl;    // ✅
    }

    // ✅ Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getVenue() { return venue; }
    public String getPrettyStartTime() { return prettyStartTime; }
    public boolean isFull() { return full; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public String getPrettyTime() { return prettyStartTime; }

    public long getStartTimeMs() { return startTimeMs; }
    public long getRegisterStartMs() { return registerStartMs; }
    public long getRegisterEndMs() { return registerEndMs; }
    public boolean isGeolocationEnabled() { return geolocationEnabled; }
    public String getType() { return type; }
}
