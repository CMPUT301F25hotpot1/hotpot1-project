package com.example.lottary.data;

import com.example.lottary.ui.notifications.NotifyPrefs;

public class User {

    private String name;
    private String email;
    private String phoneNum;
    private String deviceID;
    private NotifyPrefs notifyPrefs;

    // Firestore 需要空构造
    public User() {
        this.notifyPrefs = new NotifyPrefs();
    }

    public User(String name, String email, String phoneNum, String deviceID) {
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.deviceID = deviceID;
        this.notifyPrefs = new NotifyPrefs();
    }

    // ✅ 这一行是新增的（不要删别的）
    public void setUserDeviceId(String userDeviceId) {
        this.deviceID = userDeviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getId() {
        return deviceID;
    }
}
