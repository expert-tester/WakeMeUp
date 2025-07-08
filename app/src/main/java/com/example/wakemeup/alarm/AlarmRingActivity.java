package com.example.wakemeup.alarm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.util.Calendar;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.wakemeup.R;
import com.example.wakemeup.games.GameHostActivity;

public class AlarmRingActivity extends Activity {

    private Ringtone ringtone;
    private int alarmId;
    private String label;
    private String time;

    private final BroadcastReceiver gamesCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (GameHostActivity.GAMES_COMPLETED.equals(intent.getAction())) {
                stopRingtone();
                finish();
            }
        }
    };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_ring);

        TextView labelTextView = findViewById(R.id.ringLabelText);
        TextView timeTextView = findViewById(R.id.ringTimeText);
        Button stopButton = findViewById(R.id.stopButton);
        Button snoozeButton = findViewById(R.id.snoozeButton);

        alarmId = getIntent().getIntExtra("alarmId", -1);
        label = getIntent().getStringExtra("label");
        time = getIntent().getStringExtra("time");

        labelTextView.setText(label != null ? label : "Alarm");
        timeTextView.setText(time != null ? time : "--:--");

        // Register BroadcastReceiver
        registerReceiver(gamesCompletedReceiver, new IntentFilter(GameHostActivity.GAMES_COMPLETED), RECEIVER_EXPORTED);

        // Play default alarm sound
        try {
            Uri defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (defaultAlarmUri == null) {
                defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(this, defaultAlarmUri);
            if (ringtone != null) {
                ringtone.play();
            } else {
                Log.e("AlarmRingActivity", "Failed to get default ringtone");
            }
        } catch (Exception e) {
            Log.e("AlarmRingActivity", "Error playing alarm sound: " + e.getMessage(), e);
        }
        // Load alarm from database to check if snooze is enabled
        AlarmDBHelper dbHelper = new AlarmDBHelper(this);
        Alarm alarm = dbHelper.getAlarmById(alarmId);

        if (alarm != null && !alarm.isSnoozeEnabled()) {
            snoozeButton.setVisibility(View.GONE); // Hide snooze button when is disabled
        }
        // Stop alarm on button click
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert alarm != null;
                if (alarm.isGameEnabled()) {
                    Intent gameIntent = new Intent(AlarmRingActivity.this, GameHostActivity.class);
                    startActivity(gameIntent);
                }  else {
                    if (ringtone != null && ringtone.isPlaying()) {
                        ringtone.stop();
                    }
                    finish(); // Close the activity
                }
            }
        });

        // Snooze alarm for 5 minutes
        snoozeButton.setOnClickListener(v -> {
            if (alarm != null && alarm.isSnoozeEnabled()){
                snoozeAlarm(5); // snooze for 5 minutes
                stopRingtone();
                finish();
            }
        });
    }
    @SuppressLint("ScheduleExactAlarm")
    private void snoozeAlarm(int minutes) {
        Calendar snoozeTime = Calendar.getInstance();
        snoozeTime.add(Calendar.MINUTE, minutes);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("alarmId", alarmId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, alarmId, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                snoozeTime.getTimeInMillis(),
                pendingIntent
        );

        Log.d("AlarmRingActivity", "Snoozed for " + minutes + " minutes");
    }

    private void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}


