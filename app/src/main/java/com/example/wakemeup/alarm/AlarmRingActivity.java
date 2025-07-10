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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wakemeup.R;
import com.example.wakemeup.games.GameHostActivity;

import java.util.Objects;

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
                dbHelper.updateSnoozeState(alarmId, false);
                assert alarm != null;
                if (alarm.isGameEnabled()) {
                    Intent gameIntent = new Intent(AlarmRingActivity.this, GameHostActivity.class);
                    startActivity(gameIntent);
                }  else {
                    stopRingtone();
                    finish();
                }
            }
        });

        // Snooze alarm for 5 minutes
        snoozeButton.setOnClickListener(v -> {
            dbHelper.updateSnoozeState(alarmId, true);
            assert alarm != null;
            Log.d("AlarmRingActivity", String.valueOf(alarm.isGameEnabled()));
            if (alarm.isGameEnabled()) {
                snoozeAlarm(1);
                Log.d("AlarmRingActivity", "gameIntent run");
                Intent gameIntent = new Intent(AlarmRingActivity.this, GameHostActivity.class);
                Log.d("AlarmRingActivity", gameIntent.toString());
                startActivity(gameIntent);
            } else {
                snoozeAlarm(1);
                stopRingtone();
                finish();
            }
        });
    }
    @SuppressLint("ScheduleExactAlarm")
    private void snoozeAlarm(int minutes) {
        Calendar snoozeTime = Calendar.getInstance();
        snoozeTime.add(Calendar.MINUTE, minutes);

        Intent snoozeIntent = new Intent(this, AlarmReceiver.class);
        snoozeIntent.putExtra("alarmId", alarmId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, alarmId, snoozeIntent, PendingIntent.FLAG_IMMUTABLE);

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
        unregisterReceiver(gamesCompletedReceiver);
        stopRingtone();
    }
}


