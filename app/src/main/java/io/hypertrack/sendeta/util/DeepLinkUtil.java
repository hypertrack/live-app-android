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
    private static final int HOME = 1;
    public static final int TRACK = 2;
    public static final int DEFAULT = HOME;

    //deeplink mapping keys
    private static final String KEY_TASK_ID = "task_id";
    private static final String KEY_ACTION_ID = "action_id";
    private static final String KEY_SHORT_CODE = "short_code";
    private static final String KEY_LOOKUP_ID = "lookup_id";
    private static final String KEY_ORDER_ID = "order_id";


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
                case KEY_ACTION_ID:
                case KEY_TASK_ID:
                    try {
                        appDeepLink.taskID = uri.getQueryParameter(paramName);
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

                case KEY_ORDER_ID:
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
