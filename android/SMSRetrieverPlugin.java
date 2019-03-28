package com.kuack.plugins.sms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.auth.api.credentials.CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE;

public class SMSRetrieverPlugin extends CordovaPlugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    
    private CallbackContext context = null;
    private BroadcastReceiver receiver = null;
    private GoogleApiClient apiClient = null;
    private int RESOLVE_HINT = 42;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("getMsisdn".equals(action)) {
            if (this.apiClient != null) {
                this.apiClient.disconnect();
                this.apiClient = null;
            }

            this.context = callbackContext;

            this.apiClient = new GoogleApiClient.Builder(webView.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Auth.CREDENTIALS_API)
                    .build();

            HintRequest hintRequest = new HintRequest.Builder()
                    .setPhoneNumberIdentifierSupported(true)
                    .build();

            PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(apiClient, hintRequest);
            cordova.setActivityResultCallback(this);
            try {
                cordova.getActivity().startIntentSenderForResult(intent.getIntentSender(), RESOLVE_HINT, null, 0, 0, 0);
            } catch (Exception e) {
                Log.e("SMSRetrieverPlugin", "getMsisdn ERROR: " + e.getMessage(), e);
            }

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            return true;

        } else if ("startListener".equals(action)) {
            if (this.receiver == null) {
                this.receiver = new SMSBroadcastReceiver(callbackContext);

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);

                webView.getContext().registerReceiver(this.receiver, intentFilter);
            }

            Context context = this.cordova.getActivity().getApplicationContext();
            SmsRetrieverClient client = SmsRetriever.getClient(context);
            Task<Void> task = client.startSmsRetriever();

            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            return true;

        } else if ("stopListener".equals(action)) {
            unregisterReceiver();
            
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
            return true;
        }

        return false;
    }

    private void unregisterReceiver() {
        if (this.receiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.receiver);
                this.receiver = null;
            } catch (Exception e) {
                Log.e("SMSRetrieverPlugin", "unregisterReceiver ERROR: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver();
        super.onDestroy();
    }

    @Override
    public void onReset() {
        this.unregisterReceiver();
        super.onReset();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            PluginResult result = null;
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                Log.d("SMSRetrieverPlugin", "onActivityResult OK: " + credential.getId());
                result = new PluginResult(PluginResult.Status.OK, credential.getId());
            } else {
                switch (resultCode) {
                    case ACTIVITY_RESULT_NO_HINTS_AVAILABLE:
                        result = new PluginResult(PluginResult.Status.ERROR, "onActivityResult NO_HINTS_AVAILABLE");
                        break;
                    default:
                        result = new PluginResult(PluginResult.Status.ERROR, "onActivityResult ERROR: " + resultCode);
                        break;
                }
            }
            result.setKeepCallback(false);
            context.sendPluginResult(result);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("SMSRetrieverPlugin", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("SMSRetrieverPlugin", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("SMSRetrieverPlugin", "onConnectionFailed");
    }
}
