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
 * QrScanActivity (现在实际作用是：选择一个活动并生成对应的二维码).
 *
 * Flow:
 * - When this activity is opened from Browse, it subscribes to Firestore and shows
 *   a list of events at the top.
 * - When the user taps an event, we generate a QR code whose *text content* is
 *   "lottary://event/<eventId>".
 * - Another device (either our in-app scanner or a generic QR app) can scan this
 *   QR code. Our app declares a deep link for this URI so it can open
 *   EventDetailsActivity for that event.
 * - A "Back" button at the bottom returns to the previous screen.
 */
public class QrScanActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ImageView qrImage;
    private TextView selectedTitle;
    private Button btnBack;

    /** In-memory list of events loaded from Firestore. */
    private final List<Event> events = new ArrayList<>();
    private EventsAdapter adapter;

    /** Firestore listener; removed in onDestroy() to avoid leaks. */
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

        // Initial label before any selection.
        selectedTitle.setText("No event selected yet");

        // --- RecyclerView setup ---
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventsAdapter(events, this::onEventSelected);
        recycler.setAdapter(adapter);

        // --- Back button ---
        btnBack.setOnClickListener(v -> finish());

        // --- Subscribe to Firestore events (reuses the same repository as BrowseListFragment) ---
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

    /**
     * Called when the user taps an event row in the list.
     * Generates a QR code for that event.
     *
     * IMPORTANT:
     * - The QR content is a deep-link style URI: "lottary://event/<eventId>".
     * - This allows generic QR apps to emit an ACTION_VIEW intent which our
     *   app can handle via an intent-filter on EventDetailsActivity.
     */
    private void onEventSelected(@NonNull Event e) {
        selectedTitle.setText("QR code for: " + e.getTitle());

        // 1) Get event id
        String eventId = e.getId();
        // 2) Encode as deep link text
        String value = "lottary://event/" + eventId;

        Bitmap bmp = createQrBitmap(value, 800);
        qrImage.setImageBitmap(bmp);
    }

    /**
     * Create a square QR code bitmap for the given text.
     */
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
            // Fallback: create a blank white bitmap so the app does not crash.
            Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
            bmp.eraseColor(Color.WHITE);
            return bmp;
        }
    }

    // ------------------------------------------------------------------------
    // RecyclerView adapter + view holder for the event list
    // ------------------------------------------------------------------------

    /**
     * ViewHolder for a simple row containing an event title.
     */
    private static class EventViewHolder extends RecyclerView.ViewHolder {
        final TextView title;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
        }
    }

    /**
     * Adapter that shows the list of events and forwards clicks to a listener.
     */
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
