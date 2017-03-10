package com.hypertrack.lib.internal.transmitter.devicehealth;

/**
 * Created by piyush on 06/08/16.
 */

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.common.HTConstants;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.TextUtils;

/** package*/ class RadioState {

    private static final String TAG = "RadioState";

    private static final String HT_SHARED_PREFERENCE_SIGNAL_STRENGTH = "com.hypertrack:Radio.SignalStrength";
    private static final String HT_SHARED_PREFERENCE_NETWORK_TYPE = "com.hypertrack:Radio.NetworkType";
    private static final String HT_SHARED_PREFERENCE_NETWORK_STATE = "io.hypertrack.lib:Radio.NetworkState";

    private static final String NETWORK_TYPE_NONE = "NONE";
    private static final String NETWORK_TYPE_WIFI = "WIFI";
    private static final String NETWORK_TYPE_OTHER = "OTHER";

    private static final String NETWORK_TYPE_GPRS = "GPRS";
    private static final String NETWORK_TYPE_EDGE = "EDGE";
    private static final String NETWORK_TYPE_CDMA = "CDMA";
    private static final String NETWORK_TYPE_1xRTT = "1XRTT";
    private static final String NETWORK_TYPE_IDEN = "IDEN";

    private static final String NETWORK_TYPE_UMTS = "UMTS";
    private static final String NETWORK_TYPE_EVDO_0 = "EVDO_0";
    private static final String NETWORK_TYPE_EVDO_A = "EVDO_A";
    private static final String NETWORK_TYPE_HSDPA = "HSDPA";
    private static final String NETWORK_TYPE_HSUPA = "HSUPA";
    private static final String NETWORK_TYPE_HSPA = "HSPA";
    private static final String NETWORK_TYPE_EVDO_B = "EVDO_B";
    private static final String NETWORK_TYPE_EHRPD = "EHRPD";
    private static final String NETWORK_TYPE_HSPAP = "HSPAP";

    private static final String NETWORK_TYPE_LTE = "LTE";
    private static final String NETWORK_TYPE_UNKNOWN = "UNKNOWN";
    private static final String NETWORK_TYPE_INVALID = "INVALID";

    private static final String NETWORK_STATE_DISCONNECTED = "DISCONNECTED";

    private Context mContext;

    RadioState(Context mContext) {
        this.mContext = mContext;
    }

    class RadioHealth {
        @SerializedName("signal_strength")
        private Integer signalStrength;

        @SerializedName("network")
        private String networkType;

        @SerializedName("state")
        private String networkState;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RadioHealth that = (RadioHealth) o;

            if (signalStrength != null ? !signalStrength.equals(that.signalStrength) : that.signalStrength != null)
                return false;
            if (!networkType.equals(that.networkType)) return false;
            return networkState != null ? networkState.equals(that.networkState) : that.networkState == null;

        }
    }

    RadioHealth getRadioHealth() {
        try {
            // Fetch Current RadioHealth
            RadioHealth radioHealth = new RadioHealth();
            radioHealth.signalStrength = getSignalStrength(mContext);
            radioHealth.networkType = getNetworkType(mContext);
            radioHealth.networkState = getNetworkState(mContext);

            // Fetch Saved RadioHealth
            RadioHealth savedRadioHealth = getSavedRadioHealth();

            if (!radioHealth.equals(savedRadioHealth)) {
                saveRadioHealth(radioHealth);
                return radioHealth;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while getRadioHealth: " + e.getMessage());
        }

        return null;
    }

    // Methods to clear cached RadioState Data
    static void clearSavedRadioStateData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(HT_SHARED_PREFERENCE_SIGNAL_STRENGTH);
        editor.remove(HT_SHARED_PREFERENCE_NETWORK_TYPE);
        editor.remove(HT_SHARED_PREFERENCE_NETWORK_STATE);
        editor.apply();
    }

    private String getNetworkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            return NETWORK_TYPE_NONE;
        }

        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_MOBILE:
                return getMobileNetworkType(context);

            case ConnectivityManager.TYPE_WIFI:
                return NETWORK_TYPE_WIFI;

            default:
                return NETWORK_TYPE_OTHER;
        }
    }

    private String getMobileNetworkType(Context context) {

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephonyManager.getNetworkType();

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return NETWORK_TYPE_UNKNOWN;

            case TelephonyManager.NETWORK_TYPE_GPRS:
                return NETWORK_TYPE_GPRS;

            case TelephonyManager.NETWORK_TYPE_EDGE:
                return NETWORK_TYPE_EDGE;

            case TelephonyManager.NETWORK_TYPE_CDMA:
                return NETWORK_TYPE_CDMA;

            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return NETWORK_TYPE_1xRTT;

            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_TYPE_IDEN;

            case TelephonyManager.NETWORK_TYPE_UMTS:
                return NETWORK_TYPE_UMTS;

            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return NETWORK_TYPE_EVDO_0;

            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return NETWORK_TYPE_EVDO_A;

            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return NETWORK_TYPE_HSDPA;

            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return NETWORK_TYPE_HSUPA;

            case TelephonyManager.NETWORK_TYPE_HSPA:
                return NETWORK_TYPE_HSPA;

            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return NETWORK_TYPE_EVDO_B;

            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return NETWORK_TYPE_EHRPD;

            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_TYPE_HSPAP;

            case TelephonyManager.NETWORK_TYPE_LTE:
                return NETWORK_TYPE_LTE;

            default:
                return NETWORK_TYPE_INVALID;
        }
    }

    private String getNetworkState(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getState() == null || TextUtils.isEmpty(networkInfo.getState().name()))
            return NETWORK_STATE_DISCONNECTED;

        return networkInfo.getState().name() != null ? networkInfo.getState().name() : NETWORK_STATE_DISCONNECTED;
    }

    private Integer getSignalStrength(Context context) {

        // Check if Android API Version is 17 (JellyBeanMR1) or Above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            try {
                final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null && tm.getAllCellInfo() != null) {
                    for (final CellInfo info : tm.getAllCellInfo()) {

                        if (info == null)
                            continue;

                        if (info.isRegistered()) {
                            if (info instanceof CellInfoGsm) {
                                final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                                return gsm != null ? gsm.getDbm() : null;

                            } else if (info instanceof CellInfoCdma) {
                                final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                                return cdma != null ? cdma.getDbm() : null;

                            } else if (info instanceof CellInfoLte) {
                                final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                                return lte != null ? lte.getDbm() : null;

                            } else if (info instanceof CellInfoWcdma) {
                                final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                                return wcdma != null ? wcdma.getDbm() : null;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private RadioHealth getSavedRadioHealth() {
        try {
            RadioHealth radioHealth = new RadioHealth();
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY,
                    Context.MODE_PRIVATE);

            // Check if RadioHealth keys were present, Return null otherwise
            if (sharedPreferences.contains(HT_SHARED_PREFERENCE_SIGNAL_STRENGTH)
                    || sharedPreferences.contains(HT_SHARED_PREFERENCE_NETWORK_TYPE)
                    || sharedPreferences.contains(HT_SHARED_PREFERENCE_NETWORK_STATE)) {

                String signalStrength = sharedPreferences.getString(HT_SHARED_PREFERENCE_SIGNAL_STRENGTH, null);
                radioHealth.signalStrength = TextUtils.isEmpty(signalStrength) ? null : Integer.valueOf(signalStrength);
                radioHealth.networkType = sharedPreferences.getString(HT_SHARED_PREFERENCE_NETWORK_TYPE, null);
                radioHealth.networkState = sharedPreferences.getString(HT_SHARED_PREFERENCE_NETWORK_STATE, null);

                return radioHealth;
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while getSavedRadioHealth: " + e);
        }

        return null;
    }

    private void saveRadioHealth(RadioHealth radioHealth) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HT_SHARED_PREFERENCE_SIGNAL_STRENGTH, radioHealth.signalStrength != null ?
                String.valueOf(radioHealth.signalStrength) : null);
        editor.putString(HT_SHARED_PREFERENCE_NETWORK_STATE, radioHealth.networkState);
        editor.putString(HT_SHARED_PREFERENCE_NETWORK_TYPE, radioHealth.networkType);
        editor.apply();
    }
}