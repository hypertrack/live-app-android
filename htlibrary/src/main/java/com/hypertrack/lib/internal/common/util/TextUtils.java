package com.hypertrack.lib.internal.common.util;

import android.support.annotation.Nullable;

/**
 * Created by piyush on 01/02/17.
 */
public class TextUtils {
    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }
}
