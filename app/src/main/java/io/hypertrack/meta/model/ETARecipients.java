package io.hypertrack.meta.model;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by suhas on 11/01/16.
 */
public class ETARecipients {
    private String[] recipients;

    public String[] getRecipients() {
        return recipients;
    }

    public void setRecipients(String[] recipients) {
        this.recipients = recipients;
    }

    @Override
    public String toString() {
        return "ETARecipients{" +
                "recipients=" + Arrays.toString(recipients) +
                '}';
    }
}
