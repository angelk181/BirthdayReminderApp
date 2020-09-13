package com.example.birthdayreminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.MyViewHolder> {

    Context context;
    ArrayList<Events> arrayList;
    DBOpenHelper dbOpenHelper;

    public EventRecyclerAdapter(Context context, ArrayList<Events> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_rowlayout,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Events birthdays = arrayList.get(position);
        holder.Birthday.setText(birthdays.getBIRTHDAY());
        holder.DateTxt.setText(birthdays.getDATE());
        holder.Time.setText(birthdays.getTIME());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCalendarEvent(birthdays.getBIRTHDAY(),birthdays.getDATE(),birthdays.getTIME());
                arrayList.remove(position);
                notifyDataSetChanged();
            }
        });

        if (isAlarm(birthdays.getDATE(),birthdays.getBIRTHDAY(),birthdays.getTIME())){
           holder.setAlarm.setImageResource(R.drawable.ic_action_notification_on);

        }else {
            holder.setAlarm.setImageResource(R.drawable.ic_action_notification_off);

        }
        Calendar datecalendar = Calendar.getInstance();
        datecalendar.setTime(convertStringToDate(birthdays.getDATE()));
        final int alarmYear = datecalendar.get(Calendar.YEAR);
        final int alarmMonth = datecalendar.get(Calendar.MONTH);
        final int alarmDay = datecalendar.get(Calendar.DAY_OF_MONTH);
        Calendar timecalendar = Calendar.getInstance();
        timecalendar.setTime(convertStringToTime(birthdays.getTIME()));
        final int alarmHour = timecalendar.get(Calendar.HOUR_OF_DAY);
        final int alarmMinute =timecalendar.get(Calendar.MINUTE);


        holder.setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAlarm(birthdays.getDATE(),birthdays.getBIRTHDAY(),birthdays.getTIME())){

                    holder.setAlarm.setImageResource(R.drawable.ic_action_notification_off);
                    cancelAlarm(getRequestCode(birthdays.getDATE(),birthdays.getBIRTHDAY(),birthdays.getTIME()));
                    updateEvent(birthdays.getDATE(),birthdays.getBIRTHDAY(),birthdays.getTIME(),"off");
                    notifyDataSetChanged();

                }else {
                    holder.setAlarm.setImageResource(R.drawable.ic_action_notification_on);
                    Calendar alarmCalendar = Calendar.getInstance();
                    alarmCalendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinute);
                    setAlarm(alarmCalendar,birthdays.getBIRTHDAY(),birthdays.getTIME(),getRequestCode(birthdays.getDATE(),
                            birthdays.getBIRTHDAY(),birthdays.getTIME()));
                    updateEvent(birthdays.getDATE(),birthdays.getBIRTHDAY(),birthdays.getTIME(),"on");
                    notifyDataSetChanged();

                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView DateTxt,Birthday,Time;
        Button delete;
        ImageButton setAlarm;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            DateTxt = itemView.findViewById(R.id.birthdaydate);
            Birthday = itemView.findViewById(R.id.bdayname);
            Time = itemView.findViewById(R.id.bdaytime);
            delete = itemView.findViewById(R.id.Delete);
            setAlarm = itemView.findViewById(R.id.alarmmeBtn);
        }
    }

    private Date convertStringToDate(String dateInString){
        java.text.SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(dateInString);

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private Date convertStringToTime(String dateInString){
        java.text.SimpleDateFormat format = new SimpleDateFormat("kk:mm", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(dateInString);

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private void deleteCalendarEvent(String birthday, String date, String time){
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.deleteEvent(birthday,date,time,database);
        dbOpenHelper.close();
    }

    private boolean isAlarm(String date,String birthday, String time){
        boolean alarmed = false;
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase sqLiteDatabase = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadIDEvents(date,birthday,time,sqLiteDatabase);
        while (cursor.moveToNext()) {

          String notify = cursor.getString(cursor.getColumnIndex(DBStructure.Notify));
          if (notify.equals("on")){
              alarmed = true;
          } else {
              alarmed = false;
          }
        }
        cursor.close();
        dbOpenHelper.close();
        return alarmed;

    }

    private void setAlarm(Calendar calendar, String birthday, String time, int RequestCode){
        Intent intent = new Intent(context.getApplicationContext(),AlarmReciever.class );
        intent.putExtra("birthday",birthday);
        intent.putExtra("time",time);
        intent.putExtra("id",RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)context.getApplicationContext().getSystemService(context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);


    }

    private void cancelAlarm(int RequestCode){
        Intent intent = new Intent(context.getApplicationContext(),AlarmReciever.class );
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)context.getApplicationContext().getSystemService(context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);


    }
    private int getRequestCode(String date, String birthday, String time){
        int code = 0;
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase sqLiteDatabase = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadIDEvents(date,birthday,time,sqLiteDatabase);
        while (cursor.moveToNext()) {

            code = cursor.getInt(cursor.getColumnIndex(DBStructure.ID));



        }
        cursor.close();
        dbOpenHelper.close();

        return code;
    }

    private void updateEvent(String date, String birthday, String time,String notify){
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.updateEvent(date,birthday,time,notify,database);
        dbOpenHelper.close();



    }
}
