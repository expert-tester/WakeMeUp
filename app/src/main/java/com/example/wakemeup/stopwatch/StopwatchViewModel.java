package com.example.wakemeup.stopwatch;

import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class StopwatchViewModel extends ViewModel {

    private final MutableLiveData<String> _timerText = new MutableLiveData<>("00:00.00");
    public LiveData<String> timerText = _timerText;

    private final MutableLiveData<List<String>> _laps = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<String>> laps = _laps;

    private final MutableLiveData<Boolean> _isRunning = new MutableLiveData<>(false);
    public LiveData<Boolean> isRunning = _isRunning;

    private boolean running = false;
    private long startTime = 0L;
    private final Handler handler = new Handler();

    private long elapsedTimeSoFar = 0L;

    private final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int minutes = (int) (millis / 60000);
            int seconds = (int) (millis / 1000) % 60;
            int centiseconds = (int) (millis % 1000) / 10;

            _timerText.postValue(String.format("%02d:%02d.%02d", minutes, seconds, centiseconds));

            if (running) {
                handler.postDelayed(this, 10);
            }
        }
    };

    public void startStop() {
        if (running) {
            stop();
        } else {
            start();
        }
    }

    private void start() {
        startTime = System.currentTimeMillis() - elapsedTimeSoFar;
        running = true;
        _isRunning.setValue(true);
        handler.post(updateTimer);
    }

    private void stop() {
        running = false;
        elapsedTimeSoFar = System.currentTimeMillis() - startTime;
        _isRunning.setValue(false);
        handler.removeCallbacks(updateTimer);
    }

    public void lapOrReset() {
        if (running) {
            List<String> currentLaps = new ArrayList<>(_laps.getValue());
            currentLaps.add(0, _timerText.getValue());
            _laps.setValue(currentLaps);
        } else {
            reset();
        }
    }

    private void reset() {
        running = false;
        startTime = 0L;
        elapsedTimeSoFar = 0L;
        _isRunning.setValue(false);
        _timerText.setValue("00:00.00");
        _laps.setValue(new ArrayList<>());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeCallbacks(updateTimer);
    }
}
