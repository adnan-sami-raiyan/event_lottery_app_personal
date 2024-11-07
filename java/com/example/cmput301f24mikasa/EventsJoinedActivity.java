package com.example.cmput301f24mikasa;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventsJoinedActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String deviceId;
    private List<QueryDocumentSnapshot> eventsJoined; // List to store matched event snapshots
    private EventsAdapter adapter; // Custom adapter to display detailed events

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_signed_up_for);

        db = FirebaseFirestore.getInstance();
        deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        eventsJoined = new ArrayList<>();

        ListView listView = findViewById(R.id.list_events_joined);
        adapter = new EventsAdapter(this, eventsJoined, deviceId); // Pass deviceId
        listView.setAdapter(adapter);

        fetchEventsSignedUpFor();
    }

    private void fetchEventsSignedUpFor() {
        db.collection("event").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> waitingList = (List<String>) document.get("waitingList");

                        if (waitingList != null && waitingList.contains(deviceId)) {
                            eventsJoined.add(document);
                        }
                    }

                    if (eventsJoined.isEmpty()) {
                        findViewById(R.id.no_events_text).setVisibility(View.VISIBLE);
                        Toast.makeText(this, "No events found for this device", Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.notifyDataSetChanged(); // Refresh ListView with found events
                    }
                })
                .addOnFailureListener(e -> Log.e("EventsSignedUpFor", "Error fetching events", e));
    }
}

