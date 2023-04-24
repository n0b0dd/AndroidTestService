package com.kosign.reminderdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.kosign.reminderdemo.data.Data;
import com.kosign.reminderdemo.data.Item;
import com.kosign.reminderdemo.data.Meeting;
import com.kosign.reminderdemo.data.Reservation;
import com.kosign.reminderdemo.widget.headerlist.FloatingGroupExpandableListView;
import com.kosign.reminderdemo.widget.headerlist.WrapperExpandableListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TextView tvRemain;
    int smoothScrollToPosition = 0;
    int countdownSecond = 0;
    int position;
    int showNowBarOnItem;
    private String currentDateTimeString;
    FrameLayout remainContainer;
    int maxChildHeight = 0;

//    private RecyclerView expList;
//    private RecyclerViewAdapter adapter;

    private FloatingGroupExpandableListView expList;
    private ExpAdapter expandableAdapter;
    private ArrayList<Data> listData;

    TimePicker myTimePicker;
    Button buttonstartSetDialog;
    TextView textAlarmPrompt;
    TimePickerDialog timePickerDialog;

    private boolean isStopwatchRunning = false;

    private BroadcastReceiver statusReceiver;
    private BroadcastReceiver timeReceiver;

    private final static String CHANNEL_ID = "CHANNEL_1";
    final static int RQS_1 = 1;

    private String[] timeHeader(){
        return new String[]{
                "7:00 AM",
                "8:00 AM",
                "9:00 AM",
                "10:00 AM",
                "11:00 AM",
                "12:00 AM",
                "1:00 PM",
                "2:00 PM",
                "3:00 PM",
                "4:00 PM",
                "5:00 PM",
                "6:00 PM"
        };
    }

    private void fetchData(){
        List<Item> items = new ArrayList<>();
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation());
        for (int j = 0; j < timeHeader().length; j++) {
            String time = timeHeader()[j];
            for (int i = 0; i < reservations.size(); i++) {
                int reservationTime = Integer.parseInt(reservations.get(i).getStart_time().split(":")[0]);
                int reservationMin = Integer.parseInt(reservations.get(i).getStart_time().split(":")[1]);
                if (reservationTime > 12){
                    reservationTime = reservationTime % 12;
                }
                if (Integer.parseInt(time.split(" ")[0].split(":")[0]) == reservationTime && Integer.parseInt(time.split(" ")[0].split(":")[1]) < reservationMin) {

                }
            }
        }
        items.add(new Item(0, null));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Moving the service to background when the app is visible
        moveToBackground();
    }

    private void getStopwatchStatus() {
        Intent stopwatchService = new Intent(this, TimerService.class);
        stopwatchService.putExtra(TimerService.STOPWATCH_ACTION, TimerService.GET_STATUS);
        startService(stopwatchService);
    }

    private void pauseStopwatch() {
        SharedPreferences preferences = getSharedPreferences("timer", MODE_PRIVATE);
        preferences.edit().remove("isRunning").apply();
        preferences.edit().remove("remainMinute").apply();
        isMeetingStartSoon = false;
        Intent stopwatchService = new Intent(this, TimerService.class);
        stopwatchService.putExtra(TimerService.STOPWATCH_ACTION, TimerService.PAUSE);
        startService(stopwatchService);

    }

    private void startStopwatch() {
        Intent stopwatchService = new Intent(this, TimerService.class);
        stopwatchService.putExtra(TimerService.STOPWATCH_ACTION, TimerService.START);
        startService(stopwatchService);
    }

    private void moveToForeground() {
        Intent stopwatchService = new Intent(this, TimerService.class);
        stopwatchService.putExtra(
                TimerService.STOPWATCH_ACTION,
                TimerService.MOVE_TO_FOREGROUND
        );
        startService(stopwatchService);
    }

    private void moveToBackground() {
        Intent stopwatchService = new Intent(this, TimerService.class);
        stopwatchService.putExtra(
                TimerService.STOPWATCH_ACTION,
                TimerService.MOVE_TO_BACKGROUND
        );
        startService(stopwatchService);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Handler hander = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hander.post(new Runnable() {
                    @Override
                    public void run() {
                        getTime();
                    }
                });
            }
        }).start();

        getStopwatchStatus();

        // Receiving stopwatch status from service
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction(TimerService.STOPWATCH_STATUS);
        statusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isRunning = intent.getBooleanExtra(TimerService.IS_STOPWATCH_RUNNING, false);
                isStopwatchRunning = isRunning;
                int timeElapsed = intent.getIntExtra(TimerService.TIME_ELAPSED, 0) + 1;

