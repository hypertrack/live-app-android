package com.hypertrack.live.models;

import android.service.autofill.RegexValidator;
import android.util.Log;

import com.hypertrack.sdk.views.dao.Trip;
import com.hypertrack.trips.ShareableTrip;

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
    private static final String TAG = "TripModelTest";

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
        String someRandomStuff = "dead-beef-1234";
        ShareableTrip shareableTrip =
                new ShareableTrip("https://trck.at/someSubUrl", "", someRandomStuff, null);

        TripModel model = TripModel.fromShareableTrip(shareableTrip);

        Trip trip = mock(Trip.class);
        Trip.Estimate estimate = mock(Trip.Estimate.class);
        Trip.Route route = mock(Trip.Route.class);
        when(route.getRemainingDuration()).thenReturn(42);
        when(estimate.getRoute()).thenReturn(route);
        when(trip.getEstimate()).thenReturn(estimate);
        when(trip.getTripId()).thenReturn(someRandomStuff);

        model.update(trip);

        Pattern pattern = Pattern.compile("Will be there by \\d\\d?:\\d\\d\\w{2}. Track my live location here https://trck.at/someSubUrl");

        String got = model.getShareableMessage();
        Log.d(TAG, "Got message " + got);
        Matcher matcher = pattern.matcher(got);
        assertTrue(matcher.matches());
    }

}