package com.example.cmput301f24mikasa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NotificationArrayAdapter extends ArrayAdapter<Notifications> {
    private final Context context;
    private final List<Notifications> notificationList;

    public NotificationArrayAdapter(Context context, List<Notifications> notificationList) {
        super(context, R.layout.activity_notification_item, notificationList);
        this.context = context;
        this.notificationList = notificationList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.activity_notification_item, parent, false);
        }

        TextView notificationTextView = rowView.findViewById(R.id.notification_text);
        Notifications notification = notificationList.get(position);
        notificationTextView.setText(notification.getNotificationText());

        return rowView;
    }
}