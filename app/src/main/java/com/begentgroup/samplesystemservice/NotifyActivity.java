package com.begentgroup.samplesystemservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class NotifyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        Intent intent = getIntent();
        int count = intent.getIntExtra("count", 0);
        TextView tv = (TextView)findViewById(R.id.text_count);
        tv.setText("" + count);

//        int id = intent.getIntExtra("id", -1);
//        if (id >= 0) {
//            NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//            nm.cancel(id);
//        }
    }
}
