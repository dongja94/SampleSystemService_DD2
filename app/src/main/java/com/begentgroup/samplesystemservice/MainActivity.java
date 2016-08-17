package com.begentgroup.samplesystemservice;

import android.app.AlarmManager;
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
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    SensorManager mSM;
    Sensor mRotationVector;
    Sensor mLinearAcc;


    NotificationManager mNM;

    AlarmManager mAM;
    EditText messageView, timeView, intervalView;
    CheckBox repeatView;
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

        btn = (Button)findViewById(R.id.btn_progress);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress = 0;
                mHandler.post(progressRunnable);
            }
        });

        btn = (Button)findViewById(R.id.btn_style);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendStyle();
            }
        });

        mAM = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        messageView = (EditText)findViewById(R.id.edit_message);
        timeView = (EditText)findViewById(R.id.edit_time);
        intervalView = (EditText)findViewById(R.id.edit_interval);
        repeatView = (CheckBox)findViewById(R.id.check_repeat);
        btn = (Button)findViewById(R.id.btn_add);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyData d = new MyData();
                d.time = System.currentTimeMillis() + Long.parseLong(timeView.getText().toString()) * 1000;
                d.repeat = repeatView.isChecked();
                if (d.repeat) {
                    d.interval = Long.parseLong(intervalView.getText().toString()) * 1000;
                }
                d.message = messageView.getText().toString();
                DataManager.getInstance().addMyData(d);
                startService(new Intent(MainActivity.this, MyService.class));
            }
        });
    }


    private void sendStyle() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_email);
        builder.setTicker("download");
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("An intent to launch instead of posting the notification to the status bar. Only for use with extremely high-priority notifications demanding the user's immediate attention, such as an incoming phone call or alarm clock that the user has explicitly set to a particular time. If this facility is used for something else, please give the user an option to turn it off and use a normal notification, as this can be extremely disruptive.\n" +
                "\n" +
                "The system UI may choose to display a heads-up notification, instead of launching this intent, while the user is using the device.");
        builder.setContentTitle("big text");
        builder.setStyle(style);

        Intent preIntent = new Intent(this, CancelService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, preIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(android.R.drawable.ic_dialog_alert, "PREV", pi);

        mNM.notify(id, builder.build());
    }

    private void sendProgress(int progress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_email);
        builder.setTicker("download");
        builder.setContentTitle("file download : " + progress);
        builder.setProgress(100, progress, false);

        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setOngoing(true);
        builder.setOnlyAlertOnce(true);

        mNM.notify(PROGRESS_ID, builder.build());
    }

    private static final int PROGRESS_ID = 100;

    int progress = 0;
    Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (progress <= 100) {
                sendProgress(progress);
                progress += 10;
                mHandler.postDelayed(this, 500);
            } else {
                mNM.cancel(PROGRESS_ID);
            }
        }
    };

    private int id = 0;
    int messageCount = 0;
    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info);
        builder.setTicker("sample notification");
        builder.setContentTitle("notification title" + messageCount);
        builder.setContentText("notification test..." + messageCount);
        builder.setWhen(System.currentTimeMillis());

        Intent[] intents = new Intent[2];
        intents[0] = new Intent(this, MainActivity.class);
        intents[0].addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent intent = new Intent(this, NotifyActivity.class);
        intent.setData(Uri.parse("myscheme://" + getPackageName() + "/" + id));
        intent.putExtra("count", messageCount);
        intent.putExtra("id", id);
        intents[1] = intent;
        PendingIntent pi = PendingIntent.getActivities(this, 0, intents,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setAutoCancel(true);

        Intent cancelIntent= new Intent(this, CancelService.class);
        cancelIntent.putExtra("count", messageCount);
        PendingIntent cancelpi = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setDeleteIntent(cancelpi);

        Intent fullIntent= new Intent(this, NotifyActivity.class);
        PendingIntent fpi = PendingIntent.getActivity(this, 0, fullIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setFullScreenIntent(fpi, true);

        Notification notification = builder.build();

        mNM.notify(id, notification);
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
