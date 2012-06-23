package com.codeskraps.talarkam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
	
    @Override
    public void onReceive(Context context, Intent intent)
    {	
    	Intent i = new Intent(context, AlarmActivity.class);
    	i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
    		    |Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
    		    |Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
