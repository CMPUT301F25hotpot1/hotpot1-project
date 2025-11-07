package com.example.lottary.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lottary.R;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_TITLE = "extra_title";

    public static void launch(Context ctx, String url, @Nullable String title) {
        Intent i = new Intent(ctx, ImageViewerActivity.class);
        i.putExtra(EXTRA_URL, url);
        i.putExtra(EXTRA_TITLE, title);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String title = getIntent().getStringExtra(EXTRA_TITLE);

        TextView tv = findViewById(R.id.tvTitle);
        ImageView iv = findViewById(R.id.ivFull);

        tv.setText(title == null ? "" : title);

        Glide.with(this)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_report_image) // 用系统图标占位，免去自定义资源
                .into(iv);

        iv.setOnClickListener(v -> finish());
    }
}
