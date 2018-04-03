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
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.view.View.OnKeyListener;
import android.view.KeyEvent;

import android.os.SystemProperties;

public class Shutdown extends Activity {

    // public static final String ACTION_REBOOT = "android.intent.action.REBOOT";
    // public static final String ACTION_REQUEST_SHUTDOWN = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
    public static final String PROPERTY_SHUTDOWN_DURATION = "persist.sys.shutdown.duration";
    public static final String PROPERTY_SHUTDOWN_COUNTER = "persist.sys.shutdown.counter";
    public static final String PROPERTY_SHUTDOWN_TOTAL = "persist.sys.shutdown.total";
    public static final String PROPERTY_SHUTDOWN_OPTION = "persist.sys.shutdown.option";

    private KeyguardManager keyguardManager;
    private String lockTag;
    private KeyguardManager.KeyguardLock keyguardLock;

    private static String TAG = "Shutdown";
    private TextView mTextView01;
    private TextView mCounter;
    private EditText mEditTotal;
    private Spinner spinner;
    private RadioGroup mRadioGroupAPI = null;
    private RadioButton mRadioButtonAPI1 = null;
    private Button mButtonStart;
    private static final int MSG_AUTO_REFRESH = 0x1000;
    private int duration;
    private int pos;
    private int counter;
    private int total;
    private int option;

    private IntentFilter mIntentFilter;
    private int mBatteryLevel;
    private int mPlugged;

    
    public void onShutdownThread() {
//        Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
//        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);

        if(option == 0) {
            Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
            intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            //reboot the device after a while
            Intent intent=new Intent(Intent.ACTION_REBOOT);
            intent.putExtra("nowait", 1);
            intent.putExtra("interval", 1);
            intent.putExtra("window", 0);
            sendBroadcast(intent);
        }

    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                mBatteryLevel = intent.getIntExtra("level", 0);
                mPlugged = intent.getIntExtra("plugged", 0);
                Log.d(TAG, "level: "+ mBatteryLevel + " mPlugged: "+ mPlugged);
                if (mPlugged != BatteryManager.BATTERY_PLUGGED_AC) {
                    //onShutdownThread();
               }
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.main);
        mTextView01 = (TextView) findViewById(R.id.myTextView1);
        mButtonStart = (Button) findViewById(R.id.start_test);
        mCounter = (TextView) findViewById(R.id.counter);

        String strOption = SystemProperties.get(PROPERTY_SHUTDOWN_OPTION, "0");
        option = Integer.valueOf(strOption).intValue();

        String time = SystemProperties.get(PROPERTY_SHUTDOWN_DURATION, "60");
        duration = Integer.valueOf(time).intValue();
        mTextView01.setText(" "+duration);

        spinner = (Spinner) findViewById(R.id.spinner_duration);
        String[] curs = getResources().getStringArray(R.array.spinner_duration);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.myspinner, curs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        if(duration == 90) pos = 0;
        else if(duration == 60) pos = 1;
        else if(duration == 30) pos = 2;
        else if(duration == 10) pos = 3;

        spinner.setSelection(pos,true);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                if(position == 0) {
                    duration = 90;
                } else if(position == 1) {
                    duration = 60;
                } else if(position == 2) {
                    duration = 30;
                } else if(position == 3) {
                    duration = 10;
                }
                //TextView tv = (TextView)view;
                //tv.setTextColor(getResources().getColor(android.R.color.white));
                //tv.setTextSize(12.0f);
                //tv.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

                mTextView01.setText(" "+duration);
                SystemProperties.set(PROPERTY_SHUTDOWN_DURATION, Integer.toString(duration));
                //Toast.makeText(Shutdown.this, "onItemSelected", Toast.LENGTH_SHORT).show();
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        mRadioGroupAPI = (RadioGroup) findViewById(R.id.radio_group_api);
        
        int RADIO_ID = (option==0)?R.id.radio_shutdown:R.id.radio_reboot;
        mRadioButtonAPI1 = (RadioButton) findViewById(RADIO_ID);
        mRadioButtonAPI1.toggle();
        mRadioGroupAPI.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {              
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if(checkedId==R.id.radio_shutdown){
                    option = 0;
                    Toast.makeText(Shutdown.this, "shut down", Toast.LENGTH_SHORT).show();
                    SystemProperties.set(PROPERTY_SHUTDOWN_OPTION, Integer.toString(option));
                }  else if(checkedId==R.id.radio_reboot){
                    option = 1;
                    Toast.makeText(Shutdown.this, "reboot", Toast.LENGTH_SHORT).show();
                    SystemProperties.set(PROPERTY_SHUTDOWN_OPTION, Integer.toString(option));
                }
            }
        });
        
        String strTotal = SystemProperties.get(PROPERTY_SHUTDOWN_TOTAL, "0");
        total = Integer.valueOf(strTotal).intValue();
        String strCount = SystemProperties.get(PROPERTY_SHUTDOWN_COUNTER, "0");
        counter = Integer.valueOf(strCount).intValue();
        mCounter.setText("Counter: "+counter);
        if(mHandler!=null && total>=counter) {
            mHandler.postDelayed(runnable, 1000);
        }

        // Capture text edit key press
        mEditTotal = (EditText) findViewById(R.id.edit_total);
        mEditTotal.setText(""+total);
        mEditTotal.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Toast.makeText(Shutdown.this, mEditTotal.getText(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });


        keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        lockTag =  "Shutdown";
        keyguardLock = keyguardManager.newKeyguardLock(lockTag);
        keyguardLock.disableKeyguard();

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mIntentReceiver, mIntentFilter);
   }
 
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_AUTO_REFRESH:
                mButtonStart.setEnabled(false);
                --duration;
                mTextView01.setText(" "+duration);
                if(duration == 0) {
                    ++counter;
                    SystemProperties.set(PROPERTY_SHUTDOWN_COUNTER, Integer.toString(counter));
                    onShutdownThread();
                }
                break;
            }
        }
    };
    
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
                if(duration > 0) {
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
        counter = 0;
        SystemProperties.set(PROPERTY_SHUTDOWN_COUNTER, Integer.toString(counter));
        mCounter.setText("Counter: "+counter);

        mHandler.removeCallbacks(runnable);
        mButtonStart.setEnabled(true);
    }

    public void onStartTest(View v) {
        //mHandler.removeCallbacks(runnable);
        String strEdit = mEditTotal.getText().toString();
        Log.d(TAG, ""+strEdit);
        int temp = Integer.valueOf(strEdit).intValue();
        Log.d(TAG, "temp: "+temp+ " total: "+total);
        if(total != temp) {
            total = temp;
            SystemProperties.set(PROPERTY_SHUTDOWN_TOTAL, Integer.toString(total));
        }
        counter = 1;
        SystemProperties.set(PROPERTY_SHUTDOWN_COUNTER, Integer.toString(counter));
        mCounter.setText("Counter: "+counter);

        if(mHandler != null && runnable!=null) {
            mHandler.removeCallbacks(runnable);
            mHandler.postDelayed(runnable, 1000);
        }
    }

    public void onShutDown(View v) {
        if(option == 0) {
            Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
            intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            //reboot the device after a while
            Intent intent=new Intent(Intent.ACTION_REBOOT);
            intent.putExtra("nowait", 1);
            intent.putExtra("interval", 1);
            intent.putExtra("window", 0);
            sendBroadcast(intent);
        }
        
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
