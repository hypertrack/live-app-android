package io.hypertrack.sendeta.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import io.hypertrack.sendeta.store.UserStore;
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

    private File profileImage;

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

        UserStore.sharedStore.updateInfo(mFirstNameView.getText().toString(), mLastNameView.getText().toString(), new SuccessErrorCallback() {
            @Override
            public void OnSuccess() {

                if (profileImage != null) {
                    UserStore.sharedStore.updatePhoto(profileImage, new SuccessErrorCallback() {
                        @Override
                        public void OnSuccess() {
                            mProgressDialog.dismiss();
                            broadcastResultIntent();
                            finish();
                        }

                        @Override
                        public void OnError() {
                            mProgressDialog.dismiss();
                            showUpdateError();
                        }
                    });
                } else {
                    mProgressDialog.dismiss();
                    broadcastResultIntent();
                    finish();
                }
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
                showUpdateError();
            }
        });
    }

    private void showUpdateError() {
        Toast.makeText(this, R.string.edit_profile_error, Toast.LENGTH_LONG).show();
    }

    private void broadcastResultIntent() {
        Intent intent = new Intent();
        setResult(EDIT_PROFILE_RESULT_CODE, intent);
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

                profileImage = ImageUtils.getScaledFile(ImageUtils.getScaledFile(imageFile));

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
