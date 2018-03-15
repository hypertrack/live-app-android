package io.hypertrack.sendeta.util;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AppDeepLink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
/**
 * Created by piyush on 12/05/17.
 */
public class DeepLinkUtilTest {
    private Context context;
    private Uri uri;

    @Before
    public void setUp() throws Exception {
        context = Mockito.mock(Context.class);
        uri = getUri(null);
    }

    @Test
    public void prepareAppDeepLinkTestWhenUriIsNull() throws Exception {
        AppDeepLink appDeepLink = DeepLinkUtil.prepareAppDeepLink(context, null);
        assertNotNull(appDeepLink);
        assertEquals(DeepLinkUtil.DEFAULT, appDeepLink.mId);
        assertNull(appDeepLink.taskID);
        assertNull(appDeepLink.uniqueId);
        assertNull(appDeepLink.shortCode);
    }

    @Test
    public void prepareAppDeepLinkTestWhenUriIsNotNull() throws Exception {
        when(context.getString(R.string.tracking_url)).thenReturn("www.trck.at");

        AppDeepLink appDeepLink = DeepLinkUtil.prepareAppDeepLink(context, uri);
        assertNotNull(appDeepLink);
        assertEquals(DeepLinkUtil.TRACK, appDeepLink.mId);
        assertNull(appDeepLink.taskID);
        assertNull(appDeepLink.uniqueId);
        assertNotNull(appDeepLink.shortCode);
    }

    private Uri getUri(String uri) {
        if (TextUtils.isEmpty(uri)) {
            uri = "https://www.trck.at/HaCXdF";
        }

        return Uri.parse(uri);
    }
}