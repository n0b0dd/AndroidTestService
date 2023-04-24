package com.kosign.reminderdemo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {

    // Channel ID for notifications
    String CHANNEL_ID = "RemainTime_Notifications";

    // Service Actions
    static String START = "START";
    static String PAUSE = "PAUSE";
    static String RESET = "RESET";
    static String GET_STATUS = "GET_STATUS";
    static String MOVE_TO_FOREGROUND = "MOVE_TO_FOREGROUND";
    static String MOVE_TO_BACKGROUND = "MOVE_TO_BACKGROUND";

    // Intent Extras
    static String STOPWATCH_ACTION = "STOPWATCH_ACTION";
    static String TIME_ELAPSED = "TIME_ELAPSED";
    static String IS_STOPWATCH_RUNNING = "IS_STOPWATCH_RUNNING";

    // Intent Actions
    static String STOPWATCH_TICK = "STOPWATCH_TICK";
    static String STOPWATCH_STATUS = "STOPWATCH_STATUS";

    private int remainMin = 1;
    private int timeElapsed = 0;
    private boolean isStopWatchRunning = false;

    private Timer updateTimer = new Timer();
    private Timer stopwatchTimer = new Timer();

    // Getting access to the NotificationManager
    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createChannel();
        getNotificationManager();

        String action = intent.getStringExtra(STOPWATCH_ACTION);

                Log.d("Stopwatch", "onStartCommand Action: $action");

        if (action.equals("START")){
            startStopwatch();
        }else if (action.equals("PAUSE")){
            pauseStopwatch();
        }
        else if (action.equals("GET_STATUS")){
            sendStatus();
        }
        else if (action.equals("MOVE_TO_FOREGROUND")){
            moveToForeground();
        }
        else if (action.equals("MOVE_TO_BACKGROUND")){
            moveToBackground();
        }

        return START_STICKY;
    }

    /*
     * This function is triggered when the app is not visible to the user anymore
     * It check if the stopwatch is running, if it is then it starts a foreground service
     * with the notification.
     * We run another timer to update the notification every second.
     * */
    private void moveToForeground() {

        if (isStopWatchRunning) {
            startForeground(1, buildNotification());

            updateTimer = new Timer();

            updateTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateNotification();
                }
            },0,1000);
        }
    }

    private void pauseStopwatch() {
        stopwatchTimer.cancel();
        isStopWatchRunning = false;
        timeElapsed = 0;
        SharedPreferences preferences = getSharedPreferences("timer", MODE_PRIVATE);
        preferences.edit().remove("isRunning").apply();
        preferences.edit().remove("remainMinute").apply();
        sendStatus();
    }

    /*
     * This function is triggered when the app is visible again to the user
     * It cancels the timer which was updating the notification every second
     * It also stops the foreground service and removes the notification
     * */
    private void moveToBackground() {
        updateTimer.cancel();
        stopForeground(true);
    }

    /*
     * This function starts the stopwatch
     * Sets the status of stopwatch running to true
     * We start a Timer and increase the timeElapsed by 1 every second and broadcast the value
     * with the action of STOPWATCH_TICK.
     * We will receive this broadcast in the MainActivity to get access to the time elapsed.
     * */
    private void startStopwatch() {
        isStopWatchRunning = true;

        sendStatus();

        stopwatchTimer = new Timer();
        stopwatchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Intent stopwatchIntent = new Intent();
                stopwatchIntent.setAction(STOPWATCH_TICK);

                timeElapsed++;

                stopwatchIntent.putExtra(TIME_ELAPSED, timeElapsed);
                sendBroadcast(stopwatchIntent);
            }
        },0, 1000);
    }

    /*
     * This function is responsible for broadcasting the status of the stopwatch
     * Broadcasts if the stopwatch is running and also the time elapsed
     * */
    private void sendStatus() {
        Intent statusIntent = new Intent();
        statusIntent.setAction(STOPWATCH_STATUS);
        statusIntent.putExtra(IS_STOPWATCH_RUNNING, isStopWatchRunning);
        statusIntent.putExtra(TIME_ELAPSED, timeElapsed);
        sendBroadcast(statusIntent);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "WEMeet",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.setSound(null, null);
            notificationChannel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void getNotificationManager() {
        notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager.class
        );
    }

    /*
     * This function is responsible for building and returning a Notification with the current
     * state of the stopwatch along with the timeElapsed
     * */
    private Notification buildNotification() {
        String title = "";
        if (isStopWatchRunning) {
            title = "Meeting start soon!";
        } else {
            title = "Meeting already to started!!";
        }

        String message = "";
        int minutes = Constant.reminderDuration - timeElapsed / 60;
        int seconds = 60 - (timeElapsed % 60 + 1);
        message = showNumberWithZero(minutes) + ":" + showNumberWithZero(seconds);
        Log.i(">>>", "buildNotification: " + timeElapsed % 60 + minutes + seconds );
        if (minutes == 0 && seconds == 0){
            message = "Let's meeting up";
            pauseStopwatch();
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setOngoing(true)
                .setContentText(message)
                .setColorized(true)
                .setColor(Color.parseColor("#757BEE"))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setOnlyAlertOnce(true)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();
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

    /*
     * This function uses the notificationManager to update the existing notification with the new notification
     * */
    private void updateNotification() {
        notificationManager.notify(
                1,
                buildNotification()
        );
    }

}