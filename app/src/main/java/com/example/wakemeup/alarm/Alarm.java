package com.example.wakemeup.alarm;

public class Alarm {
    private int id;
    private String time;
    private int hour;
    private int minute;
    private boolean isEnabled;
    private String label;
    private String soundUri;
    private String repeat;
    private boolean snoozeEnabled;
    private boolean gameEnabled;



    public Alarm(int id, String time, boolean isEnabled, int hour, int minute, String label, String soundUri, boolean gameEnabled) {
        this.id = id;
        this.time = time;
        this.isEnabled = isEnabled;
        this.label = label;
        this.soundUri = soundUri;
        this.repeat = repeat;
        this.gameEnabled = gameEnabled;
    }

    public Alarm() {

    }

    public int getId() {
        return id;
    }
    public String getTime() {
        return time;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }
    public boolean isEnabled() {
        return isEnabled;
    }
    public String getLabel(){
        return label;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
    public void setLabel(String label){ this.label = label; }

    public void setId(int restoredId) {
    }
    public String getSoundUri() {
        return soundUri;
    }

    public void setSoundUri(String sound) {
        this.soundUri = sound;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public boolean isGameEnabled() {
        return gameEnabled;
    }

    public void setGameEnabled(boolean gameEnabled) {
        this.gameEnabled = gameEnabled;
    }

    public void setTime(String alarmTime) {
        this.time = alarmTime;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public void setMinute(int minute) {
        this.minute = minute;
    }
    public boolean isSnoozeEnabled() {
        return snoozeEnabled;
    }

    public void setSnoozeEnabled(boolean snoozeEnabled) {
        this.snoozeEnabled = snoozeEnabled;
    }

}
