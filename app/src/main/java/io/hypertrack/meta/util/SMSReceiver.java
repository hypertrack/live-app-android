package io.hypertrack.meta.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by suhas on 26/11/15.
 */
public class SMSReceiver extends BroadcastReceiver
{
    public void onReceive(Context context, Intent intent)
    {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;
        String strMessage = "";

        if (myBundle != null)
        {

            if(Build.VERSION.SDK_INT>=19) { //KITKAT
                SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                messages = msgs;

                for (int i = 0; i < messages.length; i++) {
                    strMessage += "SMS From: " + messages[i].getOriginatingAddress();
                    strMessage += " : ";
                    strMessage += messages[i].getMessageBody();
                    strMessage += "\n";
                }

                Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
