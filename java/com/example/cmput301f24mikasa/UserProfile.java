package com.example.cmput301f24mikasa;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;

public class UserProfile implements Serializable {
    private String name;
    private String profilePicture;
    private String deviceId;
    private String gmailAddress;
    private String phoneNumber;
    private ArrayList<String> eventsJoined;  // New field for event IDs

    // Firestore instance for retrieving profile pictures
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // No-argument constructor required by Firestore
    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public UserProfile() {
        this.eventsJoined = new ArrayList<>();
    }

    // Constructor with profile picture provided
    public UserProfile(String name, String profilePicture, String deviceId, String gmailAddress, String phoneNumber) {
        this.name = name;
        this.deviceId = deviceId;
        this.gmailAddress = gmailAddress;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
        this.eventsJoined = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getGmailAddress() {
        return gmailAddress;
    }

    public void setGmailAddress(String gmailAddress) {
        this.gmailAddress = gmailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Getter and Setter for eventsJoined
    public ArrayList<String> getEventsJoined() {
        return eventsJoined;
    }

    public void setEventsJoined(ArrayList<String> eventsJoined) {
        this.eventsJoined = eventsJoined;
    }

    // Method to add an event ID to eventsJoined
    public void addEvent(String eventId) {
        if (!eventsJoined.contains(eventId)) {
            eventsJoined.add(eventId);
        }
    }
}