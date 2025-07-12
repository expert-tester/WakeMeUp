package com.example.wakemeup.timer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wakemeup.R;

import java.lang.reflect.Field;

public class Timer extends Fragment {

    private enum TimerState {
        STOPPED,
        RUNNING,
        PAUSED
    }

    private NumberPicker pickerHour, pickerMin, pickerSec;
    private TextView timerText;
    private Button startButton, resetButton;
    private CountDownTimer countDownTimer;
    private TimerState currentTimerState = TimerState.STOPPED;
    private long timeInMillis;
    private long timeLeftInMillis;
    private LinearLayout pickerContainer;
    private Ringtone ringtone;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pickerHour = view.findViewById(R.id.pickerHour);
        pickerMin = view.findViewById(R.id.pickerMin);
        pickerSec = view.findViewById(R.id.pickerSec);
        timerText = view.findViewById(R.id.timerText);
        startButton = view.findViewById(R.id.startButton);
        resetButton = view.findViewById(R.id.resetButton);
        pickerContainer = view.findViewById(R.id.pickerContainer);

        setupNumberPickers();

        startButton.setOnClickListener(v -> {
            switch (currentTimerState) {
                case STOPPED:
                    startTimer();
                    break;
                case RUNNING:
                    pauseTimer();
                    break;
                case PAUSED:
                    resumeTimer();
                    break;
            }
        });

        resetButton.setOnClickListener(v -> resetTimer());
        updateUI();
    }

    private void setupNumberPickers() {
        pickerHour.setMinValue(0);
        pickerHour.setMaxValue(23);
        setNumberPickerTextColor(pickerHour, Color.WHITE);

        pickerMin.setMinValue(0);
        pickerMin.setMaxValue(59);
        setNumberPickerTextColor(pickerMin, Color.WHITE);

        pickerSec.setMinValue(0);
        pickerSec.setMaxValue(59);
        setNumberPickerTextColor(pickerSec, Color.WHITE);
    }

    private void startTimer() {
        int hour = pickerHour.getValue();
        int min = pickerMin.getValue();
        int sec = pickerSec.getValue();
        timeInMillis = (hour * 3600 + min * 60 + sec) * 1000L;

        if (timeInMillis <= 0) {
            showAlert("Please set a valid time!");
            return;
        }

        timeLeftInMillis = timeInMillis;
        startCountdown(timeLeftInMillis);
        currentTimerState = TimerState.RUNNING;
        updateUI();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        currentTimerState = TimerState.PAUSED;
        updateUI();
    }

    private void resumeTimer() {
        startCountdown(timeLeftInMillis);
        currentTimerState = TimerState.RUNNING;
        updateUI();
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopRingtone(); // Stop ringtone if playing
        currentTimerState = TimerState.STOPPED;
        pickerHour.setValue(0);
        pickerMin.setValue(0);
        pickerSec.setValue(0);
        updateUI();
    }

    private void startCountdown(long millis) {
        countDownTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay(timeLeftInMillis);
            }

            public void onFinish() {
                timeLeftInMillis = 0;
                updateTimerDisplay(0);
                currentTimerState = TimerState.STOPPED;
                updateUI();
                playRingtone();
                showTimerFinishedDialog();
            }
        }.start();
    }

    private void updateUI() {
        switch (currentTimerState) {
            case STOPPED:
                pickerContainer.setVisibility(View.VISIBLE);
                timerText.setVisibility(View.INVISIBLE);
                startButton.setText("Start");
                startButton.setBackgroundResource(R.drawable.circle_button_green);
                startButton.setTextColor(Color.parseColor("#5fab72"));
                resetButton.setVisibility(View.VISIBLE);
                break;
            case RUNNING:
                pickerContainer.setVisibility(View.GONE);
                timerText.setVisibility(View.VISIBLE);
                startButton.setText("Stop");
                startButton.setBackgroundResource(R.drawable.circle_button_red); // Set to red
                startButton.setTextColor(Color.parseColor("#F87171")); // Set to light red
                resetButton.setVisibility(View.VISIBLE);
                break;
            case PAUSED:
                pickerContainer.setVisibility(View.GONE);
                timerText.setVisibility(View.VISIBLE);
                startButton.setText("Resume");
                startButton.setBackgroundResource(R.drawable.circle_button_green);
                startButton.setTextColor(Color.parseColor("#5fab72"));
                resetButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateTimerDisplay(long millis) {
        long totalSeconds = millis / 1000;
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        timerText.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void playRingtone() {
        try {
            Uri defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (defaultAlarmUri == null) {
                defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(requireContext(), defaultAlarmUri);
            if (ringtone != null) {
                ringtone.play();
            } else {
                Log.e("TimerFragment", "Failed to get default ringtone");
            }
        } catch (Exception e) {
            Log.e("TimerFragment", "Error playing alarm sound", e);
        }
    }

    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop the ringtone if the user leaves the fragment
        stopRingtone();
    }

    private void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        try {
            @SuppressLint("SoonBlockedPrivateApi") Field selectorWheelPaintField = numberPicker.getClass()
                    .getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
            numberPicker.invalidate();
        } catch (Exception e) {
            Log.w("NumberPicker", "Could not set text color", e);
        }
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Timer")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showTimerFinishedDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Timer")
                .setMessage("Timer finished!")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    stopRingtone();
                })
                .show();
    }
}