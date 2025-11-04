package com.example.lottary.ui.events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lottary.R;
import com.example.lottary.ui.browse.BrowseActivity;
import com.example.lottary.ui.events.create.CreateEventActivity;
import com.example.lottary.ui.notifications.NotificationsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyEventsActivity extends AppCompatActivity {

    private Button btnSearch, btnCreate;
    private EditText inputSearch;

    private final EventsListFragment joinedFragment  = EventsListFragment.newInstance(false);
    private final EventsListFragment createdFragment = EventsListFragment.newInstance(true);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        inputSearch = findViewById(R.id.input_search);
        btnSearch   = findViewById(R.id.btn_search);
        btnCreate   = findViewById(R.id.btn_create);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override public int getItemCount() { return 2; }
            @Override public Fragment createFragment(int position) {
                return position == 0 ? joinedFragment : createdFragment;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Joined Events" : "Created Events")
        ).attach();

        btnSearch.setOnClickListener(v -> {
            String q = inputSearch.getText() == null ? "" : inputSearch.getText().toString();
            joinedFragment.applyFilter(q);
            createdFragment.applyFilter(q);
        });

        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(this, CreateEventActivity.class)));

        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_my_events);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_browse) {
                Intent i = new Intent(this, BrowseActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_my_events) {
                return true; // already here
            } else if (id == R.id.nav_notifications) {
                Intent i = new Intent(this, NotificationsActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_my_events);
    }
}
