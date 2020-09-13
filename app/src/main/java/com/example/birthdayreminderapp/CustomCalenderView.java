package com.example.birthdayreminderapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CustomCalenderView extends LinearLayout {

    ImageButton ForwardButton,BackButton;
    TextView CurrentDate;
    GridView gridView;
    private static final int MAX_CALENDAR_DAYS = 42;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM",Locale.ENGLISH);
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy",Locale.ENGLISH);
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    Context context;
    List<Events> eventsList = new ArrayList<>();
    List<Date> dates = new ArrayList<>();
    int alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinute;
    DBOpenHelper dbOpenHelper;
    AlertDialog alertDialog;
    MyGridAdapter adapter;

    public CustomCalenderView (final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        IntializeUILayout();
        SetUpCalendar();
        BackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH,-1);
                SetUpCalendar();

            }
        });

        ForwardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH,1);
                SetUpCalendar();
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder =new AlertDialog.Builder(context);
                builder.setCancelable(true);
                final View eventView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_newevent_layout,null);
                final EditText birthdayName = eventView.findViewById(R.id.birthdayname);
                final TextView birthdayTime = eventView.findViewById(R.id.birthdaytime);
                ImageButton SelectTime = eventView.findViewById(R.id.setbirthdaytime);
                final CheckBox alarmMe = eventView.findViewById(R.id.alarmme);
                Calendar dateCalendar = Calendar.getInstance();
                dateCalendar.setTime(dates.get(position));
                alarmYear = dateCalendar.get(Calendar.YEAR);
                alarmMonth = dateCalendar.get(Calendar.MONTH);
                alarmDay = dateCalendar.get(Calendar.DAY_OF_MONTH);


                Button addBirthday = eventView.findViewById(R.id.addbirthday);
                SelectTime.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();

                        final int hours =calendar.get(Calendar.HOUR_OF_DAY);
                        final int minutes = calendar.get(Calendar.MINUTE);

                        TimePickerDialog timePickerDialog;
                        timePickerDialog = new TimePickerDialog(getContext(),R.style.Theme_AppCompat_Dialog, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar c = Calendar.getInstance();
                                c.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                c.set(Calendar.MINUTE,minute);
                                c.setTimeZone(TimeZone.getDefault());
                                SimpleDateFormat format = new SimpleDateFormat("K:mm a", Locale.ENGLISH);
                                String birthday_Time = format.format(c.getTime());
                                birthdayTime.setText(birthday_Time);
                                alarmHour = c.get(Calendar.HOUR_OF_DAY);
                                alarmMinute = c.get(Calendar.MINUTE);
                            }
                        },hours,minutes,false);

                        timePickerDialog.show();
                    }
                });

                final String date = dateFormat.format(dates.get(position));

                addBirthday.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(alarmMe.isChecked()) {
                            SaveBirthday(birthdayName.getText().toString(),birthdayTime.getText().toString(),date
                                    ,monthFormat.format(dates.get(position)),yearFormat.format(dates.get(position)),"on");
                            SetUpCalendar();
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinute);
                            setAlarm(calendar,birthdayName.getText().toString(),birthdayTime.getText().toString(),getRequestCode(date
                            ,birthdayName.getText().toString(),birthdayTime.getText().toString()));
                            alertDialog.dismiss();

                        }else{
                            SaveBirthday(birthdayName.getText().toString(),birthdayTime.getText().toString(),date
                                    ,monthFormat.format(dates.get(position)),yearFormat.format(dates.get(position)),"off");
                            SetUpCalendar();
                            alertDialog.dismiss();

                        }



                    }
                });


                builder.setView(eventView);
                alertDialog = builder.create();
                alertDialog.show();






            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String date = dateFormat.format(dates.get(position));

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                View showView = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_layout,null);
                RecyclerView birthdayRV= (RecyclerView) showView.findViewById(R.id.BirthdaysRV);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(showView.getContext());
                birthdayRV.setLayoutManager(layoutManager);
                birthdayRV.setHasFixedSize(true);

                EventRecyclerAdapter eventRecyclerAdapter = new EventRecyclerAdapter(showView.getContext()
                        ,CollectEvent(date));
                birthdayRV.setAdapter(eventRecyclerAdapter);
                eventRecyclerAdapter.notifyDataSetChanged();
                builder.setView(showView);
                alertDialog =builder.create();
                alertDialog.show();
                alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                       SetUpCalendar();
                    }
                });

                return true;
            }
        });

    }
    public CustomCalenderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

    private void setAlarm(Calendar calendar, String birthday, String time, int RequestCode){
        Intent intent = new Intent(context.getApplicationContext(),AlarmReciever.class );
        intent.putExtra("birthday",birthday);
        intent.putExtra("time",time);
        intent.putExtra("id",RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)context.getApplicationContext().getSystemService(context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);


    }

    private ArrayList<Events> CollectEvent(String date){
        ArrayList<Events> arrayList = new ArrayList<>();
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase sqLiteDatabase = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadEvents(date,sqLiteDatabase);
        while (cursor.moveToNext()){
            String event = cursor.getString(cursor.getColumnIndex(DBStructure.Birthday));
            String Time = cursor.getString(cursor.getColumnIndex(DBStructure.Time));
            String Date = cursor.getString(cursor.getColumnIndex(DBStructure.Date));
            String month = cursor.getString(cursor.getColumnIndex(DBStructure.Month));
            String year = cursor.getString(cursor.getColumnIndex(DBStructure.Year));
            Events birthdays = new Events(event,Time,Date,month,year);
            arrayList.add(birthdays);
        }
        cursor.close();
        dbOpenHelper.close();
// Toast.makeText(context, String.valueOf(arrayList.size()), Toast.LENGTH_SHORT).show();

        return arrayList;
    }

    private void IntializeUILayout(){

        LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.calender_layout,this);
        BackButton = view.findViewById(R.id.back);
        ForwardButton = view.findViewById(R.id.forward);
        CurrentDate = view.findViewById(R.id.current_Date);
        gridView = view.findViewById(R.id.gridView);


    }

    private void SetUpCalendar(){
        String StartDate = simpleDateFormat.format(calendar.getTime());
        CurrentDate.setText(StartDate);
        dates.clear();
        Calendar monthCalendar = (Calendar)calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH,1);
        int FirstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK)-1;
        monthCalendar.add(Calendar.DAY_OF_MONTH,-FirstDayOfMonth);


        CollectBirthdaysPerMonth(monthFormat.format(calendar.getTime()),yearFormat.format(calendar.getTime()));


        while (dates.size() < MAX_CALENDAR_DAYS){
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH,1);

        }
        adapter = new MyGridAdapter(context,dates,calendar,eventsList);
        gridView.setAdapter(adapter);


    }

    private void SaveBirthday(String birthday,String time,String date,String Month,String Year, String notify){
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.SaveEvent(birthday,time,date,Month,Year,notify,database);
        dbOpenHelper.close();
        Toast.makeText(context, "Birthday Saved! ^.^", Toast.LENGTH_SHORT).show();
    }

    private Date convertStringToDate(String dateInString) {
        java.text.SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(dateInString);

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private void CollectBirthdaysPerMonth(String Month,String Year){
        eventsList.clear();
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadEventsPerMonth(Month,Year,database);
        while (cursor.moveToNext()){
            String birthday = cursor.getString(cursor.getColumnIndex(DBStructure.Birthday));
            String Time = cursor.getString(cursor.getColumnIndex(DBStructure.Time));
            String Date = cursor.getString(cursor.getColumnIndex(DBStructure.Date));
            String month = cursor.getString(cursor.getColumnIndex(DBStructure.Month));
            String year = cursor.getString(cursor.getColumnIndex(DBStructure.Year));
            Events events = new Events(birthday,Time,Date,month,year);
            eventsList.add(events);
        }
    }


}
