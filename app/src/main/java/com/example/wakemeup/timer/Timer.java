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
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wakemeup.R;

import java.lang.reflect.Field;

public class Timer extends Fragment {

    private NumberPicker pickerHour, pickerMin, pickerSec;
    private TextView timerText;
    private Button startButton, resetButton;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long timeInMillis;
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

        // Initialize views
        pickerHour = view.findViewById(R.id.pickerHour);
        pickerMin = view.findViewById(R.id.pickerMin);
        pickerSec = view.findViewById(R.id.pickerSec);
        timerText = view.findViewById(R.id.timerText);
        startButton = view.findViewById(R.id.startButton);
        resetButton = view.findViewById(R.id.resetButton);
        pickerContainer = view.findViewById(R.id.pickerContainer);

        // Hide timer initially
        timerText.setVisibility(View.INVISIBLE);

        // Setup number pickers with white text
        setupNumberPickers();

        startButton.setOnClickListener(v -> {
            if (isRunning) {
                stopTimer();
            } else {
                startTimer();
            }
        });

        resetButton.setOnClickListener(v -> resetTimer());
    }

    private void setupNumberPickers() {
        // Hour picker
        pickerHour.setMinValue(0);
        pickerHour.setMaxValue(23);
        setNumberPickerTextColor(pickerHour, Color.WHITE);

        // Minute picker
        pickerMin.setMinValue(0);
        pickerMin.setMaxValue(59);
        setNumberPickerTextColor(pickerMin, Color.WHITE);

        // Second picker
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

        // Show timer and hide pickers
        timerText.setVisibility(View.VISIBLE);
        pickerContainer.setVisibility(View.GONE);
        isRunning = true;
        startButton.setText("STOP");

        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                updateTimerDisplay(millisUntilFinished);
            }

            public void onFinish() {
                timerText.setText("00:00:00");
                isRunning = false;
                startButton.setText("START");

                // Play default alarm sound
                try {
                    Uri defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    if (defaultAlarmUri == null) {
                        defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    }
                    ringtone = RingtoneManager.getRingtone(requireContext(), defaultAlarmUri);
                    if (ringtone != null) {
                        ringtone.play();
                    } else {
                        Log.e("AlarmRingActivity", "Failed to get default ringtone");
                    }
                } catch (Exception e) {
                    Log.e("AlarmRingActivity", "Error playing alarm sound: " + e.getMessage(), e);
                }
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        startButton.setText("START");
        pickerContainer.setVisibility(View.VISIBLE);
        timerText.setVisibility(View.INVISIBLE);
    }

    private void resetTimer() {
        stopTimer();
        pickerHour.setValue(0);
        pickerMin.setValue(0);
        pickerSec.setValue(0);
    }

    private void updateTimerDisplay(long millis) {
        long totalSeconds = millis / 1000;
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        timerText.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
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
}