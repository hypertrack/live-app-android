package io.hypertrack.sendeta.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import java.io.File;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.presenter.IProfilePresenter;
import io.hypertrack.sendeta.presenter.ProfilePresenter;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.ImageUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.images.DefaultCallback;
import io.hypertrack.sendeta.util.images.EasyImage;
import io.hypertrack.sendeta.util.images.RoundedImageView;

public class Profile extends BaseActivity implements ProfileView {

    public AutoCompleteTextView mFirstNameView, mLastNameView;
    public RoundedImageView mProfileImageView;
    public ProgressBar mProfileImageLoader;
    private ProgressDialog mProgressDialog;

    private File profileImage;

    private IProfilePresenter<ProfileView> presenter = new ProfilePresenter();

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

        // Initialize Toolbar
        initToolbar("Profile");

        // Initialize UI Views before Attaching View Presenter
        mFirstNameView = (AutoCompleteTextView) findViewById(R.id.profile_first_name);
        mLastNameView = (AutoCompleteTextView) findViewById(R.id.profile_last_name);
        mProfileImageView = (RoundedImageView) findViewById(R.id.profile_image_view);
        mProfileImageLoader = (ProgressBar) findViewById(R.id.profile_image_loader);

        // Attach View Presenter to View
        presenter.attachView(this);

        // Initialize UI Action Listeners
        mFirstNameView.setOnEditorActionListener(mFirstNameEditorActionListener);
        mLastNameView.setOnEditorActionListener(mLastNameEditorActionListener);

        // Check & Request for READ_EXTERNAL_STORAGE permission, if not available
        PermissionUtils.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public void onProfileImageViewClicked(View view) {
        EasyImage.openChooser(Profile.this, "Please select", true);
    }

    private void onSignInButtonClicked() {
        showProgress(true);

        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();

        presenter.attemptLogin(firstName, lastName, profileImage);
    }

    public void onNextButtonClicked(MenuItem menuItem) {
        this.onSignInButtonClicked();
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

                    profileImage = ImageUtils.getFileFromBitmap(Profile.this, bitmap);
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
    public void showProfilePicUploadSuccess() {
        showProgress(false);
        Toast.makeText(Profile.this, "Profile Pic uploaded successfully", Toast.LENGTH_SHORT).show();

        // Complete User Signup on Profile Pic Upload Success
        navigateToHomeScreen();
    }

    @Override
    public void showProfilePicUploadError() {
        showProgress(false);
        Toast.makeText(Profile.this, ErrorMessages.PROFILE_PIC_UPLOAD_FAILED, Toast.LENGTH_SHORT).show();

        // TODO: 30/06/16 Add Background Upload of Image

        // Complete User Signup on Profile Pic Upload Failure
        navigateToHomeScreen();
    }

    public void navigateToHomeScreen() {
        Intent intent = new Intent(Profile.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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

    @Override
    public void showErrorMessage() {
        showProgress(false);
        Toast.makeText(Profile.this, ErrorMessages.PROFILE_UPDATE_FAILED, Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source) {
                Toast.makeText(Profile.this, ErrorMessages.PROFILE_PIC_CHOOSE_FAILED, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source) {
                if (imageFile == null) {
                    return;
                }

                profileImage = ImageUtils.getScaledFile(imageFile);

                Bitmap bitmap = ImageUtils.getRotatedBitMap(imageFile);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this,"Permission Granted, Now you can access location data.",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denied, You cannot access storage.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        // Detach View Presenter from View
        presenter.detachView();
        super.onDestroy();
    }
}