package com.example.timer1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class timer extends AppCompatActivity {

    private NumberPicker pickerHour, pickerMin, pickerSec;
    private TextView timerText;
    private Button startButton, resetButton;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long timeInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer);

        pickerHour = findViewById(R.id.pickerHour);
        pickerMin = findViewById(R.id.pickerMin);
        pickerSec = findViewById(R.id.pickerSec);
        timerText = findViewById(R.id.timerText);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);

        // Setup pickers
        pickerHour.setMinValue(0);
        pickerHour.setMaxValue(23);

        pickerMin.setMinValue(0);
        pickerMin.setMaxValue(59);

        pickerSec.setMinValue(0);
        pickerSec.setMaxValue(59);

        startButton.setOnClickListener(v -> {
            if (isRunning) {
                stopTimer();
            } else {
                int hour = pickerHour.getValue();
                int min = pickerMin.getValue();
                int sec = pickerSec.getValue();
                timeInMillis = (hour * 3600 + min * 60 + sec) * 1000L;

                if (timeInMillis == 0) {
                    showAlert("Please set a valid time!");
                    return;
                }

                startTimer(timeInMillis);
            }
        });

        resetButton.setOnClickListener(v -> resetTimer());
    }

    private void startTimer(long millis) {
        isRunning = true;
        startButton.setText("Stop");

        // Disable pickers while running
        setPickersEnabled(false);

        countDownTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                updateTimerDisplay(millisUntilFinished);
            }

            public void onFinish() {
                isRunning = false;
                startButton.setText("Start");
                updateTimerDisplay(0);
                setPickersEnabled(true);
                showAlert("Time is up!");
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        startButton.setText("Start");
        setPickersEnabled(true);
    }

    private void resetTimer() {
        stopTimer();
        updateTimerDisplay(0);
        pickerHour.setValue(0);
        pickerMin.setValue(0);
        pickerSec.setValue(0);
    }

    private void updateTimerDisplay(long millis) {
        int totalSeconds = (int) (millis / 1000);
        int h = totalSeconds / 3600;
        int m = (totalSeconds % 3600) / 60;
        int s = totalSeconds % 60;
        timerText.setText(String.format("%02d:%02d:%02d", h, m, s));
    }

    private void setPickersEnabled(boolean enabled) {
        pickerHour.setEnabled(enabled);
        pickerMin.setEnabled(enabled);
        pickerSec.setEnabled(enabled);
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Timer")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
