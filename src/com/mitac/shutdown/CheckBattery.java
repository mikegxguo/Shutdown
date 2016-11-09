package com.mitac.shutdown;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.SystemClock;
//import android.widget.Toast;
import android.telephony.PhoneStateListener;
import android.util.Log;

public class CheckBattery extends Service {

    private KeyguardManager keyguardManager;
    private String lockTag;
    private KeyguardManager.KeyguardLock keyguardLock;

    public static final String CHECK_BATTERY_ALARM = "android.intent.action.CHECK_BATTERY_ALARM";
    private IntentFilter mIntentFilter;
    private AlarmManager am;
    private PendingIntent pi;
    private static String TAG = "CheckBattery";
    private int mBatteryLevel;
    private int mPlugged;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE); 
        lockTag =  "CheckBattery"; 
        keyguardLock = keyguardManager.newKeyguardLock(lockTag);
        keyguardLock.disableKeyguard(); 


        Intent intent = new Intent(CHECK_BATTERY_ALARM);
        intent.putExtra("msg", "CheckBattery");
        pi = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Calendar calendar=Calendar.getInstance();
        // calendar.setTimeInMillis(System.currentTimeMillis());
        // calendar.add(Calendar.SECOND, 60);

        int delay = 60 * 1000;
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pi);
        // am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+60*1000, pi);

        mIntentFilter = new IntentFilter();
        //mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mIntentFilter.addAction(CHECK_BATTERY_ALARM);
        registerReceiver(mIntentReceiver, mIntentFilter);
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
//                mBatteryLevel = intent.getIntExtra("level", 0);
//                mPlugged = intent.getIntExtra("plugged", 0);
//                if (mBatteryLevel < 1 && mPlugged != BatteryManager.BATTERY_PLUGGED_AC) {
//                    onShutdownThread();
//               }
//            } else 
            if (action.equals(CHECK_BATTERY_ALARM)) {
                    Log.d(TAG, "CHECK_BATTERY_ALARM");
                    onShutdownThread();
            }
        }
    };

    public void onShutdownThread() {
        Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public IBinder onBind(Intent intent) {
        //Toast.makeText(getApplicationContext(), "No message", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onBind");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override  
    public void onDestroy() {  
         Log.d(TAG, "onDestroy");
         am.cancel(pi);
         unregisterReceiver(mIntentReceiver);
         super.onDestroy();
    }
}