//                updateLayout(isStopwatchRunning);
                updateStopwatchValue(timeElapsed);
            }
        };
        registerReceiver(statusReceiver, statusFilter);

        // Receiving time values from service
        IntentFilter timeFilter = new IntentFilter();
        timeFilter.addAction(TimerService.STOPWATCH_TICK);
        timeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int timeElapsed = intent.getIntExtra(TimerService.TIME_ELAPSED, 0) + 1;
                updateStopwatchValue(timeElapsed);
            }
        };
        registerReceiver(timeReceiver, timeFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(statusReceiver);
        unregisterReceiver(timeReceiver);

        // Moving the service to foreground when the app is in background / not visible
        moveToForeground();
    }

    @SuppressLint("SetTextI18n")
    private void updateStopwatchValue(int timeElapsed) {
        int minutes = Constant.reminderDuration - timeElapsed / 60;
        int seconds = 60 - (timeElapsed % 60 + 1);

        Log.d(">>>", "updateStopwatchValue: " + timeElapsed/60 + Constant.reminderDuration + seconds );
        remainContainer.setVisibility(View.VISIBLE);
        tvRemain.setText(showNumberWithZero(minutes)+ ":" + showNumberWithZero(seconds));

        if (minutes == 0 && seconds == 0){
            remainContainer.setVisibility(View.GONE);
            pauseStopwatch();
        }
    }

    private String showNumberWithZero(int i){
        String countdownSec;
        if (i < 10){
            countdownSec = "0"+i;
        }else {
            countdownSec = String.valueOf(i);
        }
        return countdownSec;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        remainContainer = findViewById(R.id.frameLayout);
        listData = new ArrayList<>();
        setUPData();
        expList             = findViewById(R.id.lvExpBal);
//        adapter = new RecyclerViewAdapter();
        List<Item> items = new ArrayList<>();

//        items.add(new Item(0, null));
//        items.add(new Item(1, new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)", "7:00 AM", "7:30 AM", false,0)));
//        items.add(new Item(1, new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)","7:00 AM", "7:30 AM", false,0)));
//        items.add(new Item(1, new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)","7:00 AM", "7:30 AM", false,0)));
//
//        items.add(new Item(0, null));
//        items.add(new Item(1, new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)","7:00 AM", "7:30 AM", false,0)));
//        items.add(new Item(1, new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)","7:00 AM", "7:30 AM", false,0)));
//
//        items.add(new Item(0, null));
//        items.add(new Item(1, new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)","7:00 AM", "7:30 AM", false,0)));
//        items.add(new Item(1, new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)","7:00 AM", "7:30 AM", false,0)));
//
//        items.add(new Item(0, null));
//        items.add(new Item(1, new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)","7:00 AM", "7:30 AM", false,0)));
//        items.add(new Item(1, new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)","7:00 AM", "7:30 AM", false,0)));
//        ArrayList<Meeting> meetings1 = new ArrayList<>();
//        meetings1.add(new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)", false,0));
//        meetings1.add(new Meeting(ContextCompat.getColor(this,R.color.color_60DF6B), "Mekong Room", "Session sharing", false,0));
//        meetings1.add(new Meeting(ContextCompat.getColor(this,R.color.color_FA5B70), "Mekong Room", "Session sharing", false,0));
//        adapter.setItems(items);
//        expList.setAdapter(adapter);

        expList.setFloatingGroupEnabled(false);
        expandableAdapter = new ExpAdapter(this, listData);
        WrapperExpandableListAdapter adapter = new WrapperExpandableListAdapter(expandableAdapter);
        expList.setAdapter(adapter);

        for(int i = 0; i< expandableAdapter.getGroupCount(); i++){
            expList.expandGroup(i);
        }

        findViewById(R.id.flBottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

//        getRealtimeLocal();

        tvRemain = findViewById(R.id.tv_remain);

//        new CountDownTimer(15*60000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                int countDownSec = 60 - ((int) (15 * 60) - (int) millisUntilFinished / 1000);
//                if (((int) (15 * 60) - (int) millisUntilFinished / 1000) > 60){
//                    countDownSec = 60 - ((int) (15 * 60) - (int) millisUntilFinished / 1000) % 60;
//                }
//
//                Log.d(">>>", "onTick: " + (int) millisUntilFinished / 1000);
//                tvRemain.setText(String.format("%s:%s", String.valueOf((int) millisUntilFinished / 1000 / 60 ),countDownSec));
//            }
//
//            @Override
//            public void onFinish() {
//                //End the game or do whatever you want.
//            }
//        }.start();

        final Handler hander = new Handler();
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hander.post(() -> getTime());
        }).start();

        clock();

    }


    private void clock() {
        final Handler hander = new Handler();
        new Thread(() -> {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hander.post(() -> {
                getTime();
                clock();
            });
        }).start();
    }

    boolean isMeetingStartSoon = false;
    private void checkIfMeetingTimeUp(int h, int min){
        SharedPreferences preferences = getSharedPreferences("timer", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < listData.size(); i++) {
            for (int j = 0; j < listData.get(i).getMeetings().size(); j++) {
                if (listData.get(i).getMeetings().get(j).getStartTime() != null) {

                    int h1 = Integer.parseInt(listData.get(i).getMeetings().get(j).getStartTime().split(" ")[0].split(":")[0]);
                    int min1 = Integer.parseInt(listData.get(i).getMeetings().get(j).getStartTime().split(" ")[0].split(":")[1]) - 1;
                    Log.i(">>", "checkIfMeetingTimeUp: " + h + " : " + h1);
                    if (h1 - h == 1) {
                        int hour = h1 - h;
                        if (min > min1) {
                            Log.i(">>", "checkIfMeetingTimeUp: " + min1 + (hour * 60 - min) + " min remain left before meeting start");
                            if (min1 + (hour * 60 - min) <= 15){
                                Log.i(">>", "checkIfMeetingTimeUp: " + preferences.contains("isRunning"));
                                if (!preferences.contains("isRunning") && !preferences.getBoolean("isRunning", false)){
                                    Constant.reminderDuration = min1 + (hour * 60 - min);
                                    editor.putInt("remainMinute", min1 + (hour * 60 - min)).apply();
                                    editor.putBoolean("isRunning", true).apply();
                                    startStopwatch();
                                }
                                return;
                            }
                        }
                    }
                    if (h == h1) {
                        if (min < min1) {
                            Log.i(">>", "checkIfMeetingTimeUp: " + (min1 - min) + " min remain left before meeting start");
                            if (min1 - min <= 15){
                                Log.i(">>", "checkIfMeetingTimeUp: " + preferences.contains("isRunning"));
                                if (!preferences.contains("isRunning") && !preferences.getBoolean("isRunning", false)){
                                    Constant.reminderDuration = min1 - min;
                                    editor.putInt("remainMinute", min1 - min).apply();
                                    editor.putBoolean("isRunning", true).apply();
                                    startStopwatch();
                                }
                                return;
                            }
                        }
                    }

                }
            }

        }

    }
    
    void getTime() {
        Date d=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss a");
        currentDateTimeString = sdf.format(d);

        int systemHour = Integer.parseInt(currentDateTimeString.split(":")[0]);
        int sysMin = Integer.parseInt(currentDateTimeString.split(":")[1]);
        int minOfHeight = 0;

        for (int i = 0; i < listData.size(); i++) {
            for (int j = 0; j < listData.get(i).getMeetings().size(); j++) {
                if (listData.get(i).getMeetings().get(j) != null) {
                    listData.get(i).getMeetings().get(j).setMovePosition(0);
                    listData.get(i).getMeetings().get(j).setNowBarShowed(false);
                }
            }
            expandableAdapter.notifyDataSetChanged();
        }

        for (int i = 0; i < listData.size(); i++) {
            if (listData.get(i).getMeetings().size() != 0){
                minOfHeight = 60 / listData.get(i).getMeetings().size();
                int parentHour = Integer.parseInt(listData.get(i).getDate().split(":")[0]);
                if (parentHour == systemHour){

                    Log.d(">>>", "getTime: " + smoothScrollToPosition + listData.get(i).getDate());

                    for (int j = 0; j < listData.get(i).getMeetings().size(); j++) {

                        if (listData.get(i).getMeetings().get(j).getRoom() == null && listData.get(i).getMeetings().get(j).getStartTime() == null){
                            listData.get(i).getMeetings().get(j).setMovePosition(0);
                            listData.get(i).getMeetings().get(j).setNowBarShowed(true);
                            expandableAdapter.notifyDataSetChanged();
                            break;
                        }

                        View childItem = expList.getChildAt(j+1);
                        if (j == sysMin / minOfHeight ){

                            Log.d(">>>", "getChildView:: if it's null >>> " + childItem);
                            if (childItem == null){
                                return;
                            }
                            Log.d(">>>", "getChildView:: " + childItem + childItem.getMeasuredHeight());
                            LinearLayout nowLabel = childItem.findViewById(R.id.fl_now);

                            if (nowLabel != null){
                                maxChildHeight = childItem.getMeasuredHeight() - nowLabel.getMeasuredHeight();
                                Log.d(">>>", "getChildView: NOW bar : " + nowLabel.getMeasuredHeight());
                            }

                            int minNow;
                            if (sysMin == minOfHeight - 1){
                                minNow = sysMin + 1;
                            }
                            else{
                                minNow = sysMin - minOfHeight * (sysMin / minOfHeight);
                                if (minNow == minOfHeight-1){
                                    minNow = minNow + 1;
                                }
                            }
                            Log.d(">>>", "get: min now >> " + minNow);
                            position = (minNow) * maxChildHeight / (minOfHeight);
                            if (position   < 0) {
                                position = position * -1;
                            }

                            smoothScrollToPosition = j;

                            listData.get(i).getMeetings().get(j).setMovePosition((int) position);
                            listData.get(i).getMeetings().get(j).setNowBarShowed(true);
                            expandableAdapter.notifyDataSetChanged();
                            break;
                        }
                        Log.d(">>>", "getChildView: " + listData.get(i).getMeetings().get(j).getNowBarShowed() + listData.get(i).getMeetings().get(j).getMovePosition());
                    }

                }
            }

        }

        checkIfMeetingTimeUp(systemHour, sysMin);
    }

    private float px2dp(int pxVal) {
        float density = getResources().getDisplayMetrics().density;

        return (pxVal/ density);
    }

    private void setUPData(){

        ArrayList<Meeting> meetings = new ArrayList<>();
        meetings.add(new Meeting());
        listData.add(new Data("6:00 AM", meetings));

        ArrayList<Meeting> meetings1 = new ArrayList<>();
        meetings1.add(new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Angkor Room", "Session sharing(AWS)","7:00 AM", "7:30 AM", false,0));
        meetings1.add(new Meeting(ContextCompat.getColor(this,R.color.color_60DF6B), "Mekong Room", "Session sharing","7:30 AM", "8:00 AM", false,0));
        meetings1.add(new Meeting(ContextCompat.getColor(this,R.color.color_FA5B70), "Mekong Room", "Session sharing","7:30 AM", "7:45 AM", false,0));
        listData.add(new Data("7:00 AM", meetings1));

        ArrayList<Meeting> meetings2 = new ArrayList<>();
        meetings2.add(new Meeting(ContextCompat.getColor(this,R.color.color_49AAFA), "Angkor Room", "Weekly report","8:00 AM", "8:30 AM", false,0));
        listData.add(new Data("8:00 AM", meetings2));

        ArrayList<Meeting> meetings3 = new ArrayList<>();
        meetings3.add(new Meeting(ContextCompat.getColor(this,R.color.color_FA7240), "Tonle sap Room", "Weekly report","9:00 AM", "9:30 AM", false,0));
        meetings3.add(new Meeting(ContextCompat.getColor(this,R.color.color_FED93A), "Mekong Room", "Communication day", "9:30 AM", "10:30 AM",false,0));
        listData.add(new Data("9:00 AM", meetings3));

        ArrayList<Meeting> meetings4 = new ArrayList<>();
        meetings4.add(new Meeting(ContextCompat.getColor(this,R.color.color_FA5B70), "Angkor Room", "Session sharing(AWS)", "10:00 AM", "10:30 AM",false,0));
        meetings4.add(new Meeting(ContextCompat.getColor(this,R.color.color_FCB131), "Mekong Room", "Session sharing", "10:00 AM", "10:30 AM",false,0));
        meetings4.add(new Meeting(ContextCompat.getColor(this,R.color.color_60DF6B), "Phnom penh Room", "Session sharing(AWS)","10:30 AM", "11:30 AM", false,0));
        listData.add(new Data("10:00 AM", meetings4));

        ArrayList<Meeting> meetings5 = new ArrayList<>();
        meetings5.add(new Meeting(ContextCompat.getColor(this,R.color.color_A9AAAE), "Angkor Room", "Weekly report","11:00 AM", "11:30 AM", false,0));
        meetings5.add(new Meeting(ContextCompat.getColor(this,R.color.color_43C0EE), "Seim reap Room", "Session sharing","11:00 AM", "11:30 AM", false,0));
        listData.add(new Data("11:00 AM", meetings5));

        ArrayList<Meeting> meetings6 = new ArrayList<>();
        meetings6.add(new Meeting(ContextCompat.getColor(this,R.color.color_49AAFA), "Angkor Room", "Weekly report","12:00 AM", "12:30 AM",false,0));
        listData.add(new Data("12:00 AM", meetings6));

        ArrayList<Meeting> meetings7 = new ArrayList<>();
        meetings7.add(new Meeting(ContextCompat.getColor(this,R.color.color_FA7240), "Tonle sap Room", "Weekly report","1:00 AM", "1:30 AM", false,0));
        meetings7.add(new Meeting(ContextCompat.getColor(this,R.color.color_FED93A), "Mekong Room", "Communication day","1:00 AM", "1:30 AM", false,0));
        listData.add(new Data("1:00 PM", meetings7));

        ArrayList<Meeting> meetings8 = new ArrayList<>();
        meetings8.add(new Meeting(ContextCompat.getColor(this,R.color.color_FA5B70), "Angkor Room", "Session sharing(AWS)","2:00 AM", "2:30 AM", false,0));
        meetings8.add(new Meeting(ContextCompat.getColor(this,R.color.color_FCB131), "Mekong Room", "Session sharing","2:00 AM", "7:30 AM", false,0));
        meetings8.add(new Meeting(ContextCompat.getColor(this,R.color.color_60DF6B), "Phnom penh Room", "Session sharing(AWS)","2:10 AM", "2:30 AM", false,0));
        listData.add(new Data("2:00 PM", meetings8));

        ArrayList<Meeting> meetings9 = new ArrayList<>();
        meetings9.add(new Meeting());
        listData.add(new Data("3:00 PM", meetings9));

        ArrayList<Meeting> meetings10 = new ArrayList<>();
        meetings10.add(new Meeting());
        listData.add(new Data("4:00 PM", meetings10));

        ArrayList<Meeting> meetings11 = new ArrayList<>();
        meetings11.add(new Meeting(ContextCompat.getColor(this,R.color.color_FA5B70), "Angkor Room", "Session sharing(AWS)","5:00 PM", "5:30 PM", false,0));
        meetings11.add(new Meeting(ContextCompat.getColor(this,R.color.color_FCB131), "Mekong Room", "Session sharing","5:00 PM", "5:30 PM", false,0));
        meetings11.add(new Meeting(ContextCompat.getColor(this,R.color.color_60DF6B), "Phnom penh Room", "Session sharing(AWS)","5:10 PM", "4:30 PM", false,0));
        listData.add(new Data("5:00 PM", meetings11));

        ArrayList<Meeting> meetings12 = new ArrayList<>();
        meetings12.add(new Meeting());
        listData.add(new Data("6:00 PM", meetings12));

    }

}