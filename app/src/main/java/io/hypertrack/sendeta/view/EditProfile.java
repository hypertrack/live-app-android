package io.hypertrack.sendeta.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Country;
import io.hypertrack.sendeta.model.CountryMaster;
import io.hypertrack.sendeta.model.CountrySpinnerAdapter;
import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ImageUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.PhoneUtils;
import io.hypertrack.sendeta.util.images.DefaultCallback;
import io.hypertrack.sendeta.util.images.EasyImage;
import io.hypertrack.sendeta.util.images.RoundedImageView;

public class EditProfile extends BaseActivity {

    public static final String TAG = "EditProfile";

    private ProgressDialog mProgressDialog;
    private RoundedImageView mProfileImageView;
    private AutoCompleteTextView mNameView, mLastNameView;

    private Bitmap oldProfileImage = null, updatedProfileImage = null;
    private EasyImage.ImageSource imageUploadSource;
    private File updatedProfileImageFile;

    private EditText phoneNumberView;
    private TextView countryCodeTextView;
    private Spinner countryCodeSpinner;
    private LinearLayout countryCodeLayout;

    private String isoCode;

    private CountrySpinnerAdapter adapter;


    private TextView.OnEditorActionListener mFirstNameEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return actionId == EditorInfo.IME_ACTION_DONE;

        }
    };

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateUserInfo();
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
        mNameView = (AutoCompleteTextView) findViewById(R.id.profile_name);

        mProfileImageView = (RoundedImageView) findViewById(R.id.profile_image_view);

        // Initialize UI Action Listeners
        mNameView.setOnEditorActionListener(mFirstNameEditorActionListener);
        phoneNumberView = (EditText) findViewById(R.id.register_phone_number);
        countryCodeTextView = (TextView) findViewById(R.id.register_country_code);
        countryCodeSpinner = (Spinner) findViewById(R.id.register_country_codes_spinner);
        countryCodeLayout = (LinearLayout) findViewById(R.id.register_country_code_layout);
        countryCodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryCodeSpinner.performClick();
            }
        });
        phoneNumberView.setOnEditorActionListener(mEditorActionListener);
        // mLastNameView.setOnEditorActionListener(mLastNameEditorActionListener);

        OnboardingUser user = OnboardingManager.sharedManager().getUser();
        if (user != null) {
            oldProfileImage = user.getImageBitmap();
        }


        // Setup UI Views with OnboardingUser Data
        updateFirstName();
        //updateLastName();
        updateImage();
        initCountryFlagSpinner();
    }

    private void initCountryFlagSpinner() {
        CountryMaster cm = CountryMaster.getInstance(this);
        final ArrayList<Country> countries = cm.getCountries();

        adapter = new CountrySpinnerAdapter(this, R.layout.view_country_list_item, countries);
        countryCodeSpinner.setAdapter(adapter);

        String isoCountryCode = PhoneUtils.getCountryRegionFromPhone(this);
        Log.v(TAG, "Region ISO: " + isoCountryCode);

        if (!TextUtils.isEmpty(isoCountryCode)) {
            for (Country c : countries) {
                if (c.mCountryIso.equalsIgnoreCase(isoCountryCode)) {
                    countryCodeSpinner.setSelection(adapter.getPosition(c));
                }
            }
        }

        countryCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isoCode = countries.get(position).mCountryIso;
                countryCodeTextView.setText("+ " + countries.get(position).mDialPrefix);
                countryCodeTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }



    private void updateFirstName() {
        OnboardingUser user = OnboardingManager.sharedManager().getUser();
        if (user == null) {
            return;
        }

        String firstName = user.getName();
        if (firstName == null) {
            return;
        }

        mNameView.setText(firstName);
    }

    private void updateLastName() {
        OnboardingUser user = OnboardingManager.sharedManager().getUser();
        if (user == null) {
            return;
        }

       /* String lastName = user.getLastName();
        if (lastName == null) {
            return;
        }

       // mLastNameView.setText(lastName);*/
    }

    private void updateImage() {
        OnboardingUser user = OnboardingManager.sharedManager().getUser();
        if (user != null) {
            Bitmap bitmap = user.getImageBitmap();
            if (bitmap != null) {
                mProfileImageView.setImageBitmap(bitmap);
                mProfileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    public void onProfileImageViewClicked(View view) {
        // Create Image Chooser Intent if READ_EXTERNAL_STORAGE permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            EasyImage.openChooser(EditProfile.this, "Please select", true);

        } else {
            // Show Rationale & Request for READ_EXTERNAL_STORAGE permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                PermissionUtils.showRationaleMessageAsDialog(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                        getString(R.string.read_external_storage_permission_msg));
            } else {
                PermissionUtils.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    public void doneButtonClicked(MenuItem menuItem) {
        if (mNameView.getText().length() == 0) {
            mNameView.setError("First Name cannot be blank");
            return;
        }

      /*  if (mLastNameView.getText().length() == 0) {
            mLastNameView.setError("Last Name cannot be blank");
            return;
        }*/

        updateUserInfo();
    }

    private void updateUserInfo() {
        if (!this.isFinishing()) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Updating profile");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        String firstName = mNameView.getText().toString();
        if (!TextUtils.isEmpty(firstName)) {
            firstName = firstName.trim();
        }
        OnboardingManager onboardingManager = OnboardingManager.sharedManager();
        OnboardingUser user = onboardingManager.getUser();
        user.setName(firstName);
        if (!TextUtils.isEmpty(phoneNumberView.getText().toString()))
            user.setPhone(phoneNumberView.getText().toString());
        OnboardingUser.setOnboardingUser();
      /*  String lastName = mLastNameView.getText().toString();
        if (!TextUtils.isEmpty(lastName)) {
            lastName = lastName.trim();
        }*/

       /*OnboardingManager.sharedManager().updateInfo(firstName, lastName, new SuccessErrorCallback() {
            @Override
            public void OnSuccess() {

                // Check if the profile image has changed from the existing one
                if (updatedProfileImageFile != null && updatedProfileImageFile.length() > 0 && !updatedProfileImage.sameAs(oldProfileImage)) {

                   OnboardingManager.sharedManager().updatePhoto(updatedProfileImageFile, new SuccessErrorCallback() {

                        @Override
                        public void OnSuccess() {
                            // Process Image Upload Success for Analytics
                            processImageDataForAnalytics(true, null, oldProfileImage, imageUploadSource);

                            if (!EditProfile.this.isFinishing() && mProgressDialog != null)
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

                            if (!EditProfile.this.isFinishing() && mProgressDialog != null)
                                mProgressDialog.dismiss();
                            
                            Toast.makeText(EditProfile.this, ErrorMessages.PROFILE_PIC_UPLOAD_FAILED,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {

                    Toast.makeText(EditProfile.this, "The image you are trying to save is same as the existing one.",
                            Toast.LENGTH_SHORT).show();
                    if (!EditProfile.this.isFinishing() && mProgressDialog != null)
                        mProgressDialog.dismiss();
                }
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
                Toast.makeText(EditProfile.this, R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private void broadcastResultIntent() {
        Intent intent = new Intent();
        setResult(Constants.EDIT_PROFILE_REQUEST_CODE, intent);
    }

    /**
     * Method to process uploaded OnboardingUser Profile Image to log Analytics Event
     *
     * @param status          Flag to indicate status of FavoritePlace Deletion event
     * @param errorMessage    ErrorMessage in case of Failure
     * @param oldProfileImage OnboardingUser's Existing Profile Image
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onProfileImageViewClicked(null);
                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                            getString(R.string.read_external_storage_permission_never_allow));
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
