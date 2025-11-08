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
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.lottary.R;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.Executor;

/**
 * QrScanActivity
 *
 * Purpose:
 * - CameraX + ML Kit based QR/Barcode scanner.
 * - When a barcode value is recognized, it is treated as an event id and
 *   {@link EventDetailsActivity} is launched with that id, then this activity finishes.
 *
 * Key points:
 * - Uses {@link LifecycleCameraController} to simplify CameraX setup (no direct provider/futures).
 * - Requests CAMERA permission at runtime if not granted.
 * - Runs an {@link ImageAnalysis.Analyzer} on the main executor and applies
 *   {@link ImageAnalysis#STRATEGY_KEEP_ONLY_LATEST} to avoid analysis backlog.
 * - Debounces multiple detections with a simple {@code handled} flag.
 */
public class QrScanActivity extends AppCompatActivity {

    /** Camera preview surface embedded in the layout. */
    private PreviewView preview;
    /** High-level CameraX controller bound to this Activity lifecycle. */
    private LifecycleCameraController controller;
    /** One-shot guard: prevents handling the same scan result multiple times. */
    private boolean handled = false;

    /** Activity Result API launcher for the CAMERA permission. */
    private final ActivityResultLauncher<String> perm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);
        preview = findViewById(R.id.preview);

        // Check camera permission; request if not yet granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            perm.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Initialize CameraX and the ML Kit analyzer.
     * - Back camera only.
     * - Image analysis only (no still capture).
     * - Keep-only-latest strategy to minimize latency on slower devices.
     * - Barcode scanner created with default options (all supported formats).
     */
    private void startCamera() {
        // 1) 创建 controller（代替 ProcessCameraProvider + ListenableFuture）
        controller = new LifecycleCameraController(this);
        controller.setCameraSelector(
                new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
        );
        controller.setImageAnalysisBackpressureStrategy(
                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
        );
        // 只需预览+分析
        controller.setEnabledUseCases(CameraController.IMAGE_ANALYSIS);

        // 2) 绑定到 PreviewView
        preview.setController(controller);
        controller.bindToLifecycle(this);

        // 3) 设置条码分析器（ML Kit）
        BarcodeScannerOptions opts = new BarcodeScannerOptions.Builder().build(); // default: all formats
        BarcodeScanner scanner = BarcodeScanning.getClient(opts);
        Executor main = ContextCompat.getMainExecutor(this);

        // Forward each frame to ML Kit; proxy must be closed in callbacks.
        controller.setImageAnalysisAnalyzer(main, image -> analyze(scanner, image));
    }

    /**
     * Analyze a single frame and attempt to decode barcodes.
     * - Closes the {@link ImageProxy} in all paths to avoid analyzer stalls.
     * - On first non-empty {@link Barcode#getRawValue()}:
     *   * Sets {@code handled = true} to debounce
     *   * Launches {@link EventDetailsActivity} with EXTRA_EVENT_ID
     *   * Finishes this activity
     */
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
                                handled = true; // debounce: only handle first successful scan
                                Intent i = new Intent(this, EventDetailsActivity.class);
                                i.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, v);
                                startActivity(i);
                                finish();
                                break;
                            }
                        }
                    })
                    // Always close the frame regardless of success/failure to keep analyzer flowing.
                    .addOnCompleteListener(t -> proxy.close());
        } catch (Exception e) {
            // Defensive: make sure to close the proxy even if unexpected errors occur.
            proxy.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear analyzer to release references promptly; unbinding is handled by lifecycle.
        if (controller != null) {
            controller.clearImageAnalysisAnalyzer();
            // controller.unbind();
        }
    }
}
