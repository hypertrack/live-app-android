package com.hypertrack.lib.internal.transmitter.controls;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.common.util.TextUtils;

import java.io.Serializable;

/**
 * Created by Arjun on 23/05/16.
 */
public class SDKControls implements Serializable {

    public class RunCommand {
        public static final String GO_ONLINE = "GO_ONLINE";
        public static final String GO_ACTIVE = "GO_ACTIVE";
        public static final String GO_OFFLINE = "GO_OFFLINE";
        public static final String FLUSH = "FLUSH";
    }

    /**
     * Id of User to be controlled.
     */
    @SerializedName("user_id")
    private String userID;

    /**
     * Run command
     */
    @SerializedName("run_command")
    private String runCommand;

    /**
     * Batch duration (in seconds) for periodic task intervals.
     */
    @SerializedName("batch_duration")
    private Integer batchDuration;

    /**
     * Time to life value for server configured batch-duration
     */
    @SerializedName("ttl")
    private Integer ttl = null;

    /**
     * Minimum duration (in seconds) for location updates.
     */
    @SerializedName("minimum_duration")
    private Integer minimumDuration;

    /**
     * Minimum displacement (in meters) for location updates.
     */
    @SerializedName("minimum_displacement")
    private Integer minimumDisplacement;

    SDKControls(String userID, String runCommand, Integer batchDuration, Integer minimumDuration,
                Integer minimumDisplacement) {
        this.userID = userID;
        this.runCommand = runCommand;
        this.batchDuration = batchDuration;
        this.minimumDuration = minimumDuration;
        this.minimumDisplacement = minimumDisplacement;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(String runCommand) {
        this.runCommand = runCommand;
    }

    public Integer getBatchDuration() {
        return batchDuration;
    }

    void setBatchDuration(Integer batchDuration) {
        this.batchDuration = batchDuration;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public Integer getMinimumDuration() {
        return minimumDuration;
    }

    void setMinimumDuration(Integer minimumDuration) {
        this.minimumDuration = minimumDuration;
    }

    public Integer getMinimumDisplacement() {
        return minimumDisplacement;
    }

    void setMinimumDisplacement(Integer minimumDisplacement) {
        this.minimumDisplacement = minimumDisplacement;
    }

    public boolean isGoOfflineCommand() {
        return (!TextUtils.isEmpty(this.runCommand) && this.runCommand.equalsIgnoreCase(RunCommand.GO_OFFLINE));
    }

    public boolean isFlushDataCommand() {
        return (!TextUtils.isEmpty(this.runCommand) && this.runCommand.equalsIgnoreCase(RunCommand.FLUSH));
    }

    public boolean isGoActiveCommand() {
        return (!TextUtils.isEmpty(this.runCommand) && this.runCommand.equalsIgnoreCase(RunCommand.GO_ACTIVE));
    }

    public boolean isGoOnlineCommand() {
        return (!TextUtils.isEmpty(this.runCommand) && this.runCommand.equalsIgnoreCase(RunCommand.GO_ONLINE));
    }

    @Override
    public String toString() {
        return "SDKControls{" +
                "userID='" + userID + '\'' +
                ", runCommand='" + runCommand + '\'' +
                ", batchDuration=" + batchDuration +
                ", ttl=" + ttl +
                ", minimumDuration=" + minimumDuration +
                ", minimumDisplacement=" + minimumDisplacement +
                '}';
    }
}
