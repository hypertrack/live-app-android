package io.hypertrack.sendeta.model;

import java.util.Arrays;

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
