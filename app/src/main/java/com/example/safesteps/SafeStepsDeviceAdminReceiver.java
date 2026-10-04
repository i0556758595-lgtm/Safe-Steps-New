package com.example.safesteps;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
public class SafeStepsDeviceAdminReceiver extends DeviceAdminReceiver {
 @Override public void onEnabled(Context c, Intent i){Toast.makeText(c,"Safe Steps הוגדרה כמנהלת מכשיר",Toast.LENGTH_SHORT).show();}
 @Override public void onDisabled(Context c, Intent i){Toast.makeText(c,"ניהול המכשיר של Safe Steps בוטל",Toast.LENGTH_SHORT).show();}
}
