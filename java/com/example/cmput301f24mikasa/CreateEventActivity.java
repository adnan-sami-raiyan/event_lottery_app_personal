package com.example.cmput301f24mikasa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class CreateEventActivity extends AppCompatActivity {

    ImageView imgEvent;
    Button btnUpload, btnGenerateQRCode, btnCreateEvent, btnCreatePoster;
    EditText editTextTitle, editTextDate, editTextPrice, editTextDesc, editTextLimitWaitingList;
    Boolean eventCreated = false;
    Boolean qrCodeGenerated = false;
    Uri imageUri;
    FirebaseFirestore db;
    StorageReference storageReference;
    ActivityResultLauncher<Intent> resultLauncher;
    CollectionReference eventRef;
    String eventID;
    CheckBox checkBoxLimitWaitingList;
    Boolean hasWaitingListLimit;
    Integer waitingListLimit;

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Initialize views
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        imgEvent = findViewById(R.id.imgEvent);
        btnUpload = findViewById(R.id.btnUpload);
        btnGenerateQRCode = findViewById(R.id.btnGenerateQRCode);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnCreatePoster = findViewById(R.id.btnCreatePoster);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDate = findViewById(R.id.editTextDate);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDesc = findViewById(R.id.editTextDesc);
        checkBoxLimitWaitingList = findViewById(R.id.checkBoxLimitWaitingList);
        editTextLimitWaitingList = findViewById(R.id.editTextLimitWaitingList);

        checkBoxLimitWaitingList.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextLimitWaitingList.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) editTextLimitWaitingList.setText("");
        });

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event");
        storageReference = FirebaseStorage.getInstance().getReference("event_images");

        registerResult();

        btnUpload.setOnClickListener(view -> pickImage());

        btnCreateEvent.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String date = editTextDate.getText().toString();
            String price = editTextPrice.getText().toString();
            String desc = editTextDesc.getText().toString();

            if (title.isEmpty() || date.isEmpty() || price.isEmpty() || desc.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!DATE_PATTERN.matcher(date).matches()) {
                Toast.makeText(CreateEventActivity.this, "Please enter the date in MM/DD/YYYY format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                createEvent(title, date, price, desc);
            } else {
                Toast.makeText(CreateEventActivity.this, "Please upload an image", Toast.LENGTH_SHORT).show();
            }
        });

        btnGenerateQRCode.setOnClickListener(v -> {
            if (eventCreated) {  // Ensure event is created before generating QR code
                qrCodeGenerated = true;
                Toast.makeText(CreateEventActivity.this, "QR Code successfully generated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CreateEventActivity.this, "Please create the event first.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreatePoster.setOnClickListener(v -> {
            if (eventCreated && qrCodeGenerated) {
                Bitmap qrCodeBitmap = generateQRCodeBitmap(eventID);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] qrCodeBytes = stream.toByteArray();

                Intent intent = new Intent(CreateEventActivity.this, EventPosterActivity.class);
                intent.putExtra("eventID", eventID);
                intent.putExtra("title", editTextTitle.getText().toString());
                intent.putExtra("startDate", editTextDate.getText().toString());
                intent.putExtra("desc", editTextDesc.getText().toString());
                intent.putExtra("price", editTextPrice.getText().toString());
                intent.putExtra("imageURL", imageUri.toString());
                intent.putExtra("qrCodeBytes", qrCodeBytes);

                startActivity(intent);
            } else {
                Toast.makeText(CreateEventActivity.this, "Please ensure event is created and QR code is generated.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imgEvent.setImageURI(imageUri);
                    } else {
                        Toast.makeText(CreateEventActivity.this, "Please select an image.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void createEvent(String title, String date, String price, String desc) {
        DocumentReference documentReference = eventRef.document();
        eventID = documentReference.getId();

        StorageReference fileRef = storageReference.child(eventID + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                waitingListLimit = 0;
                hasWaitingListLimit = checkBoxLimitWaitingList.isChecked();
                if (hasWaitingListLimit) {
                    String limitText = editTextLimitWaitingList.getText().toString();
                    if (!limitText.isEmpty()) {
                        waitingListLimit = Integer.parseInt(limitText);
                    }
                }

                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    HashMap<String, Object> eventDetails = new HashMap<>();
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    eventDetails.put("deviceID", deviceId);
                    eventDetails.put("title", title);
                    eventDetails.put("startDate", date);
                    eventDetails.put("price", price);
                    eventDetails.put("description", desc);
                    eventDetails.put("capacity", 1);
                    eventDetails.put("eventID", eventID);
                    eventDetails.put("imageURL", uri.toString());
                    eventDetails.put("cancelledEntrants", new ArrayList<>());
                    eventDetails.put("selectedEntrants", new ArrayList<>());
                    eventDetails.put("hasWaitingListLimit", hasWaitingListLimit);
                    eventDetails.put("waitingListLimit", waitingListLimit);
                    eventDetails.put("waitingList", new ArrayList<>()); // Add empty WaitingList array

                    documentReference.set(eventDetails).addOnSuccessListener(aVoid -> {
                        eventCreated = true;
                        Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e ->
                            Toast.makeText(CreateEventActivity.this, "Sorry, unable to create event.", Toast.LENGTH_SHORT).show()
                    );
                }).addOnFailureListener(e ->
                        Toast.makeText(CreateEventActivity.this, "Sorry, image upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                );
            }
        }).addOnFailureListener(e ->
                Toast.makeText(CreateEventActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show()
        );
    }

    public Bitmap generateQRCodeBitmap(String content) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}

/*
package com.example.cmput301f24mikasa;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateEventActivity extends AppCompatActivity {

    ImageView imgEvent;
    Button btnUpload, btnGenerateQRCode, btnCreateEvent;
    EditText editTextTitle, editTextDate, editTextPrice, editTextDesc;
    Boolean eventCreated;
    Uri imageUri;
    FirebaseFirestore db;
    StorageReference storageReference;
    ActivityResultLauncher<Intent> resultLauncher;
    CollectionReference eventRef;
    String eventID;  // Class-level variable to store the unique event ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Back button to return to Organizer Dashboard
        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        eventCreated = false;
        imgEvent = findViewById(R.id.imgEvent);
        btnUpload = findViewById(R.id.btnUpload);
        btnGenerateQRCode = findViewById(R.id.btnGenerateQRCode);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDate = findViewById(R.id.editTextDate);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextDesc = findViewById(R.id.editTextDesc);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event");
        storageReference = FirebaseStorage.getInstance().getReference("event_images");

        registerResult();
        btnUpload.setOnClickListener(view -> pickImage());

        btnCreateEvent.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String date = editTextDate.getText().toString();
            String price = editTextPrice.getText().toString();
            String desc = editTextDesc.getText().toString();

            if (title.isEmpty() || date.isEmpty() || price.isEmpty() || desc.isEmpty()) {
                Toast.makeText(CreateEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                createEvent(title, date, price, desc);
            } else {
                Toast.makeText(CreateEventActivity.this, "Please upload an image", Toast.LENGTH_SHORT).show();
            }
        });

        btnGenerateQRCode.setOnClickListener(v -> {
            if (eventID != null) {  // Ensure eventID is set before generating QR code
                Bitmap qrCode = generateQRCodeBitmap(eventID);
                eventRef.document(eventID).update("qrRef", eventID).addOnSuccessListener(aVoid ->
                        Toast.makeText(CreateEventActivity.this, "QR Code successfully generated", Toast.LENGTH_SHORT).show()
                ).addOnFailureListener(e ->
                        Toast.makeText(CreateEventActivity.this, "Sorry, something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
                );
            } else {
                Toast.makeText(CreateEventActivity.this, "Event has not been created yet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        resultLauncher.launch(intent);
    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            imgEvent.setImageURI(imageUri);
                        } else {
                            Toast.makeText(CreateEventActivity.this, "Please select an image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void createEvent(String title, String date, String price, String desc) {
        DocumentReference documentReference = eventRef.document();
        eventID = documentReference.getId();  // Generate and store eventID once

        StorageReference fileRef = storageReference.child(eventID + ".jpg");  // Use eventID for image storage
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    HashMap<String, Object> eventDetails = new HashMap<>();
                    @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    eventDetails.put("deviceID", deviceId);
                    eventDetails.put("title", title);
                    eventDetails.put("startDate", date);
                    eventDetails.put("price", price);
                    eventDetails.put("description", desc);
                    eventDetails.put("capacity", 1);
                    eventDetails.put("eventID", eventID);  // Save eventID in Firestore
                    eventDetails.put("imageURL", uri.toString());
                    eventDetails.put("cancelledEntrants", new ArrayList<>());
                    eventDetails.put("selectedEntrants", new ArrayList<>());

                    documentReference.set(eventDetails).addOnSuccessListener(aVoid ->
                            Toast.makeText(CreateEventActivity.this, "Event created successfully", Toast.LENGTH_SHORT).show()
                    ).addOnFailureListener(e ->
                            Toast.makeText(CreateEventActivity.this, "Sorry, unable to create event.", Toast.LENGTH_SHORT).show()
                    );
                }).addOnFailureListener(e ->
                        Toast.makeText(CreateEventActivity.this, "Sorry, image upload failed. Please try again.", Toast.LENGTH_SHORT).show()
                );
            }
        }).addOnFailureListener(e ->
                Toast.makeText(CreateEventActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show()
        );
    }

    public Bitmap generateQRCodeBitmap(String content) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
*/