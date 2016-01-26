package io.hypertrack.meta;

import android.*;
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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.network.retrofit.ServiceGenerator;
import io.hypertrack.meta.network.retrofit.UserService;
import io.hypertrack.meta.util.HTConstants;
import io.hypertrack.meta.util.images.DefaultCallback;
import io.hypertrack.meta.util.images.EasyImage;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * A login screen that offers login via email/password.
 */
public class Profile extends AppCompatActivity {

    private static final String TAG = Profile.class.getSimpleName();

    // UI references.
    private AutoCompleteTextView mFirstNameView;
    private EditText mLastNameView;
    private View mProgressView;
    private View mProfileFormView;
    private ImageButton mProfileImageButton;
    private File profileImage;

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data; boundary=" + boundary;
    private byte[] multipartBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // Set up the login form.
        mProfileImageButton = (ImageButton) findViewById(R.id.profileImageView);
        mFirstNameView = (AutoCompleteTextView) findViewById(R.id.firstName);

        mLastNameView = (EditText) findViewById(R.id.lastName);

        initViews();
        populateAutoComplete();

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProfileFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if (checkPermission()) {

        } else {
            requestPermission();
        }
    }

    private void initViews() {

        mProfileImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyImage.openChooser(Profile.this, "Please select", true);
            }
        });
    }

    private void populateAutoComplete() {

        SharedPreferences settings = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String userFirstName =  settings.getString(HTConstants.USER_FIRSTNAME, null);
        String userLastName =  settings.getString(HTConstants.USER_LASTNAME, null);
        String urlProfilePic = settings.getString(HTConstants.USER_PROFILE_PIC, null);

        if(!TextUtils.isEmpty(userFirstName)) {
            mFirstNameView.setText(userFirstName);
        }

        if(!TextUtils.isEmpty(userLastName)) {
            mLastNameView.setText(userLastName);
        }

        if(!TextUtils.isEmpty(urlProfilePic)) {
            Picasso.with(this)
                    .load(urlProfilePic)
                    .placeholder(R.drawable.default_profile_pic) // optional
                    .error(R.drawable.default_profile_pic)         // optional
                    .into(mProfileImageButton);
        }

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mFirstNameView.setError(null);
        mLastNameView.setError(null);

        // Store values at the time of the login attempt.
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            updateUserProfileRetro(firstName, lastName);

            //mAuthTask = new UserLoginTask(firstName, lastName);
            //mAuthTask.execute((Void) null);
        }
    }

    private void updateUserProfileRetro(String firstName, String lastName) {

        UserService userService = ServiceGenerator.createService(UserService.class, BuildConfig.API_KEY);

        SharedPreferences settings = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int userId =  settings.getInt(HTConstants.USER_ID, -1);

        User user = new User(firstName, lastName);

        Call<User> call = userService.updateUserName(String.valueOf(userId), user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(retrofit2.Response<User> response) {
                Log.v(TAG, "Response from Retrofit");

                Log.d("Response", "User :" + response.body().toString());

                showProgress(false);

                SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(HTConstants.HYPERTRACK_COURIER_ID, response.body().getHypertrackCourierId());
                editor.putBoolean("isUserOnboard", true);
                editor.commit();

                Intent intent = new Intent(Profile.this, Home.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }

            @Override
            public void onFailure(Throwable t) {
                Log.v(TAG, "Inside error block of retrofit. " + t.getMessage());
            }
        });

        //Upload photo
        if (profileImage == null)
            return;

        RequestBody requestBody =
                RequestBody.create(MediaType.parse("image/*"), profileImage);

        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        String fileName = "photo\"; filename=\"" + uuid + ".jpg";
        requestBodyMap.put(fileName, requestBody);

        Call<User> updatePicCall = userService.updateUserProfilePic(String.valueOf(userId), requestBodyMap);
        updatePicCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(retrofit2.Response<User> response) {
                Log.v(TAG, "Pic updated successfully");
                Log.v(TAG, response.headers().toString());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.v(TAG, "Error while updating profile pic. " + t.getMessage());
            }
        });
    }

    private void updateUserProfile(String firstName, String lastName) {

        SharedPreferences settings = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int userId =  settings.getInt(HTConstants.USER_ID, -1);

        String url = HTConstants.API_ENDPOINT + "/api/v1/users/"+userId+"/";

        User user = new User(firstName, lastName);
        Gson gson = new Gson();
        String jsonObjectBody = gson.toJson(user);

        Log.d("Response", "Request Body - " + jsonObjectBody);
        Log.d("Response", "URL - " + url);

        HTCustomPostRequest<User> request = new HTCustomPostRequest<User>(
                7,
                url,
                jsonObjectBody,
                User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        Log.d("Response", "User :" + response.toString());

                        showProgress(false);

                        SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(HTConstants.HYPERTRACK_COURIER_ID, response.getHypertrackCourierId());
                        editor.putBoolean("isUserOnboard", true);
                        editor.commit();

                        Intent intent = new Intent(Profile.this, Home.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgress(false);
                        Log.v(TAG, "Inside on Error");
                    }
                }
        );

        MetaApplication.getInstance().addToRequestQueue(request);

        /*
        if (profileImage == null)
            return;

        byte[] fileData1 = readContentIntoByteArray(profileImage);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            // the first file
            buildPart(dos, fileData1);
            // send multipart form data necesssary after file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // pass to multipart body
            multipartBody = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HTMultipartRequest htMultipartRequest = new HTMultipartRequest(url, mimeType, multipartBody, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                Log.v(TAG, "Successfully Uploaded File !");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error in uploading the file!");
            }
        });

        MetaApplication.getInstance().addToRequestQueue(htMultipartRequest);
        */

    }

    private void buildPart(DataOutputStream dataOutputStream, byte[] fileData) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"photo\";" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    private static byte[] readContentIntoByteArray(File file)
    {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[(int) file.length()];
        try
        {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return bFile;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProfileFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mProfileFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProfileFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProfileFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
                //Handle the image
                profileImage = imageFile;
                Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                mProfileImageButton.setImageBitmap(myBitmap);
                Log.v(TAG, "PhotoName: " + imageFile.getName());
            }
        });
    }

    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean checkPermission(){

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }

    }

    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){

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

                    Toast.makeText(this,"Permission Denied, You cannot access storage.",Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

}

