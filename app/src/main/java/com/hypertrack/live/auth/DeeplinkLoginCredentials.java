package com.hypertrack.live.auth;

import android.util.Log;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.hypertrack.live.HTMobileClient;
import com.hypertrack.live.LaunchActivity;

import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class DeeplinkLoginCredentials {

    private static final String TAG = "BranchLoginCredentials";

    @NonNull private final LaunchActivity mLaunchActivity;
    @NonNull private final HTMobileClient mHtMobileClient;

    private final Object credentialsLock = new Object();
    @GuardedBy("credentialsLock")
    @NonNull private String email = "";
    @GuardedBy("credentialsLock")
    @NonNull private String password = "";
    @GuardedBy("credentialsLock")
    private boolean isDeeplinkFinished = false;

    @GuardedBy("credentialsLock")
    private boolean isCancelled = false;
    @GuardedBy("credentialsLock")
    private Runnable fallback;

    public DeeplinkLoginCredentials(@NonNull LaunchActivity launchActivity, @NonNull HTMobileClient htMobileClient) {
        mLaunchActivity = launchActivity;
        mHtMobileClient = htMobileClient;
    }

    public void setCancelled() {
        synchronized (credentialsLock) {
            isCancelled = true;
        }
    }

    public void savedLoginFailed(Runnable fallback) {
        if (isDeeplinkFinished) {
            this.fallback = fallback;
            login();
            return;
        }
        synchronized (credentialsLock) {
            this.fallback = fallback;
            login();
        }
    }

    public Branch.BranchReferralInitListener getDeeplinkListener() {
        return new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(@Nullable JSONObject referringParams, @Nullable BranchError error) {
                if (isCancelled) {
                    Log.d(TAG, "onInitFinished: cancelled. Ignoring");
                    return;
                }

                Log.d(TAG, "Got referring Params " + (referringParams != null ? referringParams.toString() : "null"));
                String login = "";
                String pwd = "";
                if (referringParams != null) {
                    login = referringParams.optString("email");
                    pwd = referringParams.optString("pwd");
                }

                if (error != null) {
                    Log.d(TAG, "Got error in referring Params " + error.getMessage());
                }

                if (fallback != null) {
                    // AWS client already finished, no need to synchronize
                    processCredentials(login, pwd);
                    return;
                }
                synchronized (credentialsLock) {
                    processCredentials(login, pwd);
                }

            }
        };
    }

    private void processCredentials(String login, String pwd) {
        if (isCancelled) return;
        isDeeplinkFinished = true;
        email = login;
        password = pwd;
        Log.d(TAG, String.format("login params: %s : %s", email, password));

        if (fallback != null) {
            // AWS already finished
            login();
        }
        // NOOP for pending AWS task, as it might use or ignore credentials
    }

    private void login() {
        if (email.isEmpty() || password.isEmpty()) {
            Log.d(TAG, "onInitFinished: Got empty login credentials. No auto sign-in occurs.");
            fallback.run();
        } else {
            mLaunchActivity.showLoader();
            mHtMobileClient.signIn(email, password, new HTMobileClient.Callback() {
                @Override
                public void onSuccess(HTMobileClient mobileClient) {
                    mLaunchActivity.removeLoader();
                    mLaunchActivity.onLoginCompleted();
                }

                @Override
                public void onError(String message, Exception e) {
                    Log.w(TAG, "onError: " + message, e);
                    mLaunchActivity.showError(e);
                    fallback.run();
                }
            });
        }
    }

}
