package com.hypertrack.live.models;

import android.service.autofill.RegexValidator;

import com.hypertrack.trips.ShareableTrip;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Test
    public void itShouldCreateShareMessageWithEtaIfRemainingDurationWasSet() {
        ShareableTrip shareableTrip =
                new ShareableTrip("https://trck.at/someSubUrl", "", "dead-beef-1234", 42);

        TripModel model = TripModel.fromShareableTrip(shareableTrip);

        Pattern pattern = Pattern.compile("Will be there by \\d\\d?:\\d\\d\\w{2}. Track my live location here https://trck.at/someSubUrl");

        String got = model.getShareableMessage();
        Matcher matcher = pattern.matcher(got);
        assertTrue(matcher.matches());
    }
}