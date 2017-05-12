package io.hypertrack.sendeta.util;

import android.content.Context;
import android.net.Uri;

import com.hypertrack.lib.internal.common.util.TextUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.model.AppDeepLink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricGradleTestRunner.class)
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
        assertNull(appDeepLink.lookupId);
        assertNull(appDeepLink.shortCode);
    }

    private Uri getUri(String uri) {
        if (TextUtils.isEmpty(uri)) {
            uri = "https://www.trck.at/HaCXdF";
        }

        return Uri.parse(uri);
    }
}