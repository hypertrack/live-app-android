package io.hypertrack.sendeta.util;

import android.content.Context;
import android.text.TextUtils;

import io.hypertrack.lib.common.util.HTLog;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AppDeepLink;

/**
 * Created by piyush on 26/07/16.
 */
public class DeepLinkUtil {

    //DeepLink Ids
    public static final int HOME = 1;
    public static final int MEMBERSHIP = 2;
    public static final int RECEIVE_ETA = 3;
    public static final int TRACK = 4;
    public static final int DEFAULT = HOME;

    //deeplink mapping keys
    public static final String KEY_ID = "id";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_ADDRESS = "add";
    public static final String KEY_SHORT_CODE = "short_code";

    //private static AppDeepLink appDeepLink;
    public static AppDeepLink prepareAppDeepLink(Context context, String url) {

        AppDeepLink appDeepLink = new AppDeepLink(DEFAULT);

        if (url != null && url.length() > 0) {
            url = url.replace("%2A", "*").replace("%3D", "=");

            try {
                if (url.contains(context.getString(R.string.request_eta_base_url) + "/request")) {
                    appDeepLink.mId = DeepLinkUtil.RECEIVE_ETA;
                }

                if (url.contains(context.getString(R.string.eta_link_url))) {
                    appDeepLink.mId = DeepLinkUtil.TRACK;

                    if (url.contains("/")) {
                        String[] allData = url.split("/");

                        if (allData.length > 1) {

                            String paramData = allData[allData.length - 1];
                            if (!TextUtils.isEmpty(paramData)) {
                                appDeepLink.shortCode = paramData;
                            }
                        }
                    }
                }

                if (url.contains("?")) {
                    String[] allData = url.split("\\?", 2);

                    if (allData.length > 1) {

                        String paramData = allData[1];

                        if (!TextUtils.isEmpty(paramData)) {

                            String params[] = paramData.split("\\*");

                            //check if it has params or not
                            if (params.length > 0) {

                                for (String param : params) {
                                    String data[] = param.split("=");

                                    switch (data[0]) {

                                        case KEY_ID: {
                                            try {
                                                appDeepLink.id = Integer.valueOf(data[1]);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        }

                                        case KEY_UUID:
                                            try {
                                                appDeepLink.uuid = String.valueOf(data[1]);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            break;

                                        case KEY_LAT:
                                            try {
                                                appDeepLink.lat = Double.valueOf(data[1]);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            break;

                                        case KEY_LNG:
                                            try {
                                                appDeepLink.lng = Double.valueOf(data[1]);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            break;

                                        case KEY_ADDRESS:
                                            try {
                                                appDeepLink.address = String.valueOf(data[1]);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return appDeepLink;
    }
}
