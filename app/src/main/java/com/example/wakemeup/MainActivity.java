package com.example.wakemeup;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.wakemeup.alarm.AlarmFragment;
import com.example.wakemeup.stopwatch.Stopwatch;
import com.example.wakemeup.timer.Timer;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_navigation);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        Fragment alarmFragment = new AlarmFragment();
        Fragment stopwatchFragment = new Stopwatch();
        Fragment timerFragment = new Timer();

        setCurrentFragment(alarmFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.alarm) {
                setCurrentFragment(alarmFragment);
            } else if (itemId == R.id.stopwatch) {
                setCurrentFragment(stopwatchFragment);
            } else if (itemId == R.id.timer) {
                setCurrentFragment(timerFragment);
            }
            return true;
        });
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }
}
