package com.alarmmanagerexample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.Calendar;

public class ResetAlarms extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            MyDatabase myDatabase = new MyDatabase(context);
            SQLiteDatabase db = myDatabase.getReadableDatabase();

            Intent receiverIntent = new Intent(context, AlarmReceiver.class);
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Cursor cursor = db.rawQuery("SELECT * FROM alarms", null);

            if (cursor.moveToFirst()){
                do {
                    int cursorId = cursor.getInt(0);
                    int alarm_time = cursor.getInt(1);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(alarm_time);

                    System.out.println("Alarm ID: " + cursorId);
                    System.out.println("Alarm Time: " + calendar.getTime().toString());

                    alarmIntent = PendingIntent.getBroadcast(context, cursorId, receiverIntent, 0);
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

                }while (cursor.moveToNext());
            }

            db.close();

            Toast.makeText(context, "Resetting Alarms!", Toast.LENGTH_SHORT).show();
        }
    }
}