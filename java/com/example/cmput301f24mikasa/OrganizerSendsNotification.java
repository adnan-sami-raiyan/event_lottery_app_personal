package com.example.cmput301f24mikasa;


import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cmput301f24mikasa.NavigatonActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//remember to adjust all the profiles, so that getName becomes an actual name
public class OrganizerSendsNotification  extends AppCompatActivity{
    ArrayList<UserProfile> selectedEntrants;
    ArrayList<UserProfile> dataList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notificationRef = db.collection("notification");
    private CollectionReference eventsRef = db.collection("event");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Intent intent = getIntent();
        String eventID=intent.getStringExtra("eventID");
        String eventTitle = intent.getStringExtra("eventTitle");
        selectedEntrants = new ArrayList<>();
        dataList = new ArrayList<>();
        fetchSelectedList(eventID, eventTitle);
        fetchWaitingList(eventID,eventTitle);



        finish();
    }
    /**
    private void sendNotification(String eventID) {
        // Create a new document reference in the notification collection
        DocumentReference documentReference = notificationRef.document();
        String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // Prepare data for Firestore
        HashMap<String, Object> notificationData = new HashMap<>();
        notificationData.put("deviceID", deviceID); // Using provided deviceID
        notificationData.put("eventID", eventID);
        notificationData.put("text", "hello AP");

        // Add data to Firestore and show success/failure toasts
        documentReference.set(notificationData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OrganizerSendsNotification.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OrganizerSendsNotification.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                });
    }
     */
    private void sendNotificationSelectedList(String eventID, String eventTitle) {
        String eventText = "Congratulations! You were selected for the " + eventTitle;
        Toast.makeText(this, "111111111111111111111", Toast.LENGTH_SHORT).show();
        for (UserProfile userProfile : selectedEntrants) {
            Toast.makeText(this, "222222222222222222", Toast.LENGTH_SHORT).show();
            String deviceID = userProfile.getName();  // Assuming deviceID is stored in the name field of UserProfile
            Toast.makeText(this, "333333333333333333", Toast.LENGTH_SHORT).show();
            // Create a new document reference in the notification collection
            DocumentReference documentReference = notificationRef.document();
            Toast.makeText(this, "4444444444444", Toast.LENGTH_SHORT).show();
            String notificationID = documentReference.getId();
            // Prepare data for Firestore
            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceID", deviceID);
            notificationData.put("eventID", eventID);
            notificationData.put("text", eventText);
            notificationData.put("responsive", "1");
            notificationData.put("notificationID", notificationID);

            // Add data to Firestore and show success/failure toasts
            documentReference.set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(OrganizerSendsNotification.this, "Notification sent successfully to selected entrant: " + deviceID, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(OrganizerSendsNotification.this, "Failed to send notification to selected entrant: " + deviceID, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void sendNotificationNotAccepted(String eventID, String eventTitle) {
        String eventText = "You were not chosen for " + eventTitle;
        for (UserProfile userProfile : dataList) {
            String deviceID = userProfile.getName();  // Assuming deviceID is stored in the name field of UserProfile

            // Create a new document reference in the notification collection
            DocumentReference documentReference = notificationRef.document();
            String notificationID = documentReference.getId();
            // Prepare data for Firestore
            HashMap<String, Object> notificationData = new HashMap<>();
            notificationData.put("deviceID", deviceID);
            notificationData.put("eventID", eventID);
            notificationData.put("text", eventText);
            notificationData.put("notificationID", notificationID);

            // Add data to Firestore and show success/failure toasts
            documentReference.set(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(OrganizerSendsNotification.this, "Notification sent to non-selected entrant: " + deviceID, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(OrganizerSendsNotification.this, "Failed to send notification to non-selected entrant: " + deviceID, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void fetchSelectedList(String eventID, String eventTitle) {
        // Query the event document by eventID to get the waitingList array
        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the waitingList array from the document
                        List<String> selectedIDs = (List<String>) documentSnapshot.get("selectedEntrants");
                        if (selectedIDs != null && !selectedIDs.isEmpty()) {
                            // For each deviceID in the waiting list, add it as a placeholder user name
                            for (String deviceID : selectedIDs) {
                                // Create a UserProfile with deviceID as the name
                                UserProfile userProfile = new UserProfile();
                                userProfile.setName(deviceID); // Set deviceID as a placeholder for name
                                //later on, instead of displaying deviceID will want to display later
                                //name associated with deviceID
                                selectedEntrants.add(userProfile);
                            }
                            sendNotificationSelectedList(eventID, eventTitle);
                        } else {
                            Toast.makeText(this, "Waiting list is empty for this event.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching event details", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
    private void fetchWaitingList(String eventID, String eventTitle) {
        // Query the event document by eventID to get the waitingList array
        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the waitingList array and alreadySampled from the document
                        List<String> waitingListIds = (List<String>) documentSnapshot.get("waitingList");

                        if (waitingListIds != null && !waitingListIds.isEmpty()) {
                            // Populate dataList with deviceIDs from waitingList
                            for (String deviceID : waitingListIds) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.setName(deviceID); // Set deviceID as a placeholder for name
                                dataList.add(userProfile);
                            }
                            sendNotificationNotAccepted(eventID, eventTitle);
                        } else {
                            Toast.makeText(this, "Waiting list is empty for this event.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching event details", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}