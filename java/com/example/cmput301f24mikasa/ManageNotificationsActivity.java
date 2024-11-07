package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageNotificationsActivity extends AppCompatActivity {
    private ListView notificationListView;
    private NotificationArrayAdapter adapter;
    private List<Notifications> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        notificationList = new ArrayList<>();
        notificationListView = findViewById(R.id.notification_list_view);
        adapter = new NotificationArrayAdapter(this, notificationList);
        notificationListView.setAdapter(adapter);

        // Load notifications from Firestore
        loadNotifications();

        // Set up click listener for notifications
        setupNotificationClickListener();

        // Back button
        Button btnBack = findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadNotifications() {
        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        notificationList.clear();

        db.collection("notification")
                .whereEqualTo("deviceID", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No notifications found for your device.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String text = document.getString("text");
                            String notificationId = document.getId();
                            Notifications notification = new Notifications(notificationId, text); // Include ID for reference
                            notificationList.add(notification);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching notifications: ", e);
                    Toast.makeText(this, "Failed to load notifications.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupNotificationClickListener() {
        notificationListView.setOnItemClickListener((parent, view, position, id) -> {
            Notifications selectedNotification = notificationList.get(position);

            // Check if "responsive" is set to "1"
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference notificationRef = db.collection("notification").document(selectedNotification.getNotificationID());

            notificationRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String responsive = documentSnapshot.getString("responsive");

                    if ("1".equals(responsive)) {
                        // Show dialog for Accept/Decline
                        new AlertDialog.Builder(this)
                                .setTitle("Respond to Notification")
                                .setMessage("Would you like to accept or decline?")
                                .setPositiveButton("Accept", (dialog, which) -> {
                                    // Update Firestore with "yes"
                                    updateNotificationResponse(notificationRef, "yes");
                                })
                                .setNegativeButton("Decline", (dialog, which) -> {
                                    // Update Firestore with "no"
                                    updateNotificationResponse(notificationRef, "no");
                                })
                                .show();
                    } else {
                        Toast.makeText(this, "Notification is not responsive.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("ManageNotificationsActivity", "Error retrieving notification", e);
            });
        });
    }

    private void updateNotificationResponse(DocumentReference notificationRef, String response) {
        notificationRef.update("responsive", response)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Response updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("ManageNotificationsActivity", "Error updating response", e);
                    Toast.makeText(this, "Failed to update response", Toast.LENGTH_SHORT).show();
                });
    }
}
