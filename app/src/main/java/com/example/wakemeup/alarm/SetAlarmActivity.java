package com.example.wakemeup.alarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.wakemeup.R;

import java.util.ArrayList;

public class SetAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Button saveBtn, cancelBtn;
    private EditText labelInput;
    private SwitchCompat snoozeSwitch, gameSwitch;
    private TextView repeatValue, alarmTitleText;
    private String selectedRepeat = "Never";
    private int alarmId = -1;  // -1 means it's a new alarm



    @SuppressLint({"ScheduleExactAlarm", "MissingInflatedId"})
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        timePicker = findViewById(R.id.timePicker);
        styleTimePickerText(timePicker);
        saveBtn = findViewById(R.id.saveAlarmBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        labelInput = findViewById(R.id.labelInput);
        snoozeSwitch = findViewById(R.id.snoozeSwitch);
        gameSwitch = findViewById(R.id.gameSwitch);
        repeatValue = findViewById(R.id.repeatValue);
        alarmTitleText = findViewById(R.id.alarmTitleText);

        AlarmDBHelper dbHelper = new AlarmDBHelper(this); // move it here so it can be reused

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("alarmId")) {
            alarmId = intent.getIntExtra("alarmId", -1);
            Log.d("EditAlarm", "Attempting to edit alarm with ID: " + alarmId);

            if (alarmId != -1) {
                Alarm existingAlarm = dbHelper.getAlarmById(alarmId);
                if (existingAlarm != null) {
                    Log.d("EditAlarm", "Editing with id " + existingAlarm.getId());
                    Log.d("EditAlarm", "Editing with all " + existingAlarm);
                    Log.d("EditAlarm", "Editing with hour " + existingAlarm.getHour());
                    timePicker.setHour(existingAlarm.getHour());
                    timePicker.setMinute(existingAlarm.getMinute());
                    labelInput.setText(existingAlarm.getLabel());

                    selectedRepeat = existingAlarm.getRepeat();
                    repeatValue.setText(
                            selectedRepeat.equals("Never") ? "Never" : "Repeat on " + selectedRepeat
                    );

                    snoozeSwitch.setChecked(existingAlarm.isSnoozeEnabled());
                    gameSwitch.setChecked(existingAlarm.isGameEnabled());


                    TextView title = alarmTitleText;
                    if (title != null) {
                        title.setText("Edit Alarm");
                    }
                }
            }
        }

        // cancel add alarm
        cancelBtn.setOnClickListener(v -> finish());

