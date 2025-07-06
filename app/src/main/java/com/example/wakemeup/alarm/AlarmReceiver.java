package com.example.wakemeup.alarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver {
    @SuppressLint("ScheduleExactAlarm")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarm triggered!");

        try {
            Log.d("AlarmReceiver", "Alarm triggered!");
            int alarmId = intent.getIntExtra("alarmId", -1);
            if (alarmId == -1) return;

            AlarmDBHelper dbHelper = new AlarmDBHelper(context);
            Alarm alarm = dbHelper.getAlarmById(alarmId);
            if (alarm == null || !alarm.isEnabled()) return;

            String repeat = alarm.getRepeat();
            if (repeat != null && !repeat.equalsIgnoreCase("Never")) {
                String[] days = repeat.split(",");
                Calendar calendar = Calendar.getInstance();
                String today = new SimpleDateFormat("EEE", Locale.ENGLISH).format(calendar.getTime());

                boolean match = false;
                for (String d : days) {
                    if (today.equalsIgnoreCase(d.trim())) {
                        match = true;
                        break;
                    }
                }
                if (!match) return;
            }

            Intent ringIntent = new Intent(context, AlarmRingActivity.class);
            ringIntent.putExtra("label", alarm.getLabel());
            ringIntent.putExtra("time", alarm.getTime());
            ringIntent.putExtra("alarmId", alarm.getId());
            ringIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ringIntent);

            if (repeat != null && !repeat.equalsIgnoreCase("Never")) {
                String[] days = repeat.split(",");
                Calendar nextAlarm = Calendar.getInstance();
                int currentDay = nextAlarm.get(Calendar.DAY_OF_WEEK);

                Map<String, Integer> dayMap = new HashMap<>();
                dayMap.put("Sun", Calendar.SUNDAY);
                dayMap.put("Mon", Calendar.MONDAY);
                dayMap.put("Tue", Calendar.TUESDAY);
                dayMap.put("Wed", Calendar.WEDNESDAY);
                dayMap.put("Thu", Calendar.THURSDAY);
                dayMap.put("Fri", Calendar.FRIDAY);
                dayMap.put("Sat", Calendar.SATURDAY);

                int minDaysUntilNext = 8;
                for (String d : days) {
                    Integer dayOfWeek = dayMap.get(d.trim());
                    if (dayOfWeek != null) {
                        int daysUntil = (dayOfWeek + 7 - currentDay) % 7;
                        if (daysUntil == 0) daysUntil = 7;
                        minDaysUntilNext = Math.min(minDaysUntilNext, daysUntil);
                    }
                }

                nextAlarm.add(Calendar.DAY_OF_YEAR, minDaysUntilNext);
                nextAlarm.set(Calendar.HOUR_OF_DAY, alarm.getHour());
                nextAlarm.set(Calendar.MINUTE, alarm.getMinute());
                nextAlarm.set(Calendar.SECOND, 0);
                nextAlarm.set(Calendar.MILLISECOND, 0);

                Intent rescheduleIntent = new Intent(context, AlarmReceiver.class);
                rescheduleIntent.putExtra("alarmId", alarm.getId());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, alarm.getId(), rescheduleIntent, PendingIntent.FLAG_IMMUTABLE
                );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextAlarm.getTimeInMillis(),
                            pendingIntent
                    );
                }

                Log.d("AlarmReceiver", "Alarm rescheduled for " + nextAlarm.getTime());
            }

        } catch (Exception e) {
            Log.e("AlarmReceiver", "Exception in onReceive: " + e.getMessage(), e);
        }
    }
}
