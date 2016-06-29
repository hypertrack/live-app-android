package io.hypertrack.meta.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.R;
import io.hypertrack.meta.presenter.IProfilePresenter;
import io.hypertrack.meta.presenter.ProfilePresenter;
import io.hypertrack.meta.store.AnalyticsStore;
import io.hypertrack.meta.util.ErrorMessages;
import io.hypertrack.meta.util.images.DefaultCallback;
import io.hypertrack.meta.util.images.EasyImage;
import io.hypertrack.meta.util.images.RoundedImageView;

public class Profile extends AppCompatActivity implements ProfileView {

    // UI references.
    @Bind(R.id.profile_first_name)
    public AutoCompleteTextView mFirstNameView;

    @Bind(R.id.profile_last_name)
    public AutoCompleteTextView mLastNameView;

    @Bind(R.id.profile_image_view)
    public RoundedImageView mProfileImageView;

    @OnClick(R.id.profile_image_view)
    public void onImageButtonClicked() {
        EasyImage.openChooser(Profile.this, "Please select", true);
    }

    @Bind(R.id.profile_image_loader)
    public ProgressBar mProfileImageLoader;

    private File profileImage;

    private ProgressDialog mProgressDialog;
    private IProfilePresenter<ProfileView> presenter = new ProfilePresenter();

    private static final int PERMISSION_REQUEST_CODE = 1;

    private TextView.OnEditorActionListener mFirstNameEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mLastNameView.requestFocus();
                return true;
            }

            return false;
        }
    };

    private TextView.OnEditorActionListener mLastNameEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Profile.this.onSignInButtonClicked();
                return true;
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ButterKnife.bind(this);
        presenter.attachView(this);

        mFirstNameView.setOnEditorActionListener(mFirstNameEditorActionListener);
        mLastNameView.setOnEditorActionListener(mLastNameEditorActionListener);

        if (!checkPermission()) {
            requestPermission();
        }
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    private void onSignInButtonClicked() {
        showProgress(true);

        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();

        presenter.attemptLogin(firstName, lastName, profileImage);
    }

    @Override
    public void updateViews(String firstName, String lastName, String profileURL) {
        if (firstName != null && !firstName.isEmpty()) {
            mFirstNameView.setText(firstName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            mLastNameView.setText(lastName);
        }

        if (profileURL != null && !profileURL.isEmpty()) {
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d("Profile", "onBitmapLoaded called!");
                    profileImage = getFileFromBitmap(bitmap);
                    mProfileImageView.setImageBitmap(bitmap);
                    mProfileImageLoader.setVisibility(View.GONE);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.d("Profile", "onBitmapFailed called!");
                    mProfileImageLoader.setVisibility(View.GONE);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Log.d("Profile", "onPrepareLoad called!");
                }
            };

            mProfileImageLoader.setVisibility(View.VISIBLE);
            Picasso.with(this)
                    .load(profileURL)
                    .placeholder(R.drawable.default_profile_pic)
                    .error(R.drawable.default_profile_pic)
                    .into(target);
            mProfileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source) {
                if (imageFile == null) {
                    return;
                }

                profileImage = getScaledFile(getScaledFile(imageFile));

                Bitmap bitmap = getRotatedBitMap(imageFile);
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                }

                if (bitmap != null) {
                    mProfileImageView.setImageBitmap(bitmap);
                }

                mProfileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        });
    }

    private Bitmap getRotatedBitMap(File imageFile) {
        if (imageFile == null) {
            return null;
        }

        Bitmap rotatedBitmap = null;
        Bitmap srcBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        try {
            ExifInterface exif = new ExifInterface(imageFile.getName());
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle, (float) srcBitmap.getWidth() / 2, (float) srcBitmap.getHeight() / 2);
            rotatedBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotatedBitmap;
    }

    private boolean checkPermission() {

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Storage access permission allows read and write image files related to you profile pic. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this,"Permission Granted, Now you can access location data.",Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Permission Denied, You cannot access storage.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private File getScaledFile(File file) {
        try {
            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void showProfilePicUploadError() {
        showProgress(false);
        Toast.makeText(Profile.this, ErrorMessages.PROFILE_PIC_UPLOAD_FAILED, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToHomeScreen() {
        showProgress(false);
        Toast.makeText(Profile.this, "Profile Pic uploaded successfully", Toast.LENGTH_SHORT).show();

        // TODO: 30/06/16 Move this event to proper place
        AnalyticsStore.getLogger().completedProfileSetUp(false);

        Intent intent = new Intent(Profile.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void showErrorMessage() {
        showProgress(false);
        Toast.makeText(Profile.this, ErrorMessages.PROFILE_UPDATE_FAILED, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showFirstNameValidationError() {
        showProgress(false);
        mFirstNameView.setError(getString(R.string.error_field_required));
        mFirstNameView.requestFocus();
    }

    @Override
    public void showLastNameValidationError() {
        showProgress(false);
        mLastNameView.setError(getString(R.string.error_field_required));
        mLastNameView.requestFocus();
    }

    private void showProgress(boolean show) {
        if (show) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.registration_phone_number));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Action bar menu methods
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onNextButtonClicked(MenuItem menuItem) {
        this.onSignInButtonClicked();
    }

    private File getFileFromBitmap(Bitmap bitmap) {
        try {
            File file = new File(this.getCacheDir(), "temp");
            if (!file.createNewFile()) {
                return null;
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            byte[] bitmapData = stream.toByteArray();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bitmapData);
            fileOutputStream.flush();
            fileOutputStream.close();

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //back button inside toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return false;
    }
}

