/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.util;

import android.content.Context;
import android.net.Uri;

import com.hypertrack.lib.internal.common.util.HTTextUtils;

import java.util.Set;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AppDeepLink;

/**
 * Created by piyush on 26/07/16.
 */
public class DeepLinkUtil {

    //DeepLink Ids
    public static final int HOME = 1;
    public static final int TRACK = 2;
    public static final int BRANCH = 3;
    public static final int SHORTCUT = 4;
    public static final int DEFAULT = HOME;

    //deeplink mapping keys
    private static final String KEY_TASK_ID = "task_id";
    private static final String KEY_ACTION_ID = "action_id";
    private static final String KEY_SHORT_CODE = "short_code";
    private static final String KEY_LOOKUP_ID = "lookup_id";
    private static final String KEY_UNIQUE_ID = "unique_id";
    private static final String KEY_COLLECTION_ID = "collection_id";

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
        if (uri.getScheme() != null && uri.getScheme().equalsIgnoreCase("hypertrack.io")
                && !HTTextUtils.isEmpty(uri.getHost())
                && uri.getHost().contains("open")) {
            appDeepLink.mId = DeepLinkUtil.BRANCH;
        }

        if (uri.getScheme() != null && uri.getScheme().equalsIgnoreCase("share.location")
                && !HTTextUtils.isEmpty(uri.getHost())
                && uri.getHost().contains("hypertrack")) {
            appDeepLink.mId = DeepLinkUtil.SHORTCUT;
        }

        if (uri.getScheme() != null
                && uri.getScheme().equalsIgnoreCase(context.getString(R.string.deeplink_scheme))
                && !HTTextUtils.isEmpty(uri.getHost())
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

        if (uri.getHost() != null && uri.getHost().contains(context.getString(R.string.tracking_eta_fyi))) {
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

                case KEY_COLLECTION_ID:
                    try {
                        appDeepLink.collectionId = uri.getQueryParameter(paramName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case KEY_LOOKUP_ID:
                case KEY_UNIQUE_ID:
                    try {
                        appDeepLink.uniqueId = uri.getQueryParameter(paramName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

            }
        }

        return appDeepLink;
    }
}