package io.hypertrack.sendeta.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class PhoneUtils {

    private static final String TAG = PhoneUtils.class.getSimpleName();

    public static String getCountryRegionFromPhone(Context paramContext) {
        TelephonyManager service = (TelephonyManager) paramContext.getSystemService(Context.TELEPHONY_SERVICE);

        String code = null;
        if (service != null) {
            code = service.getNetworkCountryIso();
        }

        if (!TextUtils.isEmpty(code)) {
            code = service.getSimCountryIso();
        }

        if (TextUtils.isEmpty(code)) {
            code = paramContext.getResources().getConfiguration().locale.getCountry();
        }

        if (code != null) {
            return code.toUpperCase();
        }

        return null;
    }

    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String parseNumber(String paramString) {
        if (paramString == null) {
            return null;
        }
        PhoneNumberUtil numberUtil = PhoneNumberUtil.getInstance();
        String result;
        try {
            Phonenumber.PhoneNumber localPhoneNumber = numberUtil.parse(paramString, null);
            Log.v(TAG, "Phone Number: " + localPhoneNumber);
            result = numberUtil.getRegionCodeForNumber(localPhoneNumber);
            if (result == null) {
                return null;
            }
        } catch (NumberParseException localNumberParseException) {
            return null;
        }
        return result;
    }


}
