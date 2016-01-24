package com.alarmmanagerexample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    EditText hoursEditText;
    EditText minutesEditText;
    EditText alarmNumberEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hoursEditText = (EditText) findViewById(R.id.hours);
        minutesEditText = (EditText) findViewById(R.id.minutes);
        alarmNumberEditText = (EditText) findViewById(R.id.alarmNumber);

        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

    }

    public void setAlarm(View view) {

        int hours = Integer.parseInt(hoursEditText.getText().toString());
        int minutes = Integer.parseInt(minutesEditText.getText().toString());

        MyDatabase myDatabase = new MyDatabase(this);
        SQLiteDatabase db = myDatabase.getWritableDatabase();

        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, (int) DatabaseUtils.queryNumEntries(db, "alarms"), intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
// With setInexactRepeating(), you have to use one of the AlarmManager interval
// constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

        ContentValues values = new ContentValues();
        values.put("alarm_time", calendar.getTimeInMillis());

        db.insert("alarms", null, values);
        db.close();


        Toast.makeText(MainActivity.this, "Alarm Set for Time: " + calendar.getTime().toString(), Toast.LENGTH_SHORT).show();
    }
}
