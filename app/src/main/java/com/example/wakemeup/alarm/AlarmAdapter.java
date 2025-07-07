package com.example.wakemeup.alarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wakemeup.R;

import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private Context context;
    private ArrayList<Alarm> alarmList;
    private AlarmDBHelper dbHelper;
    private String formatRepeatText(String repeat) {
        if (repeat == null || repeat.trim().isEmpty() || repeat.equals("Never")) {
            return "Never";
        } else if (repeat.contains(",")) {
            return "Repeat on " + repeat;
        } else {
            return "Repeat every " + repeat;
        }
    }

    public AlarmAdapter(Context context, ArrayList<Alarm> alarmList, AlarmDBHelper dbHelper) {
        this.context = context;
        this.alarmList = alarmList;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.alarm_item, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm currentAlarm = alarmList.get(position);

        int hour = currentAlarm.getHour();
        int minute = currentAlarm.getMinute();
        String amPm = (hour >= 12) ? "PM" : "AM";
        int displayHour = (hour % 12 == 0) ? 12 : hour % 12;
        String formattedTime = String.format("%02d:%02d %s", displayHour, minute, amPm);

        holder.alarmTimeText.setText(currentAlarm.getTime());
        holder.alarmSwitch.setChecked(currentAlarm.isEnabled());
        holder.alarmLabelText.setText(currentAlarm.getLabel());
        holder.alarmSwitch.setOnCheckedChangeListener(null);
        holder.alarmRepeatText.setText(formatRepeatText(currentAlarm.getRepeat()));

        int textColor = currentAlarm.isEnabled() ? Color.WHITE : Color.GRAY;

        holder.alarmTimeText.setTextColor(textColor);
        holder.alarmLabelText.setTextColor(textColor);
        holder.alarmRepeatText.setTextColor(textColor);


        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentAlarm.setEnabled(isChecked);
            dbHelper.updateToggle(currentAlarm.getId(), isChecked);

            // Update text color when switch is toggled
            int updatedColor = isChecked ? Color.WHITE : Color.GRAY;
            holder.alarmTimeText.setTextColor(updatedColor);
            holder.alarmLabelText.setTextColor(updatedColor);
            holder.alarmRepeatText.setTextColor(updatedColor);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SetAlarmActivity.class);
            intent.putExtra("alarmId", currentAlarm.getId());  // ✅ pass alarm ID
            ((Activity) context).startActivityForResult(intent, 1); // or REQUEST_CODE_EDIT if you define it
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public Alarm getAlarmAt(int position) {
        return alarmList.get(position);
    }

    public void removeAlarm(int position) {
        alarmList.remove(position);
        notifyItemRemoved(position);
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView alarmTimeText;
        TextView alarmLabelText;
        SwitchCompat alarmSwitch;
        TextView alarmRepeatText;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmTimeText = itemView.findViewById(R.id.alarmTimeText);
            alarmLabelText = itemView.findViewById(R.id.alarmLabelText);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
            alarmRepeatText = itemView.findViewById(R.id.alarmRepeatText);
        }

    }
}
