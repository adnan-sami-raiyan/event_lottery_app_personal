package com.example.cmput301f24mikasa;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private PreviewView previewView;
    private final BarcodeScanner barcodeScanner = BarcodeScanning.getClient();
    private String scannedQRContent; // Variable to hold QR code content
    private FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firebase instance
    private long lastScanTime = 0; // Timestamp for the last scan
    private boolean hasScanned = false; // Flag to indicate if a scan has been processed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        previewView = findViewById(R.id.previewView);

        Button startScanningButton = findViewById(R.id.btnScanQR);
        startScanningButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, 1001);
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyzeImage);

                // Bind camera to lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(ImageProxy imageProxy) {
        @OptIn(markerClass = ExperimentalGetImage.class)
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage != null && System.currentTimeMillis() - lastScanTime >= 1000 && !hasScanned) { // Check if 1 second has passed and scan hasn't been processed
            lastScanTime = System.currentTimeMillis(); // Update the last scan time
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            barcodeScanner.process(image)
                    .addOnSuccessListener(this::processBarcodeResults)
                    .addOnFailureListener(e -> e.printStackTrace())
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close(); // Release the image if not processing
        }
    }

    private void processBarcodeResults(List<Barcode> barcodes) {
        for (Barcode barcode : barcodes) {
            String qrText = barcode.getRawValue();
            if (qrText != null && !hasScanned) {
                scannedQRContent = qrText; // Store the scanned QR code content in a variable
                hasScanned = true;
                Toast.makeText(this, "QR Code Scanned: " + scannedQRContent, Toast.LENGTH_SHORT).show();
                fetchEventDetailsAndNavigate();
                break; // Stop processing after finding a QR code
            }
        }
    }

    private void fetchEventDetailsAndNavigate() {
        db.collection("event").document(scannedQRContent).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Event details retrieved successfully
                        String eventId = documentSnapshot.getId();
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        String date = documentSnapshot.getString("date");
                        String price = documentSnapshot.getString("price");

                        // Pass the event details to the ViewEventActivity
                        Intent intent = new Intent(this, ViewEventActivity.class);
                        intent.putExtra("eventId", eventId);
                        intent.putExtra("title", title);
                        intent.putExtra("description", description);
                        intent.putExtra("date", date);
                        intent.putExtra("price", price);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
