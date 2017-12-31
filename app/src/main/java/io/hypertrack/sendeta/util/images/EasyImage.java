/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.util.images;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suhas on 21/01/16.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal", "ResultOfMethodCallIgnored"})
public class EasyImage implements EasyImageConfig {

    private static final boolean SHOW_GALLERY_IN_CHOOSER = false;

    public enum ImageSource {
        GALLERY, DOCUMENTS, CAMERA
    }

    public interface Callbacks {
        void onImagePickerError(Exception e, ImageSource source);

        void onImagePicked(File imageFile, ImageSource source);

        void onCanceled(ImageSource source);
    }

    private static final String KEY_PHOTO_URI = "pl.aprilapps.easyphotopicker.photo_uri";
    private static final String KEY_LAST_CAMERA_PHOTO = "pl.aprilapps.easyphotopicker.last_photo";

    private static Intent createDocumentsIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        return intent;
    }

    private static Intent createGalleryIntent() {
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    private static Uri createCameraPictureFile(Context context) throws IOException {
        File imagePath = EasyImageFiles.getCameraPicturesLocation(context);
        Uri uri = Uri.fromFile(imagePath);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(KEY_PHOTO_URI, uri.toString());
        editor.putString(KEY_LAST_CAMERA_PHOTO, imagePath.toString());
        editor.apply();
        return uri;
    }

    private static Intent createCameraIntent(Context context) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri capturedImageUri = createCameraPictureFile(context);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return intent;
    }

    private static Intent createChooserIntent(Context context, String chooserTitle) throws IOException {
        return createChooserIntent(context, chooserTitle, SHOW_GALLERY_IN_CHOOSER);
    }

    private static Intent createChooserIntent(Context context, String chooserTitle, boolean showGallery) throws IOException {
        Uri outputFileUri = createCameraPictureFile(context);
        List<Intent> cameraIntents = new ArrayList<>();
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> camList = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : camList) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }
        Intent galleryIntent;

        if (showGallery) {
            galleryIntent = createGalleryIntent();
        } else {
            galleryIntent = createDocumentsIntent();
        }

        Intent chooserIntent = Intent.createChooser(galleryIntent, chooserTitle);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        return chooserIntent;
    }

    public static void openChooser(Activity activity, String chooserTitle) {
        try {
            Intent intent = createChooserIntent(activity, chooserTitle);
            activity.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooser(Fragment fragment, String chooserTitle) {
        try {
            Intent intent = createChooserIntent(fragment.getActivity(), chooserTitle);
            fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooser(android.app.Fragment fragment, String chooserTitle) {
        try {
            Intent intent = createChooserIntent(fragment.getActivity(), chooserTitle);
            fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooser(Activity activity, String chooserTitle, boolean showGallery) {
        try {
            Intent intent = createChooserIntent(activity, chooserTitle, showGallery);
            activity.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooser(Fragment fragment, String chooserTitle, boolean showGallery) {
        try {
            Intent intent = createChooserIntent(fragment.getActivity(), chooserTitle, showGallery);
            fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openChooser(android.app.Fragment fragment, String chooserTitle, boolean showGallery) {
        try {
            Intent intent = createChooserIntent(fragment.getActivity(), chooserTitle, showGallery);
            fragment.startActivityForResult(intent, REQ_SOURCE_CHOOSER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openDocuments(Activity activity) {
        Intent intent = createDocumentsIntent();
        activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_DOCUMENTS);
    }

    public static void openDocuments(Fragment fragment) {
        Intent intent = createDocumentsIntent();
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_DOCUMENTS);
    }

    public static void openDocuments(android.app.Fragment fragment) {
        Intent intent = createDocumentsIntent();
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_DOCUMENTS);
    }

    public static void openGallery(Activity activity) {
        Intent intent = createGalleryIntent();
        activity.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openGallery(Fragment fragment) {
        Intent intent = createGalleryIntent();
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openGallery(android.app.Fragment fragment) {
        Intent intent = createGalleryIntent();
        fragment.startActivityForResult(intent, REQ_PICK_PICTURE_FROM_GALLERY);
    }

    public static void openCamera(Activity activity) {
        Intent intent = createCameraIntent(activity);
        activity.startActivityForResult(intent, REQ_TAKE_PICTURE);
    }

    public static void openCamera(Fragment fragment) {
        Intent intent = createCameraIntent(fragment.getActivity());
        fragment.startActivityForResult(intent, REQ_TAKE_PICTURE);
    }

    public static void openCamera(android.app.Fragment fragment) {
        Intent intent = createCameraIntent(fragment.getActivity());
        fragment.startActivityForResult(intent, REQ_TAKE_PICTURE);
    }


    private static File takenCameraPicture(Context context) throws IOException, URISyntaxException {
        @SuppressWarnings("ConstantConditions")
        URI imageUri = new URI(PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PHOTO_URI, null));
        notifyGallery(context, imageUri);
        return new File(imageUri);
    }


    private static void notifyGallery(Context context, URI pictureUri) throws URISyntaxException {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(pictureUri);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    public static void handleActivityResult(int requestCode, int resultCode, Intent data, Activity activity, Callbacks callbacks) {
        if (requestCode == EasyImageConfig.REQ_SOURCE_CHOOSER || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY || requestCode == EasyImageConfig.REQ_TAKE_PICTURE || requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {
                    onPictureReturnedFromDocuments(data, activity, callbacks);
                } else if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY) {
                    onPictureReturnedFromGallery(data, activity, callbacks);
                } else if (requestCode == EasyImageConfig.REQ_TAKE_PICTURE) {
                    onPictureReturnedFromCamera(activity, callbacks);
                } else if (data == null || data.getData() == null) {
                    onPictureReturnedFromCamera(activity, callbacks);
                } else {
                    onPictureReturnedFromDocuments(data, activity, callbacks);
                }
            } else {
                if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_DOCUMENTS) {
                    callbacks.onCanceled(ImageSource.DOCUMENTS);
                } else if (requestCode == EasyImageConfig.REQ_PICK_PICTURE_FROM_GALLERY) {
                    callbacks.onCanceled(ImageSource.GALLERY);
                } else if (requestCode == EasyImageConfig.REQ_TAKE_PICTURE) {
                    callbacks.onCanceled(ImageSource.CAMERA);
                } else if (data == null || data.getData() == null) {
                    callbacks.onCanceled(ImageSource.CAMERA);
                } else {
                    callbacks.onCanceled(ImageSource.DOCUMENTS);
                }
            }
        }
    }

    /**
     * @param context context
     * @return File containing lastly taken (using camera) photo. Returns null if there was no photo taken or it doesn't exist anymore.
     */
    public static File lastlyTakenButCanceledPhoto(Context context) {
        String filePath = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LAST_CAMERA_PHOTO, null);
        if (filePath == null) return null;
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    private static void onPictureReturnedFromDocuments(Intent data, Activity activity, Callbacks callbacks) {
        try {
            Uri photoPath = data.getData();
            File photoFile = EasyImageFiles.pickedExistingPicture(activity, photoPath);
            callbacks.onImagePicked(photoFile, ImageSource.DOCUMENTS);
        } catch (Exception e) {
            e.printStackTrace();
            callbacks.onImagePickerError(e, ImageSource.DOCUMENTS);
        }
    }

    private static void onPictureReturnedFromGallery(Intent data, Activity activity, Callbacks callbacks) {
        try {
            Uri photoPath = data.getData();
            File photoFile = EasyImageFiles.pickedExistingPicture(activity, photoPath);
            callbacks.onImagePicked(photoFile, ImageSource.GALLERY);
        } catch (Exception e) {
            e.printStackTrace();
            callbacks.onImagePickerError(e, ImageSource.GALLERY);
        }
    }

    private static void onPictureReturnedFromCamera(Activity activity, Callbacks callbacks) {
        try {
            File photoFile = EasyImage.takenCameraPicture(activity);
            callbacks.onImagePicked(photoFile, ImageSource.CAMERA);
            PreferenceManager.getDefaultSharedPreferences(activity).edit().remove(KEY_LAST_CAMERA_PHOTO).apply();
        } catch (Exception e) {
            e.printStackTrace();
            callbacks.onImagePickerError(e, ImageSource.CAMERA);
        }
    }

    public static void clearPublicTemp(Context context) {
        List<File> tempFiles = new ArrayList<>();
        File[] files = EasyImageFiles.publicTemplDir(context).listFiles();
        for (File file : files) {
            file.delete();
        }
    }


    /**
     * Method to clear configuration. Would likely be used in onDestroy(), onDestroyView()...
     *
     * @param context context
     */
    public static void clearConfiguration(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .remove(BundleKeys.FOLDER_NAME)
                .remove(BundleKeys.FOLDER_LOCATION)
                .remove(BundleKeys.PUBLIC_TEMP)
                .apply();
    }

    public static Configuration configuration(Context context) {
        return new Configuration(context);
    }

    public static class Configuration {
        private Context context;

        private Configuration(Context context) {
            this.context = context;
        }

        public Configuration setImagesFolderName(String folderName) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putString(BundleKeys.FOLDER_NAME, folderName)
                    .apply();
            return this;
        }

        public Configuration saveInRootPicturesDirectory() {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString(BundleKeys.FOLDER_LOCATION, EasyImageFiles.publicRootDir().toString())
                    .apply();
            return this;
        }

        public Configuration saveInAppExternalFilesDir() {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString(BundleKeys.FOLDER_LOCATION, EasyImageFiles.publicAppExternalDir(context).toString())
                    .apply();
            return this;
        }


        /**
         * Use this method if you want your picked gallery or documents pictures to be duplicated into public, other apps accessible, directory.
         * You'll have to take care of removing that file on your own after you're done with it. Use EasyImage.clearPublicTemp() method for that.
         * If you don't delete them they could show up in user galleries.
         *
         * @return modified Configuration object
         */
        public Configuration setCopyExistingPicturesToPublicLocation(boolean copyToPublicLocation) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(BundleKeys.PUBLIC_TEMP, copyToPublicLocation)
                    .apply();
            return this;
        }
    }
}