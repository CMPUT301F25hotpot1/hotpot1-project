package com.example.lottary.ui.browse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.lottary.R;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.Executor;

public class QrScanActivity extends AppCompatActivity {
    private PreviewView preview;
    private boolean handled = false;

    private final ActivityResultLauncher<String> perm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);
        preview = findViewById(R.id.preview);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            perm.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        Executor main = ContextCompat.getMainExecutor(this);
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider provider = ProcessCameraProvider.getInstance(this).get();

                Preview p = new Preview.Builder().build();
                p.setSurfaceProvider(preview.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder().build();
                BarcodeScannerOptions opts = new BarcodeScannerOptions.Builder().build();
                BarcodeScanner scanner = BarcodeScanning.getClient(opts);
                analysis.setAnalyzer(main, image -> analyze(scanner, image));

                provider.unbindAll();
                provider.bindToLifecycle(
                        this,
                        new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build(),
                        p, analysis
                );
            } catch (Exception ignored) {}
        }, main);
    }

    private void analyze(BarcodeScanner scanner, ImageProxy proxy) {
        if (handled) { proxy.close(); return; }
        try {
            if (proxy.getImage() == null) { proxy.close(); return; }
            InputImage img = InputImage.fromMediaImage(
                    proxy.getImage(), proxy.getImageInfo().getRotationDegrees());
            scanner.process(img)
                    .addOnSuccessListener(barcodes -> {
                        if (handled) return;
                        for (Barcode b : barcodes) {
                            String v = b.getRawValue();
                            if (v != null && !v.isEmpty()) {
                                handled = true;
                                Intent i = new Intent(this, EventDetailsActivity.class);
                                i.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, v);
                                startActivity(i);
                                finish();
                                break;
                            }
                        }
                    })
                    .addOnCompleteListener(r -> proxy.close());
        } catch (Exception e) {
            proxy.close();
        }
    }
}



