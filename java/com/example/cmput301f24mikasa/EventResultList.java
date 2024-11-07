package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
//when picking a replacement, notify them as well
//change sp tjat you use a proper getter for device id not name lol
//toast message for cancelled usaer
//cancelled = amount of times I can press pick new user
public class EventResultList extends AppCompatActivity {
    ArrayList<UserProfile> selectedEntrants;
    ArrayList<UserProfile> canceledEntrants;
    private String eventTitle;
    // List for canceled entrants
    private ListView selectedEntrantsListView;             // ListView for selected entrants
    private ListView canceledEntrantsListView;             // ListView for canceled entrants
    private UserProfileArrayAdapter selectedEntrantsAdapter; // Adapter for selected entrants
    private UserProfileArrayAdapter canceledEntrantsAdapter; // Adapter for canceled entrants
    private Button notifyButton, backButton, pickNewUserButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventsRef = db.collection("event");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_result_list);
        Intent intent = getIntent();
        selectedEntrants=new ArrayList<>();
        canceledEntrants=new ArrayList<>();
        // Fetch the event ID from the intent
        String eventID = intent.getStringExtra("eventID");
        String eventTitle = intent.getStringExtra("eventTitle");

        if (eventID != null) {
            // Fetch the selected and canceled entrants for this event
            //fetchEventResultData(eventID);
            Toast.makeText(this, "TITLE! "+ eventTitle, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Event ID not found!", Toast.LENGTH_SHORT).show();
        }
        fetchSelectedList(eventID);
        //set the name of the result list
        selectedEntrantsListView = findViewById(R.id.selected_entrants_list_view);
        canceledEntrantsListView = findViewById(R.id.canceled_entrants_list_view);

        selectedEntrantsAdapter = new UserProfileArrayAdapter(this, selectedEntrants);
        canceledEntrantsAdapter = new UserProfileArrayAdapter(this, canceledEntrants);

        selectedEntrantsListView.setAdapter(selectedEntrantsAdapter);
        canceledEntrantsListView.setAdapter(canceledEntrantsAdapter);


        pickNewUserButton = findViewById(R.id.picker_button);
        //have to notify those waitinglist that they have not been chosen
        notifyButton = findViewById(R.id.notify_the_selected);
        //Cancelled users ie when they dont accept will automatically recieve
        //a you canceled notification when they say no
        backButton = findViewById(R.id.go_back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the input field and show a toast message
                Intent intent = new Intent(EventResultList.this, WaitingListActivity.class);
                intent.putExtra("eventID", eventID);
                intent.putExtra("eventTitle", eventTitle);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the input field and show a toast message
                Intent intent = new Intent(EventResultList.this, OrganizerSendsNotification.class);
                intent.putExtra("eventID", eventID);
                intent.putExtra("eventTitle", eventTitle);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void fetchSelectedList(String eventID) {
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

                            // Notify the adapter to update the ListView
                            selectedEntrantsAdapter.notifyDataSetChanged();
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