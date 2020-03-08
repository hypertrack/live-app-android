package com.hypertrack.live.models;

import com.hypertrack.trips.ShareableTrip;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class TripModelTest {
    @Test
    public void itShouldCreateShareMessageWithoutEtaIfNoRemainingDurationWasSet() {
        ShareableTrip shareableTrip =
                new ShareableTrip("https://trck.at/someSubUrl", "", "dead-beef-1234", null);

        TripModel model = TripModel.fromShareableTrip(shareableTrip);

        String expected = "Track my live location here https://trck.at/someSubUrl";

        String got = model.getShareableMessage();
        assertEquals(expected, got);
    }
}