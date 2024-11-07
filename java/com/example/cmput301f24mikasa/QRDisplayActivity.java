package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class QRDisplayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);

        // Get the scanned data from the intent
        Intent intent = getIntent();
        String qrText = intent.getStringExtra("QR_TEXT");

        // Set the scanned data to the TextView
        TextView qrTextView = findViewById(R.id.qrTextView);
        qrTextView.setText(qrText);
    }
}

