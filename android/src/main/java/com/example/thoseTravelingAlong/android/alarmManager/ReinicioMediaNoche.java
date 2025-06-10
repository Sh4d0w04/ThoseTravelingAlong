package com.example.thoseTravelingAlong.android.alarmManager;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.thoseTravelingAlong.android.workManager.BBDDWorker;

import java.util.concurrent.TimeUnit;

public class ReinicioMediaNoche extends BroadcastReceiver{
    private int steps;

    @Override
    public void onReceive(Context context, Intent intent) {
        WorkManager workManager = WorkManager.getInstance(context);
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(BBDDWorker.class, 24, TimeUnit.HOURS).build();

        workManager.enqueueUniquePeriodicWork("BBDD periodico", ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, periodicWorkRequest);
    }
}
