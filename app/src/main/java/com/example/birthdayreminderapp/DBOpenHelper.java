package com.example.birthdayreminderapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;




public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String CREATE_BIRTHDAYS_TABLE = "create table "+DBStructure.BIRTHDAY_TABLE_NAME+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, "
            +DBStructure.Birthday+" TEXT, "+DBStructure.Time+" TEXT, "+DBStructure.Date+" TEXT, "+DBStructure.Month+" TEXT, "
            +DBStructure.Year+" TEXT, "+DBStructure.Notify+" TEXT)";
    private static final String DROP_BIRTHDAYS_TABLE= "DROP TABLE IF EXISTS "+DBStructure.BIRTHDAY_TABLE_NAME;



    public DBOpenHelper(@Nullable Context context) {
        super(context, DBStructure.DB_NAME, null, DBStructure.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BIRTHDAYS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_BIRTHDAYS_TABLE);
        onCreate(db);
    }

    public void SaveEvent (String birthday, String time, String date, String month, String year,String notify, SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBStructure.Birthday,birthday);
        contentValues.put(DBStructure.Time,time);
        contentValues.put(DBStructure.Date, date);
        contentValues.put(DBStructure.Month, month);
        contentValues.put(DBStructure.Year, year);
        contentValues.put(DBStructure.Notify,notify);
        database.insert(DBStructure.BIRTHDAY_TABLE_NAME, null, contentValues);

    }

    public Cursor ReadEvents(String date, SQLiteDatabase database) {
        String [] Projections = {DBStructure.Birthday, DBStructure.Time,DBStructure.Date,DBStructure.Month,DBStructure.Year};
        String Selection = DBStructure.Date +"=?";
        String [] SelectionArgs = {date};

        return database.query(DBStructure.BIRTHDAY_TABLE_NAME,Projections,Selection,SelectionArgs, null, null,null);
    }

    public Cursor ReadIDEvents(String date,String birthday, String time,SQLiteDatabase database) {
        String [] Projections = {DBStructure.ID,DBStructure.Notify,DBStructure.Time};
        String Selection = DBStructure.Date +"=? and "+DBStructure.Birthday+"=? and "+DBStructure.Time+"=?";
        String [] SelectionArgs = {date,birthday,time};

        return database.query(DBStructure.BIRTHDAY_TABLE_NAME,Projections,Selection,SelectionArgs,null,null,null);
    }


    public Cursor ReadEventsPerMonth(String month , String year, SQLiteDatabase database) {
        String [] Projections = {DBStructure.Birthday, DBStructure.Time,DBStructure.Date,DBStructure.Month,DBStructure.Year};
        String Selection = DBStructure.Month +"=? and " +DBStructure.Year+"=?";
        String [] SelectionArgs = {month,year};

        return database.query(DBStructure.BIRTHDAY_TABLE_NAME,Projections,Selection,SelectionArgs, null, null,null);
    }

    public void deleteEvent(String birthday,String date,String time,SQLiteDatabase database){
        String selection = DBStructure.Birthday+"=? and "+DBStructure.Date+"=? and "+DBStructure.Time+"=?";
        String[] selectionArg = {birthday,date,time};
        database.delete(DBStructure.BIRTHDAY_TABLE_NAME,selection,selectionArg);
    }


    public void updateEvent(String date,String birthday, String time,String notify,SQLiteDatabase database){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBStructure.Notify,notify);
        String Selection = DBStructure.Date +"=? and "+DBStructure.Birthday+"=? and "+DBStructure.Time+"=?";
        String [] SelectionArgs = {date,birthday,time};
        database.update(DBStructure.BIRTHDAY_TABLE_NAME,contentValues,Selection,SelectionArgs);
    }



}
