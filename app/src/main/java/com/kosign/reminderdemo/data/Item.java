package com.kosign.reminderdemo.data;

import java.util.List;

public class Item {

    private int itemType;
    private Meeting meeting;

    public Item(int itemType, Meeting meeting) {
        this.itemType = itemType;
        this.meeting = meeting;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }
}
