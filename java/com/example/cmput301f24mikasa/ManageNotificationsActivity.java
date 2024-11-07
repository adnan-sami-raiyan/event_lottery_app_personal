package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageNotificationsActivity extends AppCompatActivity {
    private ListView notificationListView;
    private NotificationArrayAdapter adapter;
    private List<Notifications> notificationList;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        notificationList = new ArrayList<>();
        notificationListView = findViewById(R.id.notification_list_view);
        adapter = new NotificationArrayAdapter(this, notificationList);
        notificationListView.setAdapter(adapter);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load notifications if enabled
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        if (notificationsEnabled) {
            loadNotifications();
        } else {
            Toast.makeText(this, "Notifications are disabled.", Toast.LENGTH_SHORT).show();
        }

        // Back button
        Button btnBack = findViewById(R.id.back_button);
        btnBack.setOnClickListener(v -> finish());

        // Settings button
        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ManageNotificationsActivity.this, NotificationSettingsActivity.class);
            startActivity(intent);
        });
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
                            // Fetch only the necessary fields
                            String text = document.getString("text");
                            Notifications notification = new Notifications(text); // Only `text` is required
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
}
