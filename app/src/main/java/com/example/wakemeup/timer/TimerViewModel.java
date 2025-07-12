package com.example.wakemeup;

import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TimerViewModel extends ViewModel {
    private final MutableLiveData<Long> timeLeft = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> running = new MutableLiveData<>(false);
    private CountDownTimer timer;
    private long totalDuration;

    public LiveData<Long> getTimeLeft() { return timeLeft; }
    public LiveData<Boolean> isRunning() { return running; }

    public void start(long millis) {
        totalDuration = millis;
        running.setValue(true);
        timer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                running.setValue(false);
                timeLeft.setValue(0L);
            }
        }.start();
    }

    public void stop() {
        if (timer != null) timer.cancel();
        running.setValue(false);
    }

    public void resume() {
        start(timeLeft.getValue());
    }

    public long getTotalDuration() { return totalDuration; }

    public void reset() {
        stop();
        timeLeft.setValue(0L);
    }
}
