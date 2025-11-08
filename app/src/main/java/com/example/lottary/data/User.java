/**
 * Model representing an application user.
 * Stores identity fields along with device ID and notification preferences.
 */
package com.example.lottary.data;

import com.example.lottary.ui.notifications.NotifyPrefs;

public class User {

    private String name;
    private String email;
    private String phoneNum;
    private String deviceID;
    private NotifyPrefs notifyPrefs;

    /** Required by Firestore */
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

    /** Setter for device ID (kept for Firestore updates) */
    public void setUserDeviceId(String userDeviceId) {
        this.deviceID = userDeviceId;
    }

    // ---- Basic getters/setters ----
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNum() { return phoneNum; }
    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }

    public String getDeviceID() { return deviceID; }

    /** Alias for deviceID (consistent with other models using getId()) */
    public String getId() { return deviceID; }
}
