package com.kuack.plugins.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    private CallbackContext callbackContext;

    public SMSBroadcastReceiver (CallbackContext callbackContext) {
        // Log.d("SMSRetrieverPlugin", "SMSBroadcastReceiver CONSTRUCTOR");
        this.callbackContext = callbackContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SMSRetrieverPlugin", "SMSBroadcastReceiver onReceive");

        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

            PluginResult result = null;
            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    result = new PluginResult(PluginResult.Status.OK, message);
                    // Log.d("SMSRetrieverPlugin", "SMSBroadcastReceiver RECEIVED: " + message);
                    break;

                case CommonStatusCodes.TIMEOUT:
                    result = new PluginResult(PluginResult.Status.ERROR, "SMSBroadcastReceiver TIMEOUT");
                    // Log.d("SMSRetrieverPlugin", "SMSBroadcastReceiver TIMEOUT");
                    break;
            }

            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
            // Log.d("SMSRetrieverPlugin", "SMSBroadcastReceiver enviando resultado....");
        }
    }

}