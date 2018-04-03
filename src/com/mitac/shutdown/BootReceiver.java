package com.mitac.shutdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        String action = intent.getAction().toString();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // u can start your service here
            //Toast.makeText(context, "boot completed action has got", Toast.LENGTH_LONG).show();
            //return;
        }

//        Intent mService = new Intent(context, CheckBattery.class);
//        context.startService(mService);
        
        Intent mActivity = new Intent(context, Shutdown.class);
        mActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mActivity);  
    }
}
