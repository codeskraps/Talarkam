/**
 * Talarkam
 * Copyright (C) Carles Sentis 2011 <codeskraps@gmail.com>
 *
 * Talarkam is free software: you can
 * redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later
 * version.
 *  
 * Talarkam is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *  
 * You should have received a copy of the GNU
 * General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

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
