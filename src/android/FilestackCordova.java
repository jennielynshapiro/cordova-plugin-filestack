package com.meldtables.filestackcordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;
import android.content.Context;

import android.content.Intent;
import com.filestack.Config;
import com.filestack.android.FsActivity;
import com.filestack.android.FsConstants;
import com.filestack.Sources;

import java.util.ArrayList;

public class FilestackCordova extends CordovaPlugin {

    static final int REQUEST_FILESTACK = 1111;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
   	 	Log.v("FilestackCordova", "execute");

   	 	if (action.equals("openFilePicker")) {
     	   String apiKey = args.getString(0);
     	   String returnUrl = args.getString(1);
    	    this.echo(apiKey + " " + returnUrl, callbackContext);
    	    this.openFilePicker(apiKey, returnUrl);
    	    return true;
   	 	}
   	 	return false;
	}

	private void echo(String message, CallbackContext callbackContext) {
   	 	if (message != null && message.length() > 0) {
    	    callbackContext.success(message);
    	} else {
    	    callbackContext.error("Expected one non-empty string argument.");
    	}
	}

	private void openFilePicker(apiKey, returnUrl) {

        final FilestackCordova me = this;

        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {


        Log.v("FilestackCordova", "openFilePicker");

        Context context = cordova.getActivity().getApplicationContext();
        Intent intent = new Intent(context, FsActivity.class);

        Config config = new Config(apiKey, returnUrl);
        intent.putExtra(FsConstants.EXTRA_CONFIG, config);

        ArrayList<String> sources = new ArrayList<>();
        sources.add(Sources.DEVICE);
        sources.add(Sources.GOOGLE_DRIVE);
        sources.add(Sources.GITHUB);
        intent.putExtra(FsConstants.EXTRA_SOURCES, sources);

        cordova.startActivityForResult(me, intent, REQUEST_FILESTACK);


            }
        });
	}
}