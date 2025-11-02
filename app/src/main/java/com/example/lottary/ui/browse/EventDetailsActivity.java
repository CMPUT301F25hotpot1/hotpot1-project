package com.example.lottary.ui.browse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lottary.R;

public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";

    public static void open(Context context, String eventId) {
        Intent i = new Intent(context, EventDetailsActivity.class);
        i.putExtra(EXTRA_EVENT_ID, eventId);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
    }
}
