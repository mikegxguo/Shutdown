package com.mitac.shutdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        // TODO Auto-generated method stub

        //Intent mService = new Intent(context, CheckBattery.class);
        //context.startService(mService);
        
        Intent mActivity = new Intent(context, Shutdown.class);
        mActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mActivity);  
    }
}
