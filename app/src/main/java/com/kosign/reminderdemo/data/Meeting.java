package com.kosign.reminderdemo.data;

public class Meeting {

    private int stickColor;
    private String room;
    private String meetingTitle;
    private String startTime;
    private String endTime;
    private Boolean nowBarShowed;
    private int movePosition;

    public Meeting(){}

    public Meeting(int stickColor, String room, String meetingTitle, String startTime, String endTime, Boolean nowBarShowed, int movePosition) {
        this.stickColor = stickColor;
        this.room = room;
        this.meetingTitle = meetingTitle;
        this.startTime = startTime;
        this.endTime = endTime;
        this.nowBarShowed = nowBarShowed;
        this.movePosition = movePosition;
    }

    public void setNowBarShowed(Boolean nowBarShowed) {
        this.nowBarShowed = nowBarShowed;
    }

    public Boolean getNowBarShowed() {
        return nowBarShowed;
    }

    public int getStickColor() {
        return stickColor;
    }

    public void setStickColor(int stickColor) {
        this.stickColor = stickColor;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getMeetingTitle() {
        return meetingTitle;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setMeetingTitle(String meetingTitle) {
        this.meetingTitle = meetingTitle;
    }

    public int getMovePosition() {
        return movePosition;
    }

    public void setMovePosition(int movePosition) {
        this.movePosition = movePosition;
    }
}
