package com.kosign.reminderdemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.kosign.reminderdemo.data.Data;
import com.kosign.reminderdemo.data.Meeting;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ExpAdapter extends BaseExpandableListAdapter {

    int showNowBarOnItem;
    private String currentDateTimeString;
    Context mContext;
    public ArrayList<Data> parents;

    public ExpAdapter(Context ctx, ArrayList<Data> parents){
        this.mContext = ctx;
        this.parents = parents;

    }


    @Override
    public int getGroupCount() {
        return parents.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return parents.get(i).getMeetings().size();
    }

    @Override
    public Object getGroup(int i) {
        return parents.get(i).getDate();
    }

    @Override
    public Object getChild(int i, int i1) {
        return parents.get(i).getMeetings().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            view = inflater.inflate(R.layout.header_item, viewGroup, false);
        }
        TextView parent_textvew = view.findViewById(R.id.textView4);
        parent_textvew.setText(parents.get(i).getDate());

        return  view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        View convertView = null;
        if(view != null){
            convertView = view;
        }else {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.child_item, viewGroup, false);
        }
        Meeting meeting = (Meeting) getChild(i, i1);
        LinearLayout frameLayout = convertView.findViewById(R.id.fl_now);
        frameLayout.bringToFront();

        ConstraintLayout itemContainer = convertView.findViewById(R.id.item_container);

        if (meeting.getRoom() == null && meeting.getStartTime() == null){
            itemContainer.setVisibility(View.GONE);
        }
        else{
            TextView tvNow = convertView.findViewById(R.id.tv_now);
            tvNow.setBackground(createRoundDrawable(meeting.getStickColor(), getValueInDP(mContext, 15)));

            View vLine = convertView.findViewById(R.id.v_line);
            vLine.setBackgroundColor(meeting.getStickColor());

            itemContainer.setVisibility(View.VISIBLE);
            itemContainer.setBackground(createRoundDrawable(meeting.getStickColor(), getValueInDP(mContext, 15)));

            TextView title = convertView.findViewById(R.id.textView);
            title.setText(meeting.getRoom());

            TextView des = convertView.findViewById(R.id.textView2);
            des.setText(meeting.getMeetingTitle());

            TextView meetingTime = convertView.findViewById(R.id.textView3);
            meetingTime.setText(meeting.getStartTime() + " - " + meeting.getEndTime());

        }

        if (meeting.getNowBarShowed()) {
            frameLayout.setTranslationY(meeting.getMovePosition());
            frameLayout.setVisibility(View.VISIBLE);
        } else {
            frameLayout.setVisibility(View.GONE);
        }

        return  convertView;
    }

    public GradientDrawable createRoundDrawable(int solidColor, float corner) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(corner);
        shape.setColor(solidColor);
        return shape;
    }

    private float px2dp(int pxVal) {
        float density = mContext.getResources().getDisplayMetrics().density;

        return (pxVal/ density);
    }

    // value in DP
    public int getValueInDP(Context context, int value){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    public float getValueInDP(Context context, float value){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    // value in PX
    public int getValueInPixel(Context context, int value){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, value, context.getResources().getDisplayMetrics());
    }

    public float getValueInPixel(Context context, float value){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, value, context.getResources().getDisplayMetrics());
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
