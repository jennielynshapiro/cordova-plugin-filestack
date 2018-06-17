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

public class FilestackCordova extends CordovaPlugin {

    static final int REQUEST_FILESTACK = 1111;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
   	 	if (action.equals("openFilePicker")) {
     	   String message = args.getString(0);
    	    this.echo(message, callbackContext);
    	    this.openFilePicker();
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

	private void openFilePicker() {

        Intent intent = new Intent(this, FsActivity.class);

        Config config = new Config("AVI0HHr8cQuGOboNeE1Gtz", "https://demo.android.filestack.com");
        intent.putExtra(FsConstants.EXTRA_CONFIG, config);

        cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_FILESTACK);

	}
}