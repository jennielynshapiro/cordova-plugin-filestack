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
import com.filestack.StorageOptions;

import java.util.Locale;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Arrays;

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

            this.openFilePicker(args);

            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);

            return true;
        }

        return false;
    }

    private void openFilePicker(JSONArray args) {

        final FilestackCordova me = this;

        this.cordova.getThreadPool().execute(new Runnable() {
            public void run() {

                Log.v("FilestackCordova", "openFilePicker");

                Context context = cordova.getActivity().getApplicationContext();
                Intent intent = new Intent(context, FsActivity.class);

                try {
                    me.parseConfig(intent, args);
                    me.parseSources(intent, args);
                    me.parseMimeTypes(intent, args);
                    me.parseStorageOptions(intent, args);
                } catch (JSONException e) {
                    Log.v("FilestackCordova", e.toString());
                }

                cordova.setActivityResultCallback(me);
                cordova.startActivityForResult(me, intent, REQUEST_FILESTACK);

            }
        });
    }

    private void parseConfig(Intent intent, JSONArray args) throws JSONException {

        String apiKey = "";
        String returnUrl = "";

        if (!args.isNull(0)) {
            apiKey = args.getString(0);
        }

        if (!args.isNull(3)) {
            returnUrl = args.getString(3);
        }

        Config config = new Config(apiKey, returnUrl);
        intent.putExtra(FsConstants.EXTRA_CONFIG, config);
    }

    private void parseSources(Intent intent, JSONArray args) throws JSONException {
        if (!args.isNull(1)) {
            String[] sources = this.parseJSONStringArray(args.getJSONArray(1));
            intent.putExtra(FsConstants.EXTRA_SOURCES, new ArrayList<String>(Arrays.asList(sources)));
        }
    }

    private void parseMimeTypes(Intent intent, JSONArray args) throws JSONException {
        if (!args.isNull(2)) {
            String[] mimeTypes = this.parseJSONStringArray(args.getJSONArray(2));
            intent.putExtra(FsConstants.EXTRA_MIME_TYPES, mimeTypes);
        }
    }

    private void parseStorageOptions(Intent intent, JSONArray args) throws JSONException {

        StorageOptions.Builder builder = new StorageOptions.Builder();

        if (!args.isNull(5)) {
            builder.location(args.getString(5));
        }

        if (!args.isNull(6)) {
            builder.container(args.getString(6));
        }

        if (!args.isNull(7)) {
            builder.region(args.getString(7));
        }

        StorageOptions storeOpts = builder.build();
        intent.putExtra(FsConstants.EXTRA_STORE_OPTS, storeOpts);

    }

    public String[] parseJSONStringArray(JSONArray jSONArray) throws JSONException {
        String[] a = new String[jSONArray.length()];
        for (int i = 0; i < jSONArray.length(); i++) {
            a[i] = jSONArray.getString(i);
        }
        return a;
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
        if (selection.getPath() != null) {
            key = selection.getPath();
        } else if (selection.getUri() != null) {
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

            if (selection == null) {
                return;
            }

            if (fileLink == null) {
                return;
            }

            CallbackContext selectionCallbackContext = selectionCallbacks.remove(getSelectionKey(selection));

            Log.i("FilestackCordova", "selectionCallbackContext: " + (selectionCallbackContext != null ? selectionCallbackContext.toString() : "null"));

            if (selectionCallbackContext == null) {
                return;
            }

            try {

                JSONObject selectionJson = selectionToJson(selection, fileLink);

                if (selectionCallbacks.containsValue(selectionCallbackContext)) {

                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultToJson(selectionJson, false));
                    pluginResult.setKeepCallback(true); // keep callback
                    selectionCallbackContext.sendPluginResult(pluginResult);

                } else {

                    selectionCallbackContext.success(resultToJson(selectionJson, true));

                }

            } catch (JSONException exception) {
                callbackContext.error("cannot parse json");
            }


        }
    }

    public JSONObject resultToJson(JSONObject selectionJson, boolean complete) throws JSONException {
        JSONObject res = new JSONObject();
        res.put("file", selectionJson);
        res.put("complete", complete);
        return res;
    }

    public JSONObject selectionToJson(Selection selection, FileLink fileLink) throws JSONException {

        JSONObject res = new JSONObject();

        String filename = "";

        if (selection != null) {

            res.put("size", selection.getSize());

            if (selection.getMimeType() != null) {
                res.put("mimetype", selection.getMimeType());
            }

            if (selection.getName() != null) {
                filename = selection.getName();
                res.put("filename", filename);
            }

        }

        if (fileLink != null && fileLink.getHandle() != null) {
            String handle = fileLink.getHandle();
            res.put("handle", handle);
        }

        return res;

    }
}