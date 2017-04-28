package io.hypertrack.sendeta.util;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Set;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AppDeepLink;

/**
 * Created by piyush on 26/07/16.
 */
public class DeepLinkUtil {

    //DeepLink Ids
    public static final int HOME = 1;
    public static final int RECEIVE_ETA = 3;
    public static final int TRACK = 4;
    public static final int DEFAULT = HOME;

    //deeplink mapping keys
    public static final String KEY_ID = "id";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_TASK_ID = "task_id";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_ADDRESS = "add";
    public static final String KEY_SHORT_CODE = "short_code";
    public static final String KEY_LOOKUP_ID = "lookup_id";

    //private static AppDeepLink appDeepLink;
    public static AppDeepLink prepareAppDeepLink(Context context, Uri uri) {

        AppDeepLink appDeepLink = new AppDeepLink(DEFAULT);

        if (uri == null)
            return appDeepLink;

        try {
            return DeepLinkUtil.parseAppDeepLinkURI(context, appDeepLink, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appDeepLink;
    }

    private static void parsePathParams(Context context, AppDeepLink appDeepLink, Uri uri) {
        if (!TextUtils.isEmpty(uri.getAuthority())
                && (uri.getAuthority().contains(context.getString(R.string.request_eta_base_url)) ||
                uri.getAuthority().contains(context.getString(R.string.hypertrack_live_request_eta_base_url)))
                && !TextUtils.isEmpty(uri.getPath())
                && uri.getPath().contains("request")) {
            appDeepLink.mId = DeepLinkUtil.RECEIVE_ETA;
            return;
        }

        if (uri.getScheme() != null
                && uri.getScheme().equalsIgnoreCase(context.getString(R.string.deeplink_scheme))
                && !TextUtils.isEmpty(uri.getHost())
                && uri.getHost().contains("track")) {
            appDeepLink.mId = DeepLinkUtil.TRACK;
        }

        if (uri.getHost() != null && uri.getHost().contains(context.getString(R.string.tracking_url))) {
            appDeepLink.mId = DeepLinkUtil.TRACK;
            String[] pathParams = uri.getPath().split("/");
            // Check if pathParams is of the format "/abCSKD"
            if (pathParams.length == 2 && !pathParams[1].contains("/")) {
                appDeepLink.shortCode = pathParams[1];
            }
        }
    }

    private static AppDeepLink parseAppDeepLinkURI(Context context, AppDeepLink appDeepLink, Uri uri) {
        DeepLinkUtil.parsePathParams(context, appDeepLink, uri);

        Set<String> queryParamNames = uri.getQueryParameterNames();
        if (queryParamNames == null || queryParamNames.isEmpty())
            return appDeepLink;

        for (String paramName : queryParamNames) {
            switch (paramName) {
                case KEY_ID: {
                    try {
                        appDeepLink.id = Integer.valueOf(uri.getQueryParameter(paramName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case KEY_UUID:
                    try {
                        appDeepLink.uuid = uri.getQueryParameter(paramName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case KEY_TASK_ID:
                    try {
                        appDeepLink.taskID = uri.getQueryParameter(paramName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case KEY_LAT:
                    try {
                        appDeepLink.lat = Double.valueOf(uri.getQueryParameter(paramName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case KEY_LNG:
                    try {
                        appDeepLink.lng = Double.valueOf(uri.getQueryParameter(paramName));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case KEY_ADDRESS:
                    try {
                        appDeepLink.address = uri.getQueryParameter(paramName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case KEY_SHORT_CODE:
                    try {
                        appDeepLink.shortCode = uri.getQueryParameter(paramName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case KEY_LOOKUP_ID:
                    try {
                        appDeepLink.lookupId = uri.getQueryParameter(paramName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        return appDeepLink;
    }
}
