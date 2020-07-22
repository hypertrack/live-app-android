package com.hypertrack.live.models;

import android.util.Log;

import com.hypertrack.live.App;
import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.backend.models.ShareableTrip;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TripModelTest {
    private static final String TAG = App.TAG + "TripModelTest";

    @Test
    public void itShouldCreateShareMessageWithoutEtaIfNoRemainingDurationWasSet() {
        ShareableTrip shareableTrip =
                new ShareableTrip("https://trck.at/someSubUrl", "", "dead-beef-1234", null);

        TripModel model = TripModel.fromShareableTrip(shareableTrip);

        String expected = "Track my live location here https://trck.at/someSubUrl";

        String got = model.getShareableMessage();
        Log.d(TAG, "Got message " + got);

        assertEquals(expected, got);
    }

    @Test
    public void itShouldCreateShareMessageWithEtaIfRemainingDurationWasSet() {
        ShareableTrip shareableTrip =
                new ShareableTrip("https://trck.at/someSubUrl", "", "dead-beef-1234", 42);

        TripModel model = TripModel.fromShareableTrip(shareableTrip);

        Pattern pattern = Pattern.compile("Will be there by \\d\\d?:\\d\\d\\w{2}. Track my live location here https://trck.at/someSubUrl");

        String got = model.getShareableMessage();
        Log.d(TAG, "Got message " + got);

        Matcher matcher = pattern.matcher(got);
        assertTrue(matcher.matches());
    }

    @Test
    public void itShouldCreateShareMessageWithEtaIfRemainingDurationWasReceivedOnUpdate() {
        String someRandomId = "dead-beef-1234";
        ShareableTrip shareableTrip =
                new ShareableTrip("https://trck.at/someSubUrl", "", someRandomId, null);

        TripModel model = TripModel.fromShareableTrip(shareableTrip);

        Trip trip = mock(Trip.class);
        Trip.Estimate estimate = mock(Trip.Estimate.class);
        Trip.Route route = mock(Trip.Route.class);
        when(route.getRemainingDuration()).thenReturn(42);
        when(estimate.getRoute()).thenReturn(route);
        when(trip.getEstimate()).thenReturn(estimate);
        when(trip.getTripId()).thenReturn(someRandomId);

        model.update(trip);

        Pattern pattern = Pattern.compile("Will be there by \\d\\d?:\\d\\d\\w{2}. Track my live location here https://trck.at/someSubUrl");

        String got = model.getShareableMessage();
        Log.d(TAG, "Got message " + got);
        Matcher matcher = pattern.matcher(got);
        assertTrue(matcher.matches());
    }

    @Test
    public void itShouldCreateShareMessageWithEtaIfRemainingDurationWasPresentWhenInitializedFromTrip() {

        String someRandomId = "dead-beef-1234";

        Trip trip = mock(Trip.class);
        Trip.Estimate estimate = mock(Trip.Estimate.class);
        Trip.Route route = mock(Trip.Route.class);
        when(route.getRemainingDuration()).thenReturn(42);
        when(estimate.getRoute()).thenReturn(route);
        when(trip.getEstimate()).thenReturn(estimate);
        when(trip.getTripId()).thenReturn(someRandomId);
        Trip.Views views = mock(Trip.Views.class);
        when(views.getSharedUrl()).thenReturn("https://trck.at/someSubUrl");
        when(trip.getViews()).thenReturn(views);

        TripModel model = TripModel.fromTrip(trip);
        assertNotNull(model);

        Pattern pattern = Pattern.compile("Will be there by \\d\\d?:\\d\\d\\w{2}. Track my live location here https://trck.at/someSubUrl");

        String got = model.getShareableMessage();
        Log.d(TAG, "Got message " + got);
        Matcher matcher = pattern.matcher(got);
        assertTrue(matcher.matches());
    }

}