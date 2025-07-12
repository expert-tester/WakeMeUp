package com.example.wakemeup.stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.wakemeup.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stopwatch extends Fragment {

    private TextView timerTextView;
    private Button startStopButton, lapResetButton;
    private ListView lapList;

    private boolean running = false;
    private long startTime = 0L;
    private long elapsedTimeSoFar = 0L;
    private final Handler handler = new Handler();

    private final List<String> laps = new ArrayList<>();

    private final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int minutes = (int) (millis / 60000);
            int seconds = (int) (millis / 1000) % 60;
            int centiseconds = (int) (millis % 1000) / 10;

            timerTextView.setText(String.format("%02d:%02d.%02d", minutes, seconds, centiseconds));

            if (running) {
                handler.postDelayed(this, 10);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stopwatch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timerTextView = view.findViewById(R.id.timer);
        startStopButton = view.findViewById(R.id.startStopButton);
        lapResetButton = view.findViewById(R.id.lapResetButton);
        lapList = view.findViewById(R.id.lapList);

        startStopButton.setOnClickListener(v -> startStop());
        lapResetButton.setOnClickListener(v -> lapOrReset());
    }

    private void startStop() {
        if (running) {
            stop();
        } else {
            start();
        }
        updateButtons();
    }

    private void start() {
        startTime = System.currentTimeMillis() - elapsedTimeSoFar;
        running = true;
        handler.post(updateTimer);
    }

    private void stop() {
        running = false;
        elapsedTimeSoFar = System.currentTimeMillis() - startTime;
        handler.removeCallbacks(updateTimer);
    }

    private void lapOrReset() {
        if (running) {
            laps.add(0, timerTextView.getText().toString());
        } else {
            reset();
        }
        updateLapList();
    }

    private void reset() {
        running = false;
        startTime = 0L;
        elapsedTimeSoFar = 0L;
        timerTextView.setText("00:00.00");
        laps.clear();
        handler.removeCallbacks(updateTimer);
        updateLapList();
        updateButtons();
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
                requireContext(),
                data,
                R.layout.lap_item,
                new String[]{"lap", "time"},
                new int[]{R.id.lapLabel, R.id.lapTime}
        );
        lapList.setAdapter(adapter);
    }

    private void updateButtons() {
        if (running) {
            startStopButton.setText("Stop");
            startStopButton.setBackgroundResource(R.drawable.circle_button_red);
            startStopButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_on_red_button));
            lapResetButton.setText("Lap");
        } else {
            startStopButton.setText("Start");
            startStopButton.setBackgroundResource(R.drawable.circle_button_green);
            startStopButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color_on_green_button));
            lapResetButton.setText("Reset");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateTimer);
        reset();
    }
}
