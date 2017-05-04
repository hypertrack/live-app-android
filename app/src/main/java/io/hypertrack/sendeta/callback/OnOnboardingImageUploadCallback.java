package io.hypertrack.sendeta.callback;

/**
 * Created by piyush on 04/07/16.
 */
public abstract class OnOnboardingImageUploadCallback {
    public abstract void onSuccess();

    public abstract void onError();

    public abstract void onImageUploadNotNeeded();
}
