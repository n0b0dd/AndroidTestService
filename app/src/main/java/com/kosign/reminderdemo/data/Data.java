package com.kosign.reminderdemo.data;

import java.util.ArrayList;

public class Data {

    private String date;
    private ArrayList<Meeting> meetings;

    public Data(){}

    public Data(String date, ArrayList<Meeting> meetings) {
        this.date = date;
        this.meetings = meetings;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<Meeting> getMeetings() {
        return meetings;
    }

    public void setMeetings(ArrayList<Meeting> meetings) {
        this.meetings = meetings;
    }
}
