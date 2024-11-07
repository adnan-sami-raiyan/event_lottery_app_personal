package com.example.cmput301f24mikasa;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class EventsAdapter extends BaseAdapter {

    private Context context;
    private List<QueryDocumentSnapshot> events;
    private FirebaseFirestore db;
    private String deviceId;

    public EventsAdapter(Context context, List<QueryDocumentSnapshot> events, String deviceId) {
        this.context = context;
        this.events = events;
        this.db = FirebaseFirestore.getInstance();
        this.deviceId = deviceId;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        }

        QueryDocumentSnapshot event = events.get(position);

        TextView txtEventTitle = convertView.findViewById(R.id.txtEventTitle);
        TextView txtEventDate = convertView.findViewById(R.id.txtEventDate);
        TextView txtEventPrice = convertView.findViewById(R.id.txtEventPrice);
        TextView txtEventDescription = convertView.findViewById(R.id.txtEventDescription);
        Button btnUnjoin = convertView.findViewById(R.id.btnUnjoin);

        txtEventTitle.setText(event.getString("title"));
        txtEventDate.setText(event.getString("startDate"));
        txtEventPrice.setText(event.getString("price"));
        txtEventDescription.setText(event.getString("description"));

        // Handle "Unjoin" button click
        btnUnjoin.setOnClickListener(v -> unjoinEvent(event.getId(), position));

        return convertView;
    }

    private void unjoinEvent(String eventId, int position) {
        DocumentReference eventRef = db.collection("event").document(eventId);

        eventRef.get().addOnSuccessListener(documentSnapshot -> {
            List<String> waitingList = (List<String>) documentSnapshot.get("waitingList");

            if (waitingList != null && waitingList.contains(deviceId)) {
                waitingList.remove(deviceId);

                eventRef.update("waitingList", waitingList)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Unjoined event successfully", Toast.LENGTH_SHORT).show();

                            // Remove the event from the local list and refresh the ListView
                            events.remove(position);
                            notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> Log.e("EventsAdapter", "Error unjoining event", e));
            } else {
                Toast.makeText(context, "You are not signed up for this event", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Log.e("EventsAdapter", "Error retrieving event", e));
    }
}
