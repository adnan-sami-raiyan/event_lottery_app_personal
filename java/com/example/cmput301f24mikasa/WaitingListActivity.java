package com.example.cmput301f24mikasa;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ListView;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
//change sp tjat you use a proper getter for device id not name lol

public class WaitingListActivity extends AppCompatActivity {
    private int setting_buttons = 0;
    private ArrayList<UserProfile> dataList;
    private ListView userList;
    private UserProfileArrayAdapter userAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventsRef = db.collection("event");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_waiting_list);

        // Set up bottom navigation
        NavigatonActivity.setupBottomNavigation(this);

        // Retrieve the event title and eventID passed from OrganizerManageEventsActivity
        Intent intent = getIntent();
        String eventID = intent.getStringExtra("eventID");
        String eventTitle = intent.getStringExtra("eventTitle");

        // Display the event details
        Toast.makeText(this, "Event ID: " + eventID, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Event title: " + eventTitle, Toast.LENGTH_SHORT).show();

        // Set the header text with the event title
        TextView headerTextView = findViewById(R.id.headerTextView);
        headerTextView.setText("Waiting List For " + eventTitle);

        // Initialize the back button
        Button backButton = findViewById(R.id.back_button_for_waiting);
        backButton.setOnClickListener(view -> finish());

        // Initialize the list and adapter for waiting list users
        dataList = new ArrayList<>();
        userList = findViewById(R.id.waiting_list_view);
        userAdapter = new UserProfileArrayAdapter(this, dataList);
        userList.setAdapter(userAdapter);

        // Initialize buttons
        Button sampleButton = findViewById(R.id.sample_button);
        Button viewResults = findViewById(R.id.view_results);

        // Fetch the waiting list from Firestore for the given event ID
        fetchWaitingList(eventID, sampleButton, viewResults);

        sampleButton.setOnClickListener(v -> {
            // Display a toast message
            Toast.makeText(WaitingListActivity.this, "Sampling!", Toast.LENGTH_SHORT).show();
            // Start ListSamplingActivity and pass the deviceIDs list
            Intent samplingIntent = new Intent(WaitingListActivity.this, ListSamplingActivity.class);
            samplingIntent.putExtra("eventID", eventID);
            samplingIntent.putExtra("eventTitle", eventTitle);
            samplingIntent.putExtra("size", dataList.size());
            startActivity(samplingIntent);
        });

        viewResults.setOnClickListener(v -> {
            Intent resultsIntent = new Intent(WaitingListActivity.this, EventResultList.class);
            resultsIntent.putExtra("eventID", eventID);
            resultsIntent.putExtra("eventTitle", eventTitle);
            resultsIntent.putExtra("size", dataList.size());
            startActivity(resultsIntent);
        });
    }

    private void fetchWaitingList(String eventID, Button sampleButton, Button viewResults) {
        // Query the event document by eventID to get the waitingList array
        eventsRef.document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the waitingList array and alreadySampled from the document
                        List<String> waitingListIds = (List<String>) documentSnapshot.get("waitingList");
                        String alreadySampled = documentSnapshot.getString("alreadySampled");

                        if (alreadySampled != null) {
                            // Set setting_buttons based on alreadySampled value from Firestore
                            setting_buttons = Integer.parseInt(alreadySampled);

                            // Update button visibility based on setting_buttons
                            if (setting_buttons == 0) {
                                viewResults.setVisibility(View.GONE);  // Hide view results button
                                sampleButton.setVisibility(View.VISIBLE);   // Show sample button
                            } else if (setting_buttons == 1) {
                                viewResults.setVisibility(View.VISIBLE);  // Show view results button
                                sampleButton.setVisibility(View.GONE);    // Hide sample button
                            }
                        } else {
                            Toast.makeText(this, "Error: 'alreadySampled' is null.", Toast.LENGTH_SHORT).show();
                        }

                        // Display the value of alreadySampled using a Toast
                        Toast.makeText(this, "alreadySampled value: " + alreadySampled, Toast.LENGTH_SHORT).show();

                        if (waitingListIds != null && !waitingListIds.isEmpty()) {
                            // Populate dataList with deviceIDs from waitingList
                            for (String deviceID : waitingListIds) {
                                UserProfile userProfile = new UserProfile();
                                userProfile.setName(deviceID); // Set deviceID as a placeholder for name
                                dataList.add(userProfile);
                            }

                            // Notify the adapter to update the ListView
                            userAdapter.notifyDataSetChanged();
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
