package com.example.birthdayreminderapp;


public class Events {
    String BIRTHDAY, TIME, DATE, MONTH, YEAR;

    public Events(String birthday, String time, String date, String month, String year) {
        BIRTHDAY = birthday;
        TIME = time;
        DATE = date;

    }

    public String getMONTH() {
        return MONTH;
    }

    public void setMONTH(String month) {
        MONTH = month;
    }

    public String getYEAR() {
        return YEAR;
    }

    public void setYEAR(String year) {
        YEAR = year;
    }


    public String getBIRTHDAY() {
        return BIRTHDAY;
    }

    public void setBIRTHDAY(String birthday) {
        BIRTHDAY = birthday;
    }

    public String getTIME() {
        return TIME;
    }

    public void setTIME(String time) {
        TIME = time;
    }

    public String getDATE() {
        return DATE;
    }

    public void setDATE(String date) {
        DATE = date;
    }




}
