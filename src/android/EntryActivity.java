package com.meldtables.filestackcordova;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.content.Context;

public class EntryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check to see if this Activity is the root activity
        if (isTaskRoot()) {

            try {

               Class mainActivity;
               Context context = getApplicationContext();
               String  packageName = context.getPackageName();
               Intent  launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
               String  className = launchIntent.getComponent().getClassName();

                //loading the Main Activity to not import it in the plugin
                mainActivity = Class.forName(className);

                // This Activity is the only Activity, so
                //  the app wasn't running. So start the app from the
                //  beginning (redirect to MainActivity)
                Intent mainIntent = getIntent(); // Copy the Intent used to launch me
                // Launch the real root Activity (launch Intent)
                mainIntent.setClass(this, mainActivity);
                // I'm done now, so finish()
                startActivity(mainIntent);

            } catch (Exception e) {
                e.printStackTrace();
            }

            finish();

        } else {
            // App was already running, so just finish, which will drop the user
            //  in to the activity that was at the top of the task stack
            finish();
        }
    }
}