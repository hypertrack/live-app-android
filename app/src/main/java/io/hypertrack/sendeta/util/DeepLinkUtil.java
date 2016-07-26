package io.hypertrack.sendeta.util;

import io.hypertrack.sendeta.model.AppDeepLink;

/**
 * Created by piyush on 26/07/16.
 */
public class DeepLinkUtil {

    //DeepLink Ids
    public static final int HOME = 1;
    public static final int MEMBERSHIP = 2;
    public static final int DEFAULT = HOME;

    //deeplink mapping keys
    public static final String KEY_MEMBERSHIP_ID = "id";

    //private static AppDeepLink appDeepLink;
    public static AppDeepLink prepareAppDeepLink(String url) {

        AppDeepLink appDeepLink = new AppDeepLink(DEFAULT);

        if (url != null && url.length() > 0) {

            try {

                // Add Business Profile Screen mId
                if (url.contains("/accept")) {
                    appDeepLink.mId = DeepLinkUtil.MEMBERSHIP;
                } else {

//                    String allData = url.split("\\?", 2)[1];
//
//                    if (!TextUtils.isEmpty(allData)) {
//
//                        String paramData[] = allData.split("\\*");
//
//                        appDeepLink.mId = Integer.valueOf(paramData[0].split("=")[1]);
//
//                        //check if it has params or not
//                        if (paramData.length > 1) {
//                            String params[] = paramData[1].split("\\*");
//
//                            for (String param : params) {
//
//                                String data[] = param.split("=");
//
//                                switch (data[0]) {
//
//                                    case KEY_MEMBERSHIP_ID: {
//                                        try {
//                                            appDeepLink.id = Integer.valueOf(data[1]);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return appDeepLink;
    }
}
