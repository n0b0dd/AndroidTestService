package com.kosign.reminderdemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TimerClass {
    private static final String TAG = "MyLog";
    private static final long TIMERINTERVAL = 1000;
    //Eigenschaften --------------------------------------------------------------------
    private boolean isActiv = false;
    private Paint paint;
    private long timerTime;
    private double actualTime;
    private int x;
    private int y;
    private CountDownTimer countDownTimer;
    private String timeString;

    //Konstruktor --------------------------------------------------------------------
    public TimerClass(int x, int y) {
        this.x = x;
        this.y = y;
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.YELLOW);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(70);
    }

    public TimerClass() {

    }

    //Methoden --------------------------------------------------------------------
    public void draw(Canvas canvas) {
        if (isActiv) {
            canvas.drawText(timeString, x, y, paint);
        }
    }

    public void createTimer(long timerTime) {
        this.timerTime = timerTime;
        countDownTimer = new CountDownTimer(timerTime, TIMERINTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {

                actualTime = (double) millisUntilFinished / TIMERINTERVAL;
                NumberFormat nf = new DecimalFormat("##.#");
                nf.format(actualTime);

                if (actualTime < 10) {
                    new DecimalFormat();
                    timeString = "0:0" + nf.format(actualTime - 1);
                } else {
                    timeString = "0:" + nf.format(actualTime - 1);
                }
            }

            @Override
            public void onFinish() {
                isActiv = false;
            }
        };

    }

    public void timerStart() {
        countDownTimer.start();
        isActiv = true;
    }

    public void timerStop() {
        if (isActiv) {
            countDownTimer.cancel();
        }
        isActiv = false;

    }

    public boolean isActiv() {
        return isActiv;
    }

    public void setActiv(boolean isActiv) {
        this.isActiv = isActiv;
    }

    public double getActualTime() {
        return actualTime;
    }

    public void setActualTime(double actualTime) {
        this.actualTime = actualTime;
    }

}
