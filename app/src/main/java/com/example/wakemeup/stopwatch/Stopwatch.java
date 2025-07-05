package com.example.wakemeup.stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.wakemeup.R;

import java.util.*;

public class Stopwatch extends AppCompatActivity {

    private TextView timerTextView;
    private Button startStopButton, lapResetButton;
    private ListView lapList;
    private boolean running = false;
    private long startTime = 0L;
    private Handler handler = new Handler();
    private List<String> laps = new ArrayList<>();
    private Runnable updateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopwatch);

        timerTextView = findViewById(R.id.timer);
        startStopButton = findViewById(R.id.startStopButton);
        lapResetButton = findViewById(R.id.lapResetButton);
        lapList = findViewById(R.id.lapList);

        updateTimer = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int minutes = (int) (millis / 60000);
                int seconds = (int) (millis / 1000) % 60;
                int centiseconds = (int) (millis % 1000) / 10;
                timerTextView.setText(String.format("%02d:%02d.%02d", minutes, seconds, centiseconds));
                handler.postDelayed(this, 10);
            }
        };

        startStopButton.setOnClickListener(v -> {
            if (running) {
                handler.removeCallbacks(updateTimer);
                startStopButton.setText("Start");
                startStopButton.setBackgroundResource(R.drawable.circle_button_green);
                startStopButton.setTextColor(ContextCompat.getColor(this, R.color.text_color_on_green_button));

                lapResetButton.setText("Reset");
                running = false;
            } else {
                startTime = System.currentTimeMillis();
                handler.post(updateTimer);
                startStopButton.setText("Stop");
                startStopButton.setBackgroundResource(R.drawable.circle_button_red);
                startStopButton.setTextColor(ContextCompat.getColor(this, R.color.text_color_on_red_button));
                lapResetButton.setText("Lap");
                running = true;
            }
        });

        lapResetButton.setOnClickListener(v -> {
            if (running) {
                // Save lap
                String lapTime = timerTextView.getText().toString();
                laps.add(0, lapTime);
                updateLapList();
            } else {
                // Reset everything
                timerTextView.setText("00:00.00");
                laps.clear();
                updateLapList();
            }
        });
    }

    private void updateLapList() {
        List<Map<String, String>> data = new ArrayList<>();
        for (int i = 0; i < laps.size(); i++) {
            Map<String, String> item = new HashMap<>();
            item.put("lap", "Lap " + (laps.size() - i));
            item.put("time", laps.get(i));
            data.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.lap_item,
                new String[]{"lap", "time"},
                new int[]{R.id.lapLabel, R.id.lapTime}
        );
        lapList.setAdapter(adapter);
    }
}
