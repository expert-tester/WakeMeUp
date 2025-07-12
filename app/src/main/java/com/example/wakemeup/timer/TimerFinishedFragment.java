package com.example.wakemeup.timer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wakemeup.MainActivity;
import com.example.wakemeup.R;

public class TimerFinishedFragment extends Fragment {

    private TextView finishedTimeText;
    private Button closeButton, repeatButton;

    private String completedTime = "00:00:00";

    public static TimerFinishedFragment newInstance(String completedTime) {
        TimerFinishedFragment fragment = new TimerFinishedFragment();
        Bundle args = new Bundle();
        args.putString("timeCompleted", completedTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer_finished, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        finishedTimeText = view.findViewById(R.id.finishedTimeText);
        closeButton = view.findViewById(R.id.closeButton);
        repeatButton = view.findViewById(R.id.repeatButton);

        if (getArguments() != null) {
            completedTime = getArguments().getString("timeCompleted", "00:00:00");
        }

        finishedTimeText.setText("Time Completed: " + completedTime);

        closeButton.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).setCurrentFragment(new TimerPickerFragment());
        });

        repeatButton.setOnClickListener(v -> {
            long millis = parseTimeToMillis(completedTime);
            ((MainActivity) requireActivity()).setCurrentFragment(TimerCountdownFragment.newInstance(millis));
        });
    }

    private long parseTimeToMillis(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return (hours * 3600 + minutes * 60 + seconds) * 1000L;
    }
}
