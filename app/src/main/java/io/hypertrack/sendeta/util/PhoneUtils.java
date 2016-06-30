package io.hypertrack.sendeta.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneUtils {

    private static final String TAG = PhoneUtils.class.getSimpleName();

    public static String getCountryRegionFromPhone(Context paramContext) {
        TelephonyManager service = null;
        int res = paramContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE");
        if (res == PackageManager.PERMISSION_GRANTED) {
            service = (TelephonyManager) paramContext.getSystemService(Context.TELEPHONY_SERVICE);
        }
        String code = null;
        if (service != null) {
            String str = service.getLine1Number();
            if (!TextUtils.isEmpty(str) && !str.matches("^0*$")) {
                code = parseNumber(str);
            }
        }
        if (code == null) {
            if (service != null) {
                code = service.getNetworkCountryIso();
            }
            if (TextUtils.isEmpty(code)) {
                code = paramContext.getResources().getConfiguration().locale.getCountry();
            }
        }
        if (code != null) {
            return code.toUpperCase();
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
