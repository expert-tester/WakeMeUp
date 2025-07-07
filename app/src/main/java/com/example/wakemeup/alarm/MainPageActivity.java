package com.example.wakemeup.alarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wakemeup.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainPageActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1;
    private RecyclerView alarmRecyclerView;
    private AlarmAdapter adapter;
    private AlarmDBHelper dbHelper;
    private ArrayList<Alarm> alarmList;
    private Button addBtn;

    private Alarm recentlyDeletedAlarm;
    private int recentlyDeletedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        dbHelper = new AlarmDBHelper(this);
        alarmRecyclerView = findViewById(R.id.alarmRecyclerView);
        addBtn = findViewById(R.id.addBtn);

        alarmList = dbHelper.getAllAlarms();
        adapter = new AlarmAdapter(this, alarmList, dbHelper);

        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alarmRecyclerView.setAdapter(adapter);

        // Attach swipe handler
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(alarmRecyclerView);

        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainPageActivity.this, SetAlarmActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    // Swipe to delete with undo
    ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            recentlyDeletedPosition = viewHolder.getAdapterPosition();
            recentlyDeletedAlarm = adapter.getAlarmAt(recentlyDeletedPosition);

            // Cancel system alarm
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(MainPageActivity.this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    MainPageActivity.this, recentlyDeletedAlarm.getId(), intent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);

            // Remove from DB and UI
            dbHelper.deleteAlarm(recentlyDeletedAlarm.getId());
            adapter.removeAlarm(recentlyDeletedPosition);

            showUndoSnackbar();
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            Drawable deleteIcon = ContextCompat.getDrawable(MainPageActivity.this, R.drawable.ic_delete); //
            ColorDrawable background = new ColorDrawable(Color.GRAY);

            int itemViewTop = viewHolder.itemView.getTop();
            int itemViewBottom = viewHolder.itemView.getBottom();
            int itemViewLeft = viewHolder.itemView.getLeft();
            int itemViewRight = viewHolder.itemView.getRight();

            if (dX > 0) { // swipe right
                background.setBounds(itemViewLeft, itemViewTop, itemViewLeft + (int) dX, itemViewBottom);
                if (deleteIcon != null) {
                    int iconTop = itemViewTop + (viewHolder.itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                    int iconMargin = (viewHolder.itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                    int iconLeft = itemViewLeft + iconMargin;
                    int iconRight = iconLeft + deleteIcon.getIntrinsicWidth();
                    int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            } else if (dX < 0) { // swipe left
                background.setBounds(itemViewRight + (int) dX, itemViewTop, itemViewRight, itemViewBottom);
                if (deleteIcon != null) {
                    int iconTop = itemViewTop + (viewHolder.itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                    int iconMargin = (viewHolder.itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                    int iconRight = itemViewRight - iconMargin;
                    int iconLeft = iconRight - deleteIcon.getIntrinsicWidth();
                    int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }
            }

            background.draw(c);
        }
    };

    @SuppressLint("ScheduleExactAlarm")
    private void showUndoSnackbar() {
        Snackbar.make(alarmRecyclerView, "Alarm deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> {
                    long restoredId = dbHelper.insertAlarm(
                            recentlyDeletedAlarm.getTime(),
                            recentlyDeletedAlarm.getHour(),
                            recentlyDeletedAlarm.getMinute(),
                            recentlyDeletedAlarm.isEnabled(),
                            recentlyDeletedAlarm.getLabel(),
                            recentlyDeletedAlarm.getRepeat(),
                            recentlyDeletedAlarm.isSnoozeEnabled(),
                            recentlyDeletedAlarm.isGameEnabled()
                    );


                    recentlyDeletedAlarm.setId((int) restoredId);
                    alarmList.add(recentlyDeletedPosition, recentlyDeletedAlarm);
                    adapter.notifyItemInserted(recentlyDeletedPosition);

                    if (recentlyDeletedAlarm.isEnabled()) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, recentlyDeletedAlarm.getHour());
                        calendar.set(Calendar.MINUTE, recentlyDeletedAlarm.getMinute());
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_YEAR, 1);
                        }

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        Intent intent = new Intent(this, AlarmReceiver.class);
                        intent.putExtra("alarmId", restoredId);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                this, (int) restoredId, intent, PendingIntent.FLAG_IMMUTABLE);

                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    }
                }).show();
    }

    @SuppressLint("ScheduleExactAlarm")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD && resultCode == RESULT_OK) {
            Toast.makeText(this, "Alarm added", Toast.LENGTH_SHORT).show();
        }
        if (data != null && data.hasExtra("alarmData")) {
            int alarmId = data.getIntExtra("alarmData", -1); // Match SetAlarmActivity key
            int hour = data.getIntExtra("hour", -1);
            int minute = data.getIntExtra("minute", -1);

            if (alarmId != -1) {
                Alarm alarm = dbHelper.getAlarmById(alarmId);
                if (alarm != null && alarm.isEnabled()) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }

                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent intent = new Intent(this, AlarmReceiver.class);
                    intent.putExtra("alarmId", alarm.getId());

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            this, alarm.getId(), intent, PendingIntent.FLAG_IMMUTABLE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(this, "Exact alarm permission not granted", Toast.LENGTH_LONG).show();
                        return;
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

                    Log.d("MainActivity", "Alarm scheduled with ID: " + alarm.getId());
                }
            }


        // Refresh list
        alarmList.clear();
        alarmList.addAll(dbHelper.getAllAlarms());

        Collections.sort(alarmList, (a1, a2) -> {
            int hourCompare = Integer.compare(a1.getHour(), a2.getHour());
            return (hourCompare != 0) ? hourCompare :
                    Integer.compare(a1.getMinute(), a2.getMinute());
        });

        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Alarm added", Toast.LENGTH_SHORT).show();
    }
}
        private void loadAlarms() {
            alarmList.clear(); // this uses your class-level variable
            alarmList.addAll(dbHelper.getAllAlarms());
            adapter.notifyDataSetChanged(); // also your class-level adapter
        }
        @Override
        protected void onResume() {
            super.onResume();
            reloadAlarmList();
        }
        private void reloadAlarmList() {
            alarmList.clear();
            alarmList.addAll(dbHelper.getAllAlarms());

            Collections.sort(alarmList, (a1, a2) -> {
                int hourCompare = Integer.compare(a1.getHour(), a2.getHour());
                return (hourCompare != 0) ? hourCompare : Integer.compare(a1.getMinute(), a2.getMinute());
            });

            adapter.notifyDataSetChanged();
        }
    }








