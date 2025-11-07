package com.example.lottary.data;

public class Event {
    private final String id;
    private final String title;
    private final String city;
    private final String venue;
    private final String prettyStartTime;
    private final boolean full;

    private final long startTimeMs;
    private final long registerStartMs;
    private final long registerEndMs;
    private final boolean geolocationEnabled;
    private final String type;

    // ✅ 新增：状态字段（不影响现有构造函数调用）
    private final String status;

    // ✅ 你原来的完整构造函数（保持完全不变）
    public Event(
            String id, String title, String city, String venue,
            String prettyStartTime, boolean full,
            long startTimeMs, long registerStartMs, long registerEndMs,
            boolean geolocationEnabled, String type
    ) {
        this(id, title, city, venue, prettyStartTime, full,
                startTimeMs, registerStartMs, registerEndMs,
                geolocationEnabled, type, full ? "Full" : "Open");  // 默认自动生成状态
    }

    // ✅ 新增：带 status 的构造函数（Adapter 可以选用）
    public Event(
            String id, String title, String city, String venue,
            String prettyStartTime, boolean full,
            long startTimeMs, long registerStartMs, long registerEndMs,
            boolean geolocationEnabled, String type, String status
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
        this.status = status == null ? "" : status;
    }

    // ✅ 新增 getter
    public String getStatus() { return status; }

    // ✅ 以下全部是你原本的 getter（没有动）
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
