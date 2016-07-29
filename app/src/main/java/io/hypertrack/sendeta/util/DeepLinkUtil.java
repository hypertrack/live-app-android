package io.hypertrack.sendeta.util;

import android.text.TextUtils;

import io.hypertrack.sendeta.model.AppDeepLink;

/**
 * Created by piyush on 26/07/16.
 */
public class DeepLinkUtil {

    //DeepLink Ids
    public static final int HOME = 1;
    public static final int MEMBERSHIP = 2;
    public static final int RECEIVE_ETA_FOR_DESTINATION = 3;
    public static final int DEFAULT = HOME;

    //deeplink mapping keys
    public static final String KEY_ID = "id";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";

    //private static AppDeepLink appDeepLink;
    public static AppDeepLink prepareAppDeepLink(String url) {

        AppDeepLink appDeepLink = new AppDeepLink(DEFAULT);

        if (url != null && url.length() > 0) {

            try {

                // Add Business Profile Screen mId
                if (url.contains("/accept")) {
                    appDeepLink.mId = DeepLinkUtil.MEMBERSHIP;
                }

                if (url.contains("/request")) {
                    appDeepLink.mId = DeepLinkUtil.RECEIVE_ETA_FOR_DESTINATION;
                }

                if (url.contains("?")) {
                    String[] allData = url.split("\\?", 2);

                    if (allData != null && allData.length > 1) {

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
