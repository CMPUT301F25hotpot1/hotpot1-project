package com.example.lottary.data;

public class Event {
    private final String id;
    private final String title;
    private final String city;
    private final String venue;
    private final String prettyStartTime;
    private final boolean full;

    public Event(String id, String title, String city, String venue, String prettyStartTime, boolean full) {
        this.id = id;
        this.title = title;
        this.city = city;
        this.venue = venue;
        this.prettyStartTime = prettyStartTime;
        this.full = full;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCity() { return city; }
    public String getVenue() { return venue; }
    public String getPrettyStartTime() { return prettyStartTime; }
    public boolean isFull() { return full; }
}
