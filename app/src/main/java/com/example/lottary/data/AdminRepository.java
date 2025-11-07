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

    // ✅ 所有事件（Firestore 原始数据）
    private List<Event> allEvents = new ArrayList<>();

    // ✅ LiveData 提供给 UI（过滤后的结果）
    private final MutableLiveData<List<Event>> eventsLive =
            new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Event>> events() { return eventsLive; }

    // ✅ 搜索 / 筛选变量
    private String searchQuery = "";
    private String filterStatus = "ALL"; // ALL / OPEN / CLOSED / FULL

    @Nullable
    private ListenerRegistration eventsReg;

    // ============================
    // ✅ Firestore realtime listen
    // ============================

    @MainThread
    public void startAdminEventsRealtime() {
        stopAdminEventsRealtime();
        eventsReg = FirestoreEventRepository.get().listenRecentCreated(items -> {
            // Firestore 回调新数据
            setEventsFromFirestore(items);
        });
    }

    @MainThread
    public void stopAdminEventsRealtime() {
        if (eventsReg != null) {
            eventsReg.remove();
            eventsReg = null;
        }
    }

    // ✅ Firestore 返回最新列表后保存
    public void setEventsFromFirestore(List<Event> items) {
        allEvents = new ArrayList<>(items);
        applyFilters();
    }

    // ============================
    // ✅ 搜索 / 筛选逻辑
    // ============================

    public void search(String query) {
        searchQuery = (query == null ? "" : query.trim());
        applyFilters();
    }

    public void setFilter(String status) {
        filterStatus = status; // "ALL", "OPEN", "CLOSED", "FULL"
        applyFilters();
    }

    // ✅ 根据搜索词 + 状态动态生成列表
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

    // ============================
    // ✅ UI 移除某事件（仅本地，不删 Firestore）
    // ============================

    public void removeEvent(Event event) {
        if (event == null) return;

        allEvents.remove(event);
        applyFilters(); // 保持当前筛选
    }
}