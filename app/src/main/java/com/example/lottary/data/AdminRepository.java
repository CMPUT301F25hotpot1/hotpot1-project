/**
 * AdminRepository
 *
 * Purpose:
 * Repository for the admin side that subscribes to Firestore for recently
 * created events, caches them in memory, and exposes a filtered list via
 * LiveData for UI observation.
 *
 * Role / Pattern:
 * Implements a singleton Repository for admin event data. It bridges the
 * Firestore data source and the presentation layer, applying local search
 * and status filters before publishing results.
 *
 * Key Behaviors:
 * - Start/stop a realtime Firestore listener for recent created events.
 * - Maintain an in-memory cache of all events received.
 * - Apply text query and status filters and post the filtered list as LiveData.
 * - Support temporary local removal of an event for UI purposes only.
 *
 * Outstanding Issues / Notes:
 * - Cache is in-memory; no persistence across process death.
 * - Filtering is done on the full list in-process; consider server-side
 *   queries or background threading for very large datasets.
 * - No debounce/throttle on search updates.
 * - Caller must pair start/stop with lifecycle to avoid leaks.
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

    // ---------------------------------------------------------------------
    // Events section: original logic
    // ---------------------------------------------------------------------

    // Raw Firestore event list
    private List<Event> allEvents = new ArrayList<>();

    // Filtered list exposed to UI
    private final MutableLiveData<List<Event>> eventsLive =
            new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Event>> events() {
        return eventsLive;
    }

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
        applyFilters();            // refresh events LiveData
        rebuildImagesFromEvents(); // keep images LiveData in sync
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
        // Also remove the corresponding image from the images list
        removeImageForEvent(event);
    }

    // ---------------------------------------------------------------------
    // Images section: derived from events.imageUrl, used by Admin Images screen
    // ---------------------------------------------------------------------

    // All images (unfiltered)
    private List<Image> allImages = new ArrayList<>();

    // Filtered image list exposed to UI
    private final MutableLiveData<List<Image>> imagesLive =
            new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Image>> images() {
        return imagesLive;
    }

    // Search query for the images screen
    private String imageSearchQuery = "";

    /**
     * Rebuild the image list from the current allEvents:
     * - For each Event that has an imageUrl, create an Image object.
     * - Image.id = Event.id, Image.title = Event.title, Image.url = Event.imageUrl.
     */
    private void rebuildImagesFromEvents() {
        List<Image> rebuilt = new ArrayList<>();

        for (Event e : allEvents) {
            // Use the existing imageUrl field from Event
            String url = e.getImageUrl();
            if (url == null || url.trim().isEmpty()) {
                continue; // skip events without an image
            }

            Image img = new Image();
            img.setId(e.getId());
            img.setUrl(url);
            img.setTitle(e.getTitle());
            // If you later add createdAt on Event, you can also set it here:
            // img.setCreatedAt(e.getCreatedAt());

            rebuilt.add(img);
        }

        allImages = rebuilt;
        applyImageFilters();
    }

    /**
     * Search entry point for the Admin Images screen
     * (called from the Activity).
     */
    public void searchImages(String query) {
        imageSearchQuery = (query == null ? "" : query.trim());
        applyImageFilters();
    }

    /**
     * Apply filtering on allImages based on imageSearchQuery
     * and update the images LiveData.
     */
    private void applyImageFilters() {
        List<Image> result = new ArrayList<>();

        String q = imageSearchQuery.toLowerCase();

        for (Image img : allImages) {
            String title = img.getTitle();
            boolean matchesSearch =
                    q.isEmpty() ||
                            (title != null && title.toLowerCase().contains(q));

            if (matchesSearch) {
                result.add(img);
            }
        }

        imagesLive.postValue(result);
    }

    /**
     * When an Event is locally removed, remove the corresponding Image as well
     * so that the images list stays consistent.
     */
    private void removeImageForEvent(Event event) {
        if (event == null) return;
        String eventId = event.getId();
        if (eventId == null) return;

        List<Image> remaining = new ArrayList<>();
        for (Image img : allImages) {
            if (!eventId.equals(img.getId())) {
                remaining.add(img);
            }
        }
        allImages = remaining;
        applyImageFilters();
    }
}
