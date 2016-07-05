package io.hypertrack.sendeta.util.images;

import java.io.File;

/**
 * Created by suhas on 21/01/16.
 */
public class DefaultCallback implements EasyImage.Callbacks {

    @Override
    public void onImagePickerError(Exception e, EasyImage.ImageSource source) {}

    @Override
    public void onImagePicked(File imageFile, EasyImage.ImageSource source) {}

    @Override
    public void onCanceled(EasyImage.ImageSource source) {}
}