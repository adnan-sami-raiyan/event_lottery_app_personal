package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class NotificationSettingsActivity extends AppCompatActivity {

    private SwitchCompat switchNotifications;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        switchNotifications = findViewById(R.id.switch_notifications);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved notification setting
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        switchNotifications.setChecked(notificationsEnabled);

        // Set listener to update preference when toggle state changes
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked);
            editor.apply();
        });

        Button backButton = findViewById(R.id.back_button);
        // Set listener for the back button
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationSettingsActivity.this, ManageNotificationsActivity.class);
            startActivity(intent); finish();
        });


    }
}
