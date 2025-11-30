package com.example.lottary.ui.browse;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottary.R;
import com.example.lottary.data.Event;
import com.example.lottary.data.FirestoreEventRepository;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * QrScanActivity (actually: "Choose event and show its QR code").
 *
 * Organizer flow:
 *  - shows a list of events;
 *  - tapping one generates a deep-link QR code for that event;
 *  - another device can scan the QR and open EventDetailsActivity.
 */
public class QrScanActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ImageView qrImage;
    private TextView selectedTitle;
    private Button btnBack;

    private final List<Event> events = new ArrayList<>();
    private EventsAdapter adapter;
    @Nullable
    private ListenerRegistration reg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        recycler = findViewById(R.id.recycler_events);
        qrImage = findViewById(R.id.image_qr);
        selectedTitle = findViewById(R.id.text_selected_title);
        btnBack = findViewById(R.id.btn_back);

        selectedTitle.setText("No event selected yet");

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventsAdapter(events, this::onEventSelected);
        recycler.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        reg = FirestoreEventRepository.get().listenRecentCreated(items -> {
            events.clear();
            events.addAll(items);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }

    private void onEventSelected(@NonNull Event e) {
        selectedTitle.setText("QR code for: " + e.getTitle());

        // Encode deep-link payload instead of bare id.
        String payload = EventDetailsActivity.buildDeepLinkPayload(e.getId());
        Bitmap bmp = createQrBitmap(payload, 800);
        qrImage.setImageBitmap(bmp);
    }

    private Bitmap createQrBitmap(@NonNull String text, int sizePx) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx);
            Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < sizePx; x++) {
                for (int y = 0; y < sizePx; y++) {
                    bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            bmp.eraseColor(Color.WHITE);
            return bmp;
        }
    }

    // ---------------- RecyclerView adapter ----------------

    private static class EventViewHolder extends RecyclerView.ViewHolder {
        final TextView title;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
        }
    }

    private static class EventsAdapter extends RecyclerView.Adapter<EventViewHolder> {

        interface Listener {
            void onClick(@NonNull Event e);
        }

        private final List<Event> data;
        private final Listener listener;

        EventsAdapter(@NonNull List<Event> data, @NonNull Listener listener) {
            this.data = data;
            this.listener = listener;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_qr_event, parent, false);
            return new EventViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            Event e = data.get(position);
            holder.title.setText(e.getTitle());
            holder.itemView.setOnClickListener(v -> listener.onClick(e));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}

