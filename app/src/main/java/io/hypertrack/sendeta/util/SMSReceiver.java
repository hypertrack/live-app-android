package io.hypertrack.sendeta.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;

/**
 * Created by suhas on 26/11/15.
 */
public class SMSReceiver extends BroadcastReceiver
{

    public static final String SENDETA_SMS_RECIEVED = "SENDETA_SMS_RECIEVED";
    public static final String VERIFICATION_CODE = "VERIFICATION_CODE";

    public void onReceive(Context context, Intent intent)
    {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages = null;
        String strMessage = "";

        if (myBundle != null)
        {

            if(Build.VERSION.SDK_INT>=19) {
                SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                messages = msgs;

                for (int i = 0; i < messages.length; i++) {
                    strMessage += "SMS From: " + messages[i].getOriginatingAddress();
                    strMessage += " : ";
                    strMessage += messages[i].getMessageBody();
                    strMessage += "\n";

                    if (messages[i].getOriginatingAddress().contains("ETAFYI")) {

                        String message = messages[i].getMessageBody();
                        String code = message.substring(message.length()-4);
                        Intent receivedIntent = new Intent(SENDETA_SMS_RECIEVED);
                        receivedIntent.putExtra(VERIFICATION_CODE, code);

                        LocalBroadcastManager.getInstance(context).sendBroadcast(receivedIntent);
                    }
                }

                //Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
