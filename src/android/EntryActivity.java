package com.meldtables.filestackcordova;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class EntryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check to see if this Activity is the root activity
        if (isTaskRoot()) {

            Class mainActivity;
            Context context = getApplicationContext();
            String  packageName = context.getPackageName();
            Intent  launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            String  className = launchIntent.getComponent().getClassName();

            try {
                //loading the Main Activity to not import it in the plugin
                mainActivity = Class.forName(className);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // This Activity is the only Activity, so
            //  the app wasn't running. So start the app from the
            //  beginning (redirect to MainActivity)
            Intent mainIntent = getIntent(); // Copy the Intent used to launch me
            // Launch the real root Activity (launch Intent)
            mainIntent.setClass(this, mainActivity);
            // I'm done now, so finish()
            startActivity(mainIntent);
            finish();
        } else {
            // App was already running, so just finish, which will drop the user
            //  in to the activity that was at the top of the task stack
            finish();
        }
    }
}