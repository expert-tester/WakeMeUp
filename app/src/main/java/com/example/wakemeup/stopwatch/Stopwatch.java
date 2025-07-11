package com.example.wakemeup.stopwatch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.wakemeup.R;

import java.util.*;

public class Stopwatch extends Fragment {

    private StopwatchViewModel viewModel;

    private TextView timerTextView;
    private Button startStopButton, lapResetButton;
    private ListView lapList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stopwatch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize views
        timerTextView = view.findViewById(R.id.timer);
        startStopButton = view.findViewById(R.id.startStopButton);
        lapResetButton = view.findViewById(R.id.lapResetButton);
        lapList = view.findViewById(R.id.lapList);

        // get ViewModel
        viewModel = new ViewModelProvider(this).get(StopwatchViewModel.class);

        // observe timer text
        viewModel.timerText.observe(getViewLifecycleOwner(), time -> {
            timerTextView.setText(time);
        });

        // observe laps
        viewModel.laps.observe(getViewLifecycleOwner(), laps -> {
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
        });

        // observe isRunning
        viewModel.isRunning.observe(getViewLifecycleOwner(), isRunning -> {
            if (isRunning) {
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
        });

        // start/stop button
        startStopButton.setOnClickListener(v -> {
            viewModel.startStop();
        });

        // lap/reset button
        lapResetButton.setOnClickListener(v -> {
            viewModel.lapOrReset();
        });
    }
}
