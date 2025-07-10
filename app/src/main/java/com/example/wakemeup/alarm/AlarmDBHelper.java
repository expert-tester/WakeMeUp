package com.example.wakemeup.alarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class AlarmDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarms.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "alarms";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_MINUTE = "minute";
    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_LABEL = "label";
    private static final String COLUMN_SOUND_URI = "sound_uri";
    private static final String COLUMN_REPEAT = "repeat_days";
    private static final String COLUMN_SNOOZE = "snooze_enabled";
    private static final String COLUMN_GAME = "game_enabled";
    private static final String COLUMN_IS_SNOOZING = "is_snoozing";

    public AlarmDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TIME + " TEXT," +
                COLUMN_HOUR + " INTEGER," +
                COLUMN_MINUTE + " INTEGER," +
                COLUMN_ENABLED + " INTEGER," +
                COLUMN_LABEL + " TEXT," +
                COLUMN_SOUND_URI + " TEXT," +
                COLUMN_REPEAT + " TEXT," +
                COLUMN_SNOOZE + " INTEGER," +
                COLUMN_GAME + " INTEGER," +
                COLUMN_IS_SNOOZING + " INTEGER)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertAlarm(String time, int hour, int minute, boolean enabled,
                            String label, String repeat, boolean snoozeEnabled, boolean gameEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TIME, time);
        values.put(COLUMN_HOUR, hour);
        values.put(COLUMN_MINUTE, minute);
        values.put(COLUMN_ENABLED, enabled ? 1 : 0);
        values.put(COLUMN_LABEL, label);
        values.put(COLUMN_SOUND_URI, ""); // No custom sound
        values.put(COLUMN_REPEAT, repeat);
        values.put(COLUMN_SNOOZE, snoozeEnabled ? 1 : 0); // Default off
        values.put(COLUMN_GAME, gameEnabled ? 1 : 0);
        values.put(COLUMN_IS_SNOOZING, 0);

        return db.insert(TABLE_NAME, null, values);
    }

    public void updateAlarm(int id, String time, int hour, int minute, boolean enabled,
                            String label, String repeat, boolean snoozeEnabled, boolean gameEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TIME, time);
        values.put(COLUMN_HOUR, hour);
        values.put(COLUMN_MINUTE, minute);
        values.put(COLUMN_ENABLED, enabled ? 1 : 0);
        values.put(COLUMN_LABEL, label);
        values.put(COLUMN_REPEAT, repeat);
        values.put(COLUMN_SNOOZE, snoozeEnabled);
        values.put(COLUMN_GAME, gameEnabled ? 1 : 0);
        values.put(COLUMN_IS_SNOOZING, 0);

        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void updateRepeat(int id, String repeat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REPEAT, repeat);
        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void updateToggle(int id, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ENABLED, isEnabled ? 1 : 0);
        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void updateSnoozeState(int alarmId, boolean isSnoozing) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_SNOOZING, isSnoozing ? 1 : 0);
        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(alarmId)});
    }

    public void deleteAlarm(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Alarm getAlarmById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Alarm alarm = extractAlarmFromCursor(cursor);
            cursor.close();
            return alarm;
        }

        return null;
    }

    public ArrayList<Alarm> getAllAlarms() {
        ArrayList<Alarm> alarms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                alarms.add(extractAlarmFromCursor(cursor));
            }
            cursor.close();
        }

        return alarms;
    }

    private Alarm extractAlarmFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME));
        int hour = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUR));
        int minute = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTE));
        boolean isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENABLED)) == 1;
        String label = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LABEL));
        String soundUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOUND_URI));
        String repeat = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPEAT));
        boolean gameEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GAME)) == 1;
        boolean snoozeEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SNOOZE)) == 1;
        boolean isSnoozing = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SNOOZING)) == 1;

        Alarm alarm = new Alarm(id, time, isEnabled, hour, minute, label, soundUri, repeat, gameEnabled);
        alarm.setSnoozeEnabled(snoozeEnabled);
        alarm.setSnoozing(isSnoozing);
        return alarm;
    }
}

