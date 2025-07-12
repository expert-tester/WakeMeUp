package com.example.wakemeup.timer;

import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.example.wakemeup.timer.TimerFinishedFragment;

public class TimerCountdownFragment extends Fragment {

    private TextView timeText;
    private Button stopResumeButton, quitButton;
    private CountDownTimer timer;
    private boolean isRunning = true;
    private long millisRemaining;

    private long totalMillis;

    public TimerCountdownFragment(long totalMillis) {
        this.totalMillis = totalMillis;
        this.millisRemaining = totalMillis;
    }

    public static TimerCountdownFragment newInstance(long totalMillis) {
        return new TimerCountdownFragment(totalMillis);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer_countdown, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timeText = view.findViewById(R.id.timeText);
        stopResumeButton = view.findViewById(R.id.stopResumeButton);
        quitButton = view.findViewById(R.id.quitButton);

        startTimer(millisRemaining);

        stopResumeButton.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                resumeTimer();
            }
        });

        quitButton.setOnClickListener(v -> {
            if (timer != null) timer.cancel();
            ((MainActivity) requireActivity()).setCurrentFragment(new TimerPickerFragment());
        });
    }

    private void startTimer(long duration) {
        timer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisRemaining = millisUntilFinished;
                updateTimeText(millisRemaining);
            }

            @Override
            public void onFinish() {
                updateTimeText(0);
                isRunning = false;

                // pass the completed time to finish fragment
                String completedTime = formatTime(totalMillis);
                TimerFinishedFragment finishFragment = TimerFinishedFragment.newInstance(completedTime);
                ((MainActivity) requireActivity()).setCurrentFragment(finishFragment);
            }
        }.start();

        isRunning = true;
        stopResumeButton.setText("Stop");
    }

    private void pauseTimer() {
        if (timer != null) timer.cancel();
        isRunning = false;
        stopResumeButton.setText("Resume");
    }

    private void resumeTimer() {
        startTimer(millisRemaining);
        isRunning = true;
        stopResumeButton.setText("Stop");
    }

    private void updateTimeText(long millis) {
        timeText.setText(formatTime(millis));
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
