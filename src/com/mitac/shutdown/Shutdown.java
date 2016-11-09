package com.mitac.shutdown;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Method;
import android.os.IBinder;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import android.os.IPowerManager;
//import android.os.ServiceManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.os.SystemProperties;

public class Shutdown extends Activity {

    // public static final String ACTION_REBOOT = "android.intent.action.REBOOT";
    // public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    public static final String PROPERTY_SHUTDOWN_REMAIN = "persist.sys.shutdown.remain";

    private KeyguardManager keyguardManager;
    private String lockTag;
    private KeyguardManager.KeyguardLock keyguardLock;

    private static String TAG = "Shutdown";
    private TextView mTextView01;
    private RadioGroup mRadioGroupAPI = null;
    private RadioButton mRadioButtonAPI1 = null;
    private static final int MSG_AUTO_REFRESH = 0x1000;
    private int counter;

    
    public void onShutdownThread() {
        Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.main);
        mTextView01 = (TextView) findViewById(R.id.myTextView1);
        mRadioGroupAPI = (RadioGroup) findViewById(R.id.radio_group_api);
        
        String time = SystemProperties.get(PROPERTY_SHUTDOWN_REMAIN, "60");
        counter = Integer.valueOf(time).intValue(); 
        int RADIO_ID = R.id.radio_api2;
        if(counter == 90) {
            RADIO_ID = R.id.radio_api1;
        } else if(counter == 60) {
            RADIO_ID = R.id.radio_api2;
        } else if(counter == 30) {
            RADIO_ID = R.id.radio_api3;
        } else if(counter == 10) {
            RADIO_ID = R.id.radio_api4;
        } else {
            RADIO_ID = R.id.radio_api2;//default 60 seconds
        }
        mRadioButtonAPI1 = (RadioButton) findViewById(RADIO_ID);
        mRadioButtonAPI1.toggle();
        mRadioGroupAPI.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {              
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if(checkedId==R.id.radio_api1){
                        counter = 90;
                        Toast.makeText(Shutdown.this, "90 seconds", Toast.LENGTH_SHORT).show();
                }  else if(checkedId==R.id.radio_api2){
                        counter = 60;
                        Toast.makeText(Shutdown.this, "60 seconds", Toast.LENGTH_SHORT).show();
                }  else if(checkedId==R.id.radio_api3) {
                        counter = 30;
                        Toast.makeText(Shutdown.this, "30 seconds", Toast.LENGTH_SHORT).show();
                } else {
                        counter = 10;
                        Toast.makeText(Shutdown.this, "10 seconds", Toast.LENGTH_SHORT).show();
                }
                SystemProperties.set(PROPERTY_SHUTDOWN_REMAIN, Integer.toString(counter));
            }
        });
        
        keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE); 
        lockTag =  "Shutdown"; 
        keyguardLock = keyguardManager.newKeyguardLock(lockTag);
        keyguardLock.disableKeyguard(); 

       
        if(mHandler != null) {
                    mHandler.postDelayed(runnable, 1000);
        }
   }
 
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_AUTO_REFRESH:
                --counter;
                mTextView01.setText(" "+counter);
                if(counter == 0) {
                    onShutdownThread();
                }
                break;
            }
        }
    };
    
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
                if(counter > 0) {
                        mHandler.obtainMessage(MSG_AUTO_REFRESH, null).sendToTarget();
                        mHandler.postDelayed(this, 1000);
                }
        }
    };
    
    @Override  
    public void onDestroy() {  
        Log.d(TAG, "onDestroy");
         super.onDestroy();
    }

    public void onCancel(View v) {
        mHandler.removeCallbacks(runnable);        
    }
    
    
    public void onShutDown(View v) {
            Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
            intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        
//        try {
//            Process proc = Runtime.getRuntime()
//                    .exec(new String[] { "su", "-c", "reboot -p" });
//            proc.waitFor();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        
//         Intent intent2 = new Intent(Intent.ACTION_REBOOT);
//         intent2.putExtra("nowait", 1); intent2.putExtra("interval", 1);
//         intent2.putExtra("window", 0); sendBroadcast(intent2);

        
//          try{ //Load classes and objects
//                  Object power; 
//                  Context fContext = getApplicationContext(); 
//                  Class <?>   ServiceManager = Class.forName("android.os.ServiceManager"); 
//                  Class<?> Stub = Class.forName("android.os.IPowerManager$Stub");
//
//                  Method getService = ServiceManager.getMethod("getService", new Class[] {String.class}); 
//                  //Method asInterface =   GetStub.getMethod("asInterface", new Class[] {IBinder.class});//of   this class? 
//                  Method asInterface = Stub.getMethod("asInterface", new Class[] {IBinder.class}); //of this class? 
//                  IBinder iBinder =          (IBinder) getService.invoke(null, new Object[] {Context.POWER_SERVICE});
//                  //power =  asInterface.invoke(null,iBinder);//or call constructor Stub?//
//                  
//                  Method shutdown = power.getClass().getMethod("shutdown", new Class[]{boolean.class, boolean.class});
//                  
//                  int Brightness = 5; 
//                  shutdown.invoke(false, false); // Method setBacklightBrightness.in 
//                  //log("Load internal IPower classes Ok");
//          }catch(InvocationTargetException e) { 
//                  // IPowerManager powerManager =  IPowerManager.Stub.asInterface( //
//                  ServiceManager.getService(Context.POWER_SERVICE)); 
//          } catch (ClassNotFoundException e) {
//                  e.printStackTrace(); 
//          } catch(NoSuchMethodException e) { 
//                  e.printStackTrace(); 
//          } catch  (IllegalAccessException e) { 
//                  e.printStackTrace();
//          }
         

//         PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
//         pm.reboot("shutdown");

//          IPowerManager powerManager = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
//          try {
//                  powerManager.shutdown(false, false); 
//          } catch (RemoteException e) { }
 
//        try {
//            Class<?> ServiceManager = Class
//                    .forName("android.os.ServiceManager");
//            Method getService = ServiceManager.getMethod("getService",
//                    java.lang.String.class);
//            Object oRemoteService = getService.invoke(null,
//                    Context.POWER_SERVICE);
//            Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
//            Method asInterface = cStub.getMethod("asInterface",
//                    android.os.IBinder.class);
//            Object oIPowerManager = asInterface.invoke(null, oRemoteService);
//            Method shutdown = oIPowerManager.getClass().getMethod("shutdown",
//                    boolean.class, boolean.class);
//            shutdown.invoke(oIPowerManager, false, true);
//        } catch (Exception e) {
//            Log.e(LOG_TAG, e.toString(), e);
//        }

    }

}