//public class AlarmDBHelper extends SQLiteOpenHelper {
//
//    private static final String DB_NAME = "alarm_db";
//    private static final int DB_VERSION = 2; // Update version if schema changes
//
//    public AlarmDBHelper(Context context) {
//        super(context, DB_NAME, null, DB_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        db.execSQL("CREATE TABLE alarms (" +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                "time TEXT, " +
//                "hour INTEGER, " +
//                "minute INTEGER, " +
//                "isEnabled INTEGER, " +
//                "label TEXT, " +
//                "sound TEXT, " +
//                "repeat TEXT, " +
//                "gameEnabled INTEGER)"
//        );
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
//        // Simple schema change: drop and recreate table
//        db.execSQL("DROP TABLE IF EXISTS alarms");
//        onCreate(db);
//    }
//
//    public long insertAlarm(String time, int hour, int minute, boolean isEnabled,
//                            String label, String repeat, boolean gameEnabled) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("time", time);
//        values.put("hour", hour);
//        values.put("minute", minute);
//        values.put("isEnabled", isEnabled ? 1 : 0);
//        values.put("label", label);
//        values.put("sound", "");
//        values.put("repeat", repeat);
//        values.put("gameEnabled", gameEnabled ? 1 : 0);
//
//        long id = db.insert("alarms", null, values);
//        db.close();
//        return id;
//    }
//
//    public ArrayList<Alarm> getAllAlarms() {
//        ArrayList<Alarm> alarms = new ArrayList<>();
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM alarms ORDER BY hour ASC, minute ASC", null);
//
//        while (cursor.moveToNext()) {
//            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
//            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
//            int hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour"));
//            int minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute"));
//            boolean isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("isEnabled")) == 1;
//            String label = cursor.getString(cursor.getColumnIndexOrThrow("label"));
//            String repeat = cursor.getString(cursor.getColumnIndexOrThrow("repeat"));
//            boolean gameEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("gameEnabled")) == 1;
//
//            alarms.add(new Alarm(id, time, isEnabled, hour, minute, label, repeat, gameEnabled));
//        }
//
//        cursor.close();
//        db.close();
//        return alarms;
//    }
//
//    public Alarm getAlarmById(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery("SELECT * FROM alarms WHERE id=?", new String[]{String.valueOf(id)});
//
//        if (cursor.moveToFirst()) {
//            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
//            int hour = cursor.getInt(cursor.getColumnIndexOrThrow("hour"));
//            int minute = cursor.getInt(cursor.getColumnIndexOrThrow("minute"));
//            boolean isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("isEnabled")) == 1;
//            String label = cursor.getString(cursor.getColumnIndexOrThrow("label"));
//            String repeat = cursor.getString(cursor.getColumnIndexOrThrow("repeat"));
//            boolean gameEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("gameEnabled")) == 1;
//
//            cursor.close();
//            db.close();
//
//            return new Alarm(id, time, isEnabled, hour, minute, label, repeat, gameEnabled);
//        }
//
//        cursor.close();
//        db.close();
//        return null;
//    }
//    public void updateAlarm(int id, String time, int hour, int minute,
//                            boolean isEnabled, String label, String repeat, boolean gameEnabled) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("time", time);
//        values.put("hour", hour);
//        values.put("minute", minute);
//        values.put("isEnabled", isEnabled ? 1 : 0);
//        values.put("label", label);
//        values.put("repeat", repeat);
//        values.put("gameEnabled", gameEnabled ? 1 : 0);
//
//        db.update("alarms", values, "id = ?", new String[]{String.valueOf(id)});
//        db.close();
//    }
//
//
//    public void deleteAlarm(int id) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete("alarms", "id=?", new String[]{String.valueOf(id)});
//        db.close();
//    }
//
//    public void updateToggle(int id, boolean isEnabled) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("isEnabled", isEnabled ? 1 : 0);
//        db.update("alarms", values, "id=?", new String[]{String.valueOf(id)});
//        db.close();
//    }
//
//    public void updateAlarmLabel(int id, String label) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("label", label);
//        db.update("alarms", values, "id=?", new String[]{String.valueOf(id)});
//        db.close();
//    }
//    public void updateRepeat(int id, String repeat) {
//        Log.d("AlarmDBHelper", "Updating repeat for alarm " + id + " to " + repeat);
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put("repeat", repeat);
//        db.update("alarms", values, "id=?", new String[]{String.valueOf(id)});
//        db.close();
//    }
//
//}
