package com.example.lottary.ui.events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lottary.R;
import com.example.lottary.ui.events.create.CreateEventActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyEventsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private EditText inputSearch;
    private Button btnSearch, btnCreate;

    private final EventsListFragment joinedFragment  =
            EventsListFragment.newInstance(EventsListFragment.MODE_JOINED);
    private final EventsListFragment createdFragment =
            EventsListFragment.newInstance(EventsListFragment.MODE_CREATED);

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        tabLayout   = findViewById(R.id.tab_layout);
        viewPager   = findViewById(R.id.view_pager);
        inputSearch = findViewById(R.id.input_search);
        btnSearch   = findViewById(R.id.btn_search);
        btnCreate   = findViewById(R.id.btn_create_event);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull @Override
            public androidx.fragment.app.Fragment createFragment(int position) {
                return position == 0 ? joinedFragment : createdFragment;
            }
            @Override public int getItemCount() { return 2; }
        });

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, pos) -> tab.setText(pos == 0 ? "Joined Events" : "Created Events")
        ).attach();

        btnSearch.setOnClickListener(v -> {
            String q = inputSearch.getText() == null ? "" : inputSearch.getText().toString();
            if (viewPager.getCurrentItem() == 0) joinedFragment.applyFilter(q);
            else createdFragment.applyFilter(q);
        });

        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(this, CreateEventActivity.class)));

        BottomNavigationView bottom = findViewById(R.id.bottom_nav);
        bottom.setSelectedItemId(R.id.nav_my_events);
    }
}
