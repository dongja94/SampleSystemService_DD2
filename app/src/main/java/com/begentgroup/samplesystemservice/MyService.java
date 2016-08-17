package com.begentgroup.samplesystemservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    AlarmManager mAM;

    @Override
    public void onCreate() {
        super.onCreate();
        mAM = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long current = System.currentTimeMillis();
        List<MyData> processList = DataManager.getInstance().listProcessData(current);
        for (MyData d : processList) {
            Log.i("MyService", "message : " + d.message);
        }
        for (MyData d : processList) {
            if (!d.repeat) {
                DataManager.getInstance().removeMyData(d);
            } else {
                d.time += d.interval;
                DataManager.getInstance().updateMyData(d);
            }
        }

        MyData alarmData = DataManager.getInstance().getAlarmData();
        Intent i = new Intent(this, MyService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        mAM.set(AlarmManager.RTC_WAKEUP, alarmData.time, pi);
        return Service.START_NOT_STICKY;
    }
}
