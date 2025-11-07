/**
 * Repository for managing admin-side event data.
 * Provides realtime Firestore updates, search filtering,
 * status filtering, and exposes results through LiveData.
 */
package com.example.lottary.data;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AdminRepository {

    private static AdminRepository INSTANCE;

    public static synchronized AdminRepository get() {
        if (INSTANCE == null) INSTANCE = new AdminRepository();
        return INSTANCE;
    }

    private AdminRepository() {}

    // Raw Firestore event list
    private List<Event> allEvents = new ArrayList<>();

    // Filtered list exposed to UI
    private final MutableLiveData<List<Event>> eventsLive =
            new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Event>> events() { return eventsLive; }

    // Search / filter state
    private String searchQuery = "";
    private String filterStatus = "ALL";

    @Nullable
    private ListenerRegistration eventsReg;

    // Start realtime listener
    @MainThread
    public void startAdminEventsRealtime() {
        stopAdminEventsRealtime();
        eventsReg = FirestoreEventRepository.get().listenRecentCreated(items -> {
            setEventsFromFirestore(items);
        });
    }

    // Stop realtime listener
    @MainThread
    public void stopAdminEventsRealtime() {
        if (eventsReg != null) {
            eventsReg.remove();
            eventsReg = null;
        }
    }

    // Receive new Firestore data
    public void setEventsFromFirestore(List<Event> items) {
        allEvents = new ArrayList<>(items);
        applyFilters();
    }

    // Update search query
    public void search(String query) {
        searchQuery = (query == null ? "" : query.trim());
        applyFilters();
    }

    // Update status filter
    public void setFilter(String status) {
        filterStatus = status;
        applyFilters();
    }

    // Apply search + status filters
    private void applyFilters() {
        List<Event> result = new ArrayList<>();

        for (Event e : allEvents) {

            boolean matchesSearch =
                    searchQuery.isEmpty() ||
                            e.getTitle().toLowerCase().contains(searchQuery.toLowerCase());

            boolean matchesStatus =
                    filterStatus.equals("ALL") ||
                            e.getStatus().equalsIgnoreCase(filterStatus);

            if (matchesSearch && matchesStatus) {
                result.add(e);
            }
        }

        eventsLive.postValue(result);
    }

    // Remove event locally (UI only; does not modify Firestore)
    public void removeEvent(Event event) {
        if (event == null) return;
        allEvents.remove(event);
        applyFilters();
    }
}
