package com.alarmmanagerexample;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmScheduler extends BroadcastReceiver {
    public AlarmScheduler() {
    }

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private PrayTimeCalculation prayers;
    private ArrayList<String> prayerTimes;
    private ArrayList<String> prayerNames;
    double latitude = 24.8600;
    double longitude = 67.0100;
    double timezone = +5;
    private SharedPreferences sharedPreferences;
    private Calendar cal;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Toast.makeText(context, "Alarm Scheduler", Toast.LENGTH_SHORT).show();

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        prayers = new PrayTimeCalculation();

        prayers.setTimeFormat(prayers.Time12);
        prayers.setCalcMethod(prayers.Jafari);
        prayers.setAsrJuristic(prayers.Shafii);
        prayers.setAdjustHighLats(prayers.AngleBased);
        int[] offsets = {0, 0, 0, 0, 0, 0, 0}; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
        prayers.tune(offsets);

        Date now = new Date(System.currentTimeMillis());
        cal = Calendar.getInstance();
        cal.setTime(now);

        String time = (cal.get(Calendar.DAY_OF_MONTH)) + "/" + cal.get(Calendar.MONTH) + 1 + "/" + cal.get(Calendar.YEAR);

        prayerTimes = prayers.getPrayerTimes(cal, latitude, longitude, timezone);
        prayerNames = prayers.getTimeNames();

        prayerTimes.remove(4);
        prayerNames.remove(4);

        MyDatabase myDatabase = new MyDatabase(context);
        SQLiteDatabase db = myDatabase.getWritableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.US);

        db.delete("alarms", null, null);

        for (int i = 0; i < prayerNames.size(); i++) {

            System.out.println(prayerTimes.get(i));

            try {
                Date alarmTime = sdf.parse(time + " " + prayerTimes.get(i));
                Date currentTime = new Date(System.currentTimeMillis());
                System.out.println("Date: " + alarmTime.toString());

                if (alarmTime.after(currentTime)){
                    int alarmID = i + 1;

                    long alarmTimeLong = alarmTime.getTime();
                    String alarmName = prayerNames.get(i);

                    Intent receiverIntent = new Intent(context, AlarmReceiver.class);
                    intent.putExtra("ALARM_ID", alarmID);
                    intent.putExtra("ALARM_TIME", alarmTimeLong);
                    intent.putExtra("ALARM_NAME", alarmName);
                    alarmIntent = PendingIntent.getBroadcast(context, alarmID, receiverIntent, 0);
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTimeLong, alarmIntent);

                    ContentValues values = new ContentValues();
                    values.put("id", alarmID);
                    values.put("alarm_time", alarmTimeLong);
                    values.put("alarm_name", alarmName);
                    db.insert("alarms", null, values);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }
}
