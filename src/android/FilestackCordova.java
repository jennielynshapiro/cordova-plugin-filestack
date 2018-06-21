package com.meldtables.filestackcordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.content.Context;

import android.content.Intent;
import com.filestack.Config;
import com.filestack.android.FsActivity;
import com.filestack.android.FsConstants;
import com.filestack.Sources;
import com.filestack.android.Selection;
import com.filestack.FileLink;

import java.util.Locale;

import android.app.Activity;

import java.util.ArrayList;

import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;

import java.util.HashMap;

public class FilestackCordova extends CordovaPlugin {

    static final int REQUEST_FILESTACK = 1111;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        Log.i("FilestackCordova", "registerReceiver");

        IntentFilter intentFilter = new IntentFilter(FsConstants.BROADCAST_UPLOAD);
        UploadStatusReceiver receiver = new UploadStatusReceiver();

        Context context = cordova.getActivity().getApplicationContext();
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter);
    }

    private CallbackContext callbackContext;

    private HashMap<String, CallbackContext> selectionCallbacks = new HashMap<String, CallbackContext>();

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
   	 	Log.v("FilestackCordova", "execute");

   	 	this.callbackContext = callbackContext;

   	 	if (action.equals("openFilePicker")) {
     	   String apiKey = args.getString(0);
     	   String returnUrl = args.getString(1);

    	   this.openFilePicker(apiKey, returnUrl);

    	   PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT);
           pluginResult.setKeepCallback(true);
           callbackContext.sendPluginResult(pluginResult);

    	   return true;
   	 	}

   	 	return false;
	}

	private void openFilePicker(String apiKey, String returnUrl) {

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

                String[] mimeTypes = {"*/*"};
                intent.putExtra(FsConstants.EXTRA_MIME_TYPES, mimeTypes);

                cordova.setActivityResultCallback(me);
                cordova.startActivityForResult(me, intent, REQUEST_FILESTACK);


            }
        });
	}

	    @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {

            super.onActivityResult(requestCode, resultCode, data);
            Log.i("FilestackCordova", "onActivityResult " + resultCode);
            Locale locale = Locale.getDefault();

            if (requestCode == REQUEST_FILESTACK && resultCode == Activity.RESULT_OK) {
                Log.i("FilestackCordova", "received filestack selections");
                String key = FsConstants.EXTRA_SELECTION_LIST;
                ArrayList<Selection> selections = data.getParcelableArrayListExtra(key);
                for (int i = 0; i < selections.size(); i++) {
                    Selection selection = selections.get(i);
                    String msg = String.format(locale, "selection %d: %s", i, selection.getName());

                    selectionCallbacks.put(getSelectionKey(selection), callbackContext);

                    Log.i("FilestackCordova", msg);
                }
            }

        }

        private String getSelectionKey(Selection selection) {
            String key = "";
            if(selection.getPath() != null) {
                key = selection.getPath();
            } else if(selection.getUri() != null) {
                key = selection.getUri().toString();
            }
            Log.i("FilestackCordova", "selection key: " + key);
            return key;
        }

        public class UploadStatusReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                Locale locale = Locale.getDefault();
                String status = intent.getStringExtra(FsConstants.EXTRA_STATUS);
                Selection selection = intent.getParcelableExtra(FsConstants.EXTRA_SELECTION);
                FileLink fileLink = (FileLink) intent.getSerializableExtra(FsConstants.EXTRA_FILE_LINK);

                String name = selection.getName();
                String handle = fileLink != null ? fileLink.getHandle() : "n/a";
                String msg = String.format(locale, "upload %s: %s (%s)", status, name, handle);
                Log.i("UploadStatusReceiver", msg);


                // get callback context


                if(fileLink != null) {

                    CallbackContext selectionCallbackContext = selectionCallbacks.remove(getSelectionKey(selection));
                    Log.i("FilestackCordova", "selectionCallbackContext: " + (selectionCallbackContext != null ? selectionCallbackContext.toString() : "null"));

                    try {
                    JSONObject jsonResult = toJSON(selection, fileLink);



                        // callbackContext.success(jsonResult);



                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonResult);
                        pluginResult.setKeepCallback(true); // keep callback
                        callbackContext.sendPluginResult(pluginResult);



                    } catch(JSONException exception) {
                        callbackContext.error("cannot parse json");
                    }

                }

            }
        }

    public JSONObject toJSON(Selection selection, FileLink fileLink) throws JSONException {

        JSONObject res = new JSONObject();

        if(selection != null) {
            if(selection.getProvider() != null) { res.put("provider", selection.getProvider()); }
            if(selection.getPath() != null) { res.put("path", selection.getPath()); }
            if(selection.getUri() != null) { res.put("uri", selection.getUri().toString()); }
            if(selection.getSize() != null) { res.put("size", selection.getSize()); }
            if(selection.getMimeType() != null) { res.put("mimeType", selection.getMimeType()); }
            if(selection.getName() != null) { res.put("name", selection.getName()); }
        }

        if(fileLink != null) {
            if(fileLink.getHandle() != null) { res.put("fileLink", fileLink.getHandle()); }
        }

        return res;

    }

        // adb logcat -s "UploadStatusReceiver","FilestackCordova"
}