package com.example.wakemeup.alarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class AlarmDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "alarm_db";
    private static final int DB_VERSION = 2; // Update version if schema changes

    public AlarmDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE alarms (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "time TEXT, " +
                "hour INTEGER, " +
                "minute INTEGER, " +
                "isEnabled INTEGER, " +
                "label TEXT, " +
                "sound TEXT, " +
                "repeat TEXT, " +
                "gameEnabled INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        // Simple schema change: drop and recreate table
        db.execSQL("DROP TABLE IF EXISTS alarms");
        onCreate(db);
    }

    public long insertAlarm(String time, int hour, int minute, boolean isEnabled,
                            String label, String repeat, boolean gameEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("hour", hour);
        values.put("minute", minute);
        values.put("isEnabled", isEnabled ? 1 : 0);
        values.put("label", label);
        values.put("sound", "");
        values.put("repeat", repeat);
        values.put("gameEnabled", gameEnabled ? 1 : 0);

        long id = db.insert("alarms", null, values);
        db.close();
        return id;
    }

    public ArrayList<Alarm> getAllAlarms() {
        ArrayList<Alarm> alarms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM alarms ORDER BY hour ASC, minute ASC", null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            int hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour"));
            int minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute"));
            boolean isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("isEnabled")) == 1;
            String label = cursor.getString(cursor.getColumnIndexOrThrow("label"));
            String repeat = cursor.getString(cursor.getColumnIndexOrThrow("repeat"));
            boolean gameEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("gameEnabled")) == 1;

            alarms.add(new Alarm(id, time, isEnabled, hour, minute, label, repeat, gameEnabled));
        }

        cursor.close();
        db.close();
        return alarms;
    }

    public Alarm getAlarmById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM alarms WHERE id=?", new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            int hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour"));
            int minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute"));
            boolean isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("isEnabled")) == 1;
            String label = cursor.getString(cursor.getColumnIndexOrThrow("label"));
            String repeat = cursor.getString(cursor.getColumnIndexOrThrow("repeat"));
            boolean gameEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("gameEnabled")) == 1;

            cursor.close();
            db.close();

            return new Alarm(id, time, isEnabled, hour, minute, label, repeat, gameEnabled);
        }

        cursor.close();
        db.close();
        return null;
    }

    public void deleteAlarm(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("alarms", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateToggle(int id, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isEnabled", isEnabled ? 1 : 0);
        db.update("alarms", values, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateAlarmLabel(int id, String label) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("label", label);
        db.update("alarms", values, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }
}
