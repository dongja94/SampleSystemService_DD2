package com.begentgroup.samplesystemservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    SensorManager mSM;
    Sensor mRotationVector;
    Sensor mLinearAcc;


    NotificationManager mNM;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSM = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mRotationVector = mSM.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mLinearAcc = mSM.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mNM = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Button btn = (Button)findViewById(R.id.btn_send);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotification();
            }
        });
    }

    private int id = 0;
    int messageCount = 0;
    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info);
        builder.setTicker("sample notification");
        builder.setContentTitle("notification title" + messageCount);
        builder.setContentText("notification test..." + messageCount);
        builder.setWhen(System.currentTimeMillis());

        Intent intent = new Intent(this, NotifyActivity.class);
        intent.setData(Uri.parse("myscheme://" + getPackageName() + "/" + id));
        intent.putExtra("count", messageCount);
        intent.putExtra("id", id);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setAutoCancel(true);

        Intent cancelIntent= new Intent(this, CancelService.class);
        PendingIntent cancelpi = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(cancelpi);

        Notification notification = builder.build();

        mNM.notify(id, notification);
        id++;
        messageCount++;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSM.registerListener(mListener, mRotationVector, SensorManager.SENSOR_DELAY_GAME);
        mSM.registerListener(mListener, mLinearAcc, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSM.unregisterListener(mListener);
    }

    protected void onShake() {

    }

    float[] mR = new float[9];
    float[] mOrientation = new float[3];
    float oldX = 0;
    private static final float DELTA = 0.5f;
    int count = 0;
    private static final int THREAHOLD = 3;
    private static final int MESSAGE_SHAKE_TIMEOUT = 1;
    private static final int TIMEOUT_SHAKE = 1000;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_SHAKE_TIMEOUT :
                    count = 0;
                    break;
            }
        }
    };

    SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ROTATION_VECTOR :
                    SensorManager.getRotationMatrixFromVector(mR, event.values );
                    SensorManager.getOrientation(mR, mOrientation);
                    float direction = (float)Math.toDegrees(mOrientation[0]);
                    Log.i("MainActivity", "d : " + direction);
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION :
                    float x = event.values[0];
                    float diff = (x - oldX);
                    if (Math.abs(diff) > DELTA && x * oldX < 0) {
                        mHandler.removeMessages(MESSAGE_SHAKE_TIMEOUT);
                        count++;
                        if (count >THREAHOLD) {
                            onShake();
                            count = 0;
                        } else {
                            mHandler.sendEmptyMessageDelayed(MESSAGE_SHAKE_TIMEOUT, TIMEOUT_SHAKE);
                        }
                    }
                    oldX = x;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
}