//        // save alarm
//        saveBtn.setOnClickListener(v -> {
//            int hour = timePicker.getHour();
//            int minute = timePicker.getMinute();
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                if (!alarmManager.canScheduleExactAlarms()) {
//                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
//                    intent.setData(Uri.parse("package:" + getPackageName()));
//                    startActivity(intent);
//                    return;
//                }
//            }
//
//            String amPm = (hour >= 12) ? "PM" : "AM";
//            int displayHour = (hour % 12 == 0) ? 12 : hour % 12;
//            String alarmTime = String.format("%02d:%02d %s", displayHour, minute, amPm);
//
//            AlarmDBHelper dbHelper = new AlarmDBHelper(this);
//            long id = dbHelper.insertAlarm(
//                    alarmTime,
//                    hour,
//                    minute,
//                    true,
//                    labelInput.getText().toString(),
//                    selectedRepeat,
//                    gameSwitch.isChecked()
//            );
//
//            if (id != -1) {
//                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                Intent intent = new Intent(this, AlarmReceiver.class);
//                intent.putExtra("alarmId", (int) id);
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                        this,
//                        (int) id,
//                        intent,
//                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//                );
//
//                Calendar calendar = Calendar.getInstance();
//                calendar.set(Calendar.HOUR_OF_DAY, hour);
//                calendar.set(Calendar.MINUTE, minute);
//                calendar.set(Calendar.SECOND, 0);
//                calendar.set(Calendar.MILLISECOND, 0);
//
//                // If the time is in the past, schedule for the next day
//                if (calendar.before(Calendar.getInstance())) {
//                    calendar.add(Calendar.DAY_OF_YEAR, 1);
//                }
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    alarmManager.setExactAndAllowWhileIdle(
//                            AlarmManager.RTC_WAKEUP,
//                            calendar.getTimeInMillis(),
//                            pendingIntent
//                    );
//                } else {
//                    alarmManager.setExact(
//                            AlarmManager.RTC_WAKEUP,
//                            calendar.getTimeInMillis(),
//                            pendingIntent
//                    );
//                }
//
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("alarmData", (int) id); // send alarmId
//                resultIntent.putExtra("hour", hour);
//                resultIntent.putExtra("minute", minute);
//                setResult(RESULT_OK, resultIntent);
//                Toast.makeText(this, "Alarm saved", Toast.LENGTH_SHORT).show();
//
//                finish();
//            } else {
//                Toast.makeText(this, "Failed to save alarm", Toast.LENGTH_SHORT).show();
//            }
//        });
        saveBtn.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    permissionIntent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(permissionIntent);
                    Toast.makeText(this, "Please grant permission to set exact alarms", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            String amPm = (hour >= 12) ? "PM" : "AM";
            int displayHour = (hour % 12 == 0) ? 12 : hour % 12;
            String alarmTime = String.format("%02d:%02d %s", displayHour, minute, amPm);

            int alarmId = getIntent().getIntExtra("alarmId", -1); // 👈 check if we're editing

            long id;
            if (alarmId != -1) {
                // Editing existing alarm
                dbHelper.updateAlarm(alarmId, alarmTime, hour, minute,
                        true,
                        labelInput.getText().toString(),
                        selectedRepeat,
                        snoozeSwitch.isChecked(),
                        gameSwitch.isChecked());
                id = alarmId;
            } else {
                // Adding new alarm
                id = dbHelper.insertAlarm(
                        alarmTime,
                        hour,
                        minute,
                        true,
                        labelInput.getText().toString(),
                        selectedRepeat,
                        snoozeSwitch.isChecked(),
                        gameSwitch.isChecked()
                );
            }

            if (id != -1) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent alarmIntent = new Intent(this, AlarmReceiver.class);
                alarmIntent.putExtra("alarmId", (int) id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        (int) id,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Intent resultIntent = new Intent();
                resultIntent.putExtra("alarmData", (int) id);
                resultIntent.putExtra("hour", hour);
                resultIntent.putExtra("minute", minute);
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(this, "Alarm saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save alarm", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.repeatLayout).setOnClickListener(v -> {
            Toast.makeText(this, "Repeat picker not implemented", Toast.LENGTH_SHORT).show();
        });

        // repeat
        LinearLayout repeatLayout = findViewById(R.id.repeatLayout);

        repeatLayout.setOnClickListener(v -> {
            ArrayList<String> selectedDays = new ArrayList<>();
            String[] days = {"Never", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            boolean[] checkedDays = new boolean[8];


            AlertDialog.Builder builder = new AlertDialog.Builder(SetAlarmActivity.this);
            builder.setTitle("Select Repeat Days");

            builder.setMultiChoiceItems(days, checkedDays, (dialog, which, isChecked) -> {
                if (isChecked) {
                    selectedDays.add(days[which]);
                } else {
                    selectedDays.remove(days[which]);
                }
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                if (selectedDays.isEmpty()) {
                    selectedRepeat = "Never";
                    repeatValue.setText("Never");

                } else {
                    selectedRepeat = String.join(", ", selectedDays);
                    repeatValue.setText("Repeat on " + selectedRepeat);

//                    StringBuilder repeatBuilder = new StringBuilder();
//                    for (int i = 0; i < selectedDays.size(); i++) {
//                        repeatBuilder.append(selectedDays.get(i));
//                        if (i < selectedDays.size() - 1) repeatBuilder.append(", ");
//                    }
//                    selectedRepeat = repeatBuilder.toString();
//
//                    // Format for display
//                    if (selectedDays.size() == 1) {
//                        repeatValue.setText("Repeat every " + selectedDays.get(0));
//                    } else if (selectedDays.size() == 7) {
//                        repeatValue.setText("Every day");
//                    } else {
//                        repeatValue.setText("Repeat on " + selectedRepeat);
//                    }
                }

                // Save to DB if alarm already created
                if (getIntent().hasExtra("alarmId")) {
                    int existingId = getIntent().getIntExtra("alarmId", -1);
                    if (existingId != -1) {
                        dbHelper.updateRepeat(existingId, selectedRepeat);
                    }
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

    }

    private void styleTimePickerText(TimePicker timePicker) {
        try {
            Resources sysRes = Resources.getSystem();
            int hourId = sysRes.getIdentifier("hour", "id", "android");
            int minuteId = sysRes.getIdentifier("minute", "id", "android");
            int amPmId = sysRes.getIdentifier("amPm", "id", "android");

            NumberPicker hour = timePicker.findViewById(hourId);
            NumberPicker minute = timePicker.findViewById(minuteId);
            NumberPicker amPm = timePicker.findViewById(amPmId);

            stylePicker(hour);
            stylePicker(minute);
            stylePicker(amPm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stylePicker(NumberPicker picker) {
        if (picker == null) return;
        for (int i = 0; i < picker.getChildCount(); i++) {
            View child = picker.getChildAt(i);
            if (child instanceof EditText) {
                EditText editText = (EditText) child;
                editText.setTextColor(Color.GRAY); // grey
                editText.setTypeface(null, Typeface.BOLD); // bold
            }
        }
    }

}

