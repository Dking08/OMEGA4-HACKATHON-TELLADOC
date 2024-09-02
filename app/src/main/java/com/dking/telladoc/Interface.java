package com.dking.telladoc;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Interface extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private com.dking.telladoc.ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewPagerAdapter = new com.dking.telladoc.ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        // Attach the TabLayout with the ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Main");
                            break;
                        case 1:
                            tab.setText("Profile");
                            break;
                    }
                }).attach();
    }
}
