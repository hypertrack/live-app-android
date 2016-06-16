package io.hypertrack.meta.view;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.BuildConfig;
import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.R;
import io.hypertrack.meta.SavedPlacesAdapter;
import io.hypertrack.meta.model.Place;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.retrofit.SendEtaService;
import io.hypertrack.meta.network.retrofit.ServiceGenerator;
import io.hypertrack.meta.presenter.IProfilePresenter;
import io.hypertrack.meta.presenter.ProfilePresenter;
import io.hypertrack.meta.util.Constants;
import io.hypertrack.meta.util.SharedPreferenceManager;
import io.hypertrack.meta.util.images.DefaultCallback;
import io.hypertrack.meta.util.images.EasyImage;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile extends AppCompatActivity implements ProfileView {

    private static final String TAG = Profile.class.getSimpleName();
    private static final int MAX_IMAGE_DIMENSION = 400;

    // UI references.
    @Bind(R.id.firstName)
    public AutoCompleteTextView mFirstNameView;

    @Bind(R.id.lastName)
    public AutoCompleteTextView mLastNameView;

    @Bind(R.id.profileImageView)
    public ImageButton mProfileImageButton;

    private File profileImage;

    private SharedPreferenceManager sharedPreferenceManager;
    private IProfilePresenter<ProfileView> presenter = new ProfilePresenter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Profile");

        ButterKnife.bind(this);

        sharedPreferenceManager = new SharedPreferenceManager(MetaApplication.getInstance());
        presenter.attachView(this);

        populateAutoComplete();

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
        int userId = sharedPreferenceManager.getUserId();

        presenter.attemptLogin(firstName, lastName, userId, saveBitmapToFile(profileImage));
    }

    @OnClick(R.id.profileImageView)
    public void onImageButtonClicked() {
        EasyImage.openChooser(Profile.this, "Please select", true);
    }

    private void populateAutoComplete() {

        String userFirstName = sharedPreferenceManager.getFirstName();
        String userLastName = sharedPreferenceManager.getLastName();
        String urlProfilePic = sharedPreferenceManager.getUserPhoto();

        if (!TextUtils.equals(userFirstName, Constants.DEFAULT_STRING_VALUE)) {
            mFirstNameView.setText(userFirstName);
        }

        if (!TextUtils.equals(userLastName, Constants.DEFAULT_STRING_VALUE)) {
            mLastNameView.setText(userLastName);
        }

        if (!TextUtils.equals(urlProfilePic, Constants.DEFAULT_STRING_VALUE)) {
            Picasso.with(this)
                    .load(urlProfilePic)
                    .placeholder(R.drawable.default_profile_pic) // optional
                    .error(R.drawable.default_profile_pic)         // optional
                    .into(mProfileImageButton);
        }

        // Modify Profile Screen UI for a LoggedIn User
        if (sharedPreferenceManager.isUserLoggedIn()) {

            // Set Linear Layout Manager for Recycler View
//            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//            linearLayoutManager.setAutoMeasureEnabled(true);
//            mRecyclerView.setLayoutManager(linearLayoutManager);
//
//            mRecyclerView.setAdapter(new SavedPlacesAdapter(sharedPreferenceManager.getSavedPlaces()));
//
//            // Make User's Saved Locations Recycler View Visible
//            mRecyclerView.setVisibility(View.VISIBLE);

        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mProfileFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mProfileFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProfileFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProfileFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
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
                //Handle the image
                profileImage = imageFile;
                Bitmap srcBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                ExifInterface exif = null;
                try {

                    exif = new ExifInterface(imageFile.getName());
                    String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                    int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

                    int rotationAngle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

                    Matrix matrix = new Matrix();
                    matrix.setRotate(rotationAngle, (float) srcBitmap.getWidth() / 2, (float) srcBitmap.getHeight() / 2);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
                    mProfileImageButton.setImageBitmap(rotatedBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static final int PERMISSION_REQUEST_CODE = 1;

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

    public File saveBitmapToFile(File file) {
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
            saveImageToPreferences(encodeToBase64(selectedBitmap));

            return file;
        } catch (Exception e) {
            return null;
        }

    }

    public static String encodeToBase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    private void saveImageToPreferences(String encodedImage) {
        sharedPreferenceManager.setProfileImage(encodedImage);
    }

    public void updateUserProfileRetro(String firstName, String lastName) {

        SendEtaService sendEtaService = ServiceGenerator.createService(SendEtaService.class, BuildConfig.API_KEY);

        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int userId = settings.getInt(Constants.USER_ID, -1);

        User user = new User(firstName, lastName);

        Call<User> call = sendEtaService.updateUserName(String.valueOf(userId), user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (response.isSuccessful()) {
                    Log.v(TAG, "Response from Retrofit");

                    Log.d("Response", "User :" + response.body().toString());

                    SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Constants.HYPERTRACK_DRIVER_ID, response.body().getHypertrackDriverID());
                    editor.putString(Constants.USER_PROFILE_PIC, response.body().getPhoto());
                    editor.putBoolean("isUserOnboard", true);
                    editor.apply();

                    showProgress(false);

                    //Upload photo
                    if (profileImage != null)
                        updateUserProfilePic();

                    Intent intent = new Intent(Profile.this, Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(Profile.this, "We had problem connecting with the server. Please try again in sometime.", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.v(TAG, "Inside error block of retrofit. " + t.getMessage());
            }
        });

    }

    public void updateUserProfilePic() {

        SendEtaService sendEtaService = ServiceGenerator.createService(SendEtaService.class, BuildConfig.API_KEY);

        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int userId = settings.getInt(Constants.USER_ID, -1);

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("image/*"), saveBitmapToFile(profileImage));

        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        String fileName = "photo\"; filename=\"" + uuid + ".jpg";
        requestBodyMap.put(fileName, requestBody);

        Call<User> updatePicCall = sendEtaService.updateUserProfilePic(String.valueOf(userId), requestBodyMap);
        updatePicCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.v(TAG, "Pic updated successfully");
                    Log.v(TAG, response.headers().toString());

                    SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(Constants.USER_PROFILE_PIC, response.body().getPhoto());
                    editor.apply();
                } else {
                    Log.d(TAG, "User profile could not be uploaded");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.v(TAG, "Error while updating profile pic. " + t.getMessage());
            }
        });
    }

    @Override
    public void navigateToHomeScreen() {

        showProgress(false);

        Intent intent = new Intent(Profile.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void showErrorMessage() {

        showProgress(false);
        Toast.makeText(Profile.this, "We had problem connecting with the server. Please try again in sometime.", Toast.LENGTH_LONG).show();
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

    /**
     * Action bar menu methods
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);

        if (sharedPreferenceManager != null && sharedPreferenceManager.isUserLoggedIn()) {
            menu.getItem(0).setTitle("Next");
            invalidateOptionsMenu();
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void onNextButtonClicked(MenuItem menuItem) {
        if (sharedPreferenceManager != null && sharedPreferenceManager.isUserLoggedIn()) {
            // Implement Next Functionality here
        } else {
            this.onSignInButtonClicked();
        }
    }
}

