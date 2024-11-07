package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * AdminActivity provides the main dashboard for admins to manage events,
 * user profiles, images, and facilities. It includes navigation buttons that
 * redirect to the appropriate management sections.
 */
public class AdminActivity extends AppCompatActivity {

    private Button manageFacilitiesButton;
    private Button manageProfilesButton;
    private Button manageImagesButton;
    private Button manageEventsButton;

    /**
     * Initializes the activity, setting up the admin dashboard and button click listeners
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize bottom navigation
        NavigationActivity.setupBottomNavigation(this);

        // Initialize buttons
        manageFacilitiesButton = findViewById(R.id.button_manage_facilities);
        manageProfilesButton = findViewById(R.id.button_manage_profiles);
        manageImagesButton = findViewById(R.id.button_manage_images);
        manageEventsButton = findViewById(R.id.button_manage_events);

        // Set click listener for Manage Facilities
        manageFacilitiesButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, ManageFacilitiesActivity.class);
            startActivity(intent);
        });

        // Set click listener for Manage Profiles
        manageProfilesButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, ManageProfileActivity.class);
            startActivity(intent);
        });

        // Set click listener for Manage Images
        manageImagesButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, ManageImagesActivity.class);
            startActivity(intent);
        });

        // Set click listener for Manage Events
        manageEventsButton.setOnClickListener(view -> {
            Intent intent = new Intent(AdminActivity.this, AdminManageEvents.class);
            startActivity(intent);
        });
    }
}
