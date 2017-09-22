package io.hypertrack.sendeta.view;

import android.widget.FrameLayout;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
/**
 * Created by piyush on 12/05/17.
 */
public class TrackTest {

    private Track activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(Track.class)
                .create()
                .resume()
                .get();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertNotNull(activity);
    }

    @Test
    public void shouldHaveHyperTrackMapFragment() throws Exception {
        assertNotNull(activity.getSupportFragmentManager().findFragmentById(R.id.map_fragment));
    }

    @Test
    public void shouldHaveRetryButton() throws Exception {
        assertNotNull(activity.findViewById(R.id.retryButton));
    }

    @Test
    public void shouldHaveDefaultMargin() throws Exception {
        TextView textView = (TextView) activity.findViewById(R.id.retryButton);
        int bottomMargin = ((FrameLayout.LayoutParams) textView.getLayoutParams()).bottomMargin;
        assertEquals(16, bottomMargin);
        int topMargin = ((FrameLayout.LayoutParams) textView.getLayoutParams()).topMargin;
        assertEquals(16, topMargin);
        int rightMargin = ((FrameLayout.LayoutParams) textView.getLayoutParams()).rightMargin;
        assertEquals(16, rightMargin);
        int leftMargin = ((FrameLayout.LayoutParams) textView.getLayoutParams()).leftMargin;
        assertEquals(16, leftMargin);
    }

    @Test
    public void shouldHaveCorrectAppName() throws Exception {
        String hello = activity.getResources().getString(R.string.app_name);
        assertThat(hello, equalTo("HyperTrack Live"));
    }
}
