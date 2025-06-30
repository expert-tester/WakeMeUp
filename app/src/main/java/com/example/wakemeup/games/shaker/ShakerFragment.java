package com.example.wakemeup.games.shaker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wakemeup.R;
import com.example.wakemeup.games.common.BaseGameFragment;

public class ShakerFragment extends BaseGameFragment implements SensorEventListener {
    private TextView shakesRemainingText;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float acceleration = 0f;
    private float currentAcceleration = 0f;
    private float lastAcceleration = 0f;

    private final int SHAKE_THRESHOLD = 12;
    private final int SHAKES_TO_WIN = 15;
    private int shakeCount = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shaker, container, false);

        shakesRemainingText = view.findViewById(R.id.shakesRemainingText);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer == null) {
            Toast.makeText(getContext(), "No accelerometer found!", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt((double) (x*x + y*y + z*z));
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;

        // shakey shakey
        if (acceleration > SHAKE_THRESHOLD) {
            Toast.makeText(getContext(), "Shake detected", Toast.LENGTH_SHORT).show();
            shakeCount++;
            updateShakesRemainingText();

            if (shakeCount >= SHAKES_TO_WIN) {
                completeGame();
            }
        }
    }

    private void updateShakesRemainingText() {
        String text = "Shakes Remaining: " + (SHAKES_TO_WIN - shakeCount);
        shakesRemainingText.setText(text);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }
    @Override
    public void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }
}
