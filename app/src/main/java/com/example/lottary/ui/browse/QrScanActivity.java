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

public class QrScanActivity extends AppCompatActivity {

    private PreviewView preview;
    private LifecycleCameraController controller;
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
        BarcodeScannerOptions opts = new BarcodeScannerOptions.Builder().build();
        BarcodeScanner scanner = BarcodeScanning.getClient(opts);
        Executor main = ContextCompat.getMainExecutor(this);

        controller.setImageAnalysisAnalyzer(main, image -> analyze(scanner, image));
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
                    .addOnCompleteListener(t -> proxy.close());
        } catch (Exception e) {
            proxy.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.clearImageAnalysisAnalyzer();
            // controller.unbind(); // 可选，通常随生命周期自动处理
        }
    }
}
