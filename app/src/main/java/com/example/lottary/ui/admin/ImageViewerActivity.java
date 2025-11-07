package com.example.lottary.ui.admin;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ImageView img = new ImageView(this);
        setContentView(img);

        String url = getIntent().getStringExtra("url");
        Picasso.get().load(url).fit().centerInside().into(img);
    }
}
