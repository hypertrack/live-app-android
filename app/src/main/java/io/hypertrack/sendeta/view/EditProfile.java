package io.hypertrack.sendeta.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.ImageUtils;
import io.hypertrack.sendeta.util.SuccessErrorCallback;
import io.hypertrack.sendeta.util.images.DefaultCallback;
import io.hypertrack.sendeta.util.images.EasyImage;
import io.hypertrack.sendeta.util.images.RoundedImageView;

public class EditProfile extends BaseActivity {

    public static final String TAG = "EditProfile";
    public static final int EDIT_PROFILE_RESULT_CODE = 101;

    private ProgressDialog mProgressDialog;
    private RoundedImageView mProfileImageView;
    private AutoCompleteTextView mFirstNameView, mLastNameView;

    private Bitmap oldProfileImage = null, updatedProfileImage = null;
    private EasyImage.ImageSource imageUploadSource;
    private File updatedProfileImageFile;

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
                EditProfile.this.doneButtonClicked(null);
                return true;
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Toolbar
        initToolbar("Profile");

        // Initialize UI Views
        mFirstNameView = (AutoCompleteTextView) findViewById(R.id.profile_first_name);
        mLastNameView = (AutoCompleteTextView) findViewById(R.id.profile_last_name);
        mProfileImageView = (RoundedImageView) findViewById(R.id.profile_image_view);

        // Initialize UI Action Listeners
        mFirstNameView.setOnEditorActionListener(mFirstNameEditorActionListener);
        mLastNameView.setOnEditorActionListener(mLastNameEditorActionListener);

        User user = UserStore.sharedStore.getUser();
        if (user != null) {
            oldProfileImage = user.getImageBitmap();
        }

        // Setup UI Views with User Data
        updateFirstName();
        updateLastName();
        updateImage();
    }

    private void updateFirstName() {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        String firstName = user.getFirstName();
        if (firstName == null) {
            return;
        }

        mFirstNameView.setText(firstName);
    }

    private void updateLastName() {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        String lastName = user.getLastName();
        if (lastName == null) {
            return;
        }

        mLastNameView.setText(lastName);
    }

    private void updateImage() {
        User user = UserStore.sharedStore.getUser();
        if (user != null) {
            Bitmap bitmap = user.getImageBitmap();
            if (bitmap != null) {
                mProfileImageView.setImageBitmap(bitmap);
                mProfileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    public void onProfileImageViewClicked(View view) {
        EasyImage.openChooser(EditProfile.this, "Please select", true);
    }

    public void doneButtonClicked(MenuItem menuItem) {
        if (mFirstNameView.getText().length() == 0) {
            mFirstNameView.setError("First Name cannot be blank");
            return;
        }

        if (mLastNameView.getText().length() == 0) {
            mLastNameView.setError("Last Name cannot be blank");
            return;
        }

        updateUserInfo();
    }

    private void updateUserInfo() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Updating profile");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String firstName = mFirstNameView.getText().toString();
        if (!TextUtils.isEmpty(firstName)) {
            firstName = firstName.trim();
        }

        String lastName = mLastNameView.getText().toString();
        if (!TextUtils.isEmpty(lastName)) {
            lastName = lastName.trim();
        }

        UserStore.sharedStore.updateInfo(firstName, lastName, new SuccessErrorCallback() {
            @Override
            public void OnSuccess() {

                // Check if the profile image has changed from the existing one
                if (updatedProfileImageFile != null && updatedProfileImageFile.length() > 0 && !updatedProfileImage.sameAs(oldProfileImage)) {

                    UserStore.sharedStore.updatePhoto(updatedProfileImageFile, new SuccessErrorCallback() {

                        @Override
                        public void OnSuccess() {
                            // Process Image Upload Success for Analytics
                            processImageDataForAnalytics(true, null, oldProfileImage, imageUploadSource);

                            mProgressDialog.dismiss();
                            Toast.makeText(EditProfile.this, "Profile Pic uploaded successfully",
                                    Toast.LENGTH_SHORT).show();

                            broadcastResultIntent();
                            finish();
                        }

                        @Override
                        public void OnError() {
                            // Process Image Upload Failure for Analytics
                            processImageDataForAnalytics(false, ErrorMessages.PROFILE_PIC_UPLOAD_FAILED,
                                    oldProfileImage, imageUploadSource);

                            mProgressDialog.dismiss();
                            Toast.makeText(EditProfile.this, ErrorMessages.PROFILE_PIC_UPLOAD_FAILED,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {

                    Toast.makeText(EditProfile.this, "The image you are trying to save is same as the existing one.",
                            Toast.LENGTH_SHORT).show();
                    mProgressDialog.dismiss();
//                    broadcastResultIntent();
//                    finish();
                }
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
                Toast.makeText(EditProfile.this, R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void broadcastResultIntent() {
        Intent intent = new Intent();
        setResult(EDIT_PROFILE_RESULT_CODE, intent);
    }

    /**
     * Method to process uploaded User Profile Image to log Analytics Event
     *
     * @param status            Flag to indicate status of FavoritePlace Deletion event
     * @param errorMessage      ErrorMessage in case of Failure
     * @param oldProfileImage   User's Existing Profile Image
     */
    private void processImageDataForAnalytics(boolean status, String errorMessage, Bitmap oldProfileImage, EasyImage.ImageSource imageSource) {

        String source;

        switch (imageSource) {
            case GALLERY:
                source = "gallery";
                break;
            case DOCUMENTS:
                source = "documents";
                break;
            case CAMERA:
                source = "camera";
                break;
            default:
                source = "";
                break;
        }

        if (oldProfileImage != null && oldProfileImage.getByteCount() > 0) {
            AnalyticsStore.getLogger().replacedPhotoViaPhotoEditor(status, errorMessage, source);
        } else {
            AnalyticsStore.getLogger().uploadedPhotoViaPhotoEditor(status, errorMessage, source);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source) {
                //Some error handling
                Toast.makeText(EditProfile.this, R.string.profile_pic_choose_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source) {
                if (imageFile == null) {
                    return;
                }

                updatedProfileImageFile = ImageUtils.getScaledFile(imageFile);
                imageUploadSource = source;

                updatedProfileImage = ImageUtils.getRotatedBitMap(imageFile);
                if (updatedProfileImage == null) {
                    updatedProfileImage = BitmapFactory.decodeFile(imageFile.getPath());
                }

                if (updatedProfileImage != null) {
                    mProfileImageView.setImageBitmap(updatedProfileImage);
                }
                mProfileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
