package com.example.wakemeup.timer;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wakemeup.R;

import java.lang.reflect.Field;

public class TimerPickerFragment extends Fragment {

    private NumberPicker hoursPicker, minutesPicker, secondsPicker;
    private Button startButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hoursPicker = view.findViewById(R.id.pickerHour);
        minutesPicker = view.findViewById(R.id.pickerMin);
        secondsPicker = view.findViewById(R.id.pickerSec);
        startButton = view.findViewById(R.id.startButton);

        setupNumberPickers();

        startButton.setOnClickListener(v -> {
            int totalMillis = (hoursPicker.getValue() * 3600 +
                    minutesPicker.getValue() * 60 +
                    secondsPicker.getValue()) * 1000;

            if (totalMillis > 0) {
                TimerCountdownFragment countdownFragment = TimerCountdownFragment.newInstance(totalMillis);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, countdownFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void setupNumberPickers() {
        // Hour picker
        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(23);
        setNumberPickerTextColor(hoursPicker, Color.WHITE);

        // Minute picker
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        setNumberPickerTextColor(minutesPicker, Color.WHITE);

        // Second picker
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);
        setNumberPickerTextColor(secondsPicker, Color.WHITE);
    }

    private void setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        try {
            @SuppressLint("SoonBlockedPrivateApi")
            Field selectorWheelPaintField = numberPicker.getClass()
                    .getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);

            Paint paint = (Paint) selectorWheelPaintField.get(numberPicker);
            paint.setColor(color);

            numberPicker.invalidate();

            // Also set color for EditText child views
            for (int i = 0; i < numberPicker.getChildCount(); i++) {
                View child = numberPicker.getChildAt(i);
                if (child instanceof android.widget.EditText) {
                    ((android.widget.EditText) child).setTextColor(color);
                }
            }

        } catch (Exception e) {
            Log.w("TimerPickerFragment", "Could not set NumberPicker text color", e);
        }
    }
}
