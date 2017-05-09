package io.hypertrack.sendeta.view;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hypertrack.lib.internal.common.logging.HTLog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Country;
import io.hypertrack.sendeta.model.CountryMaster;
import io.hypertrack.sendeta.model.CountrySpinnerAdapter;
import io.hypertrack.sendeta.presenter.IProfilePresenter;
import io.hypertrack.sendeta.presenter.ProfilePresenter;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.ImageUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.Utils;
import io.hypertrack.sendeta.util.images.DefaultCallback;
import io.hypertrack.sendeta.util.images.EasyImage;
import io.hypertrack.sendeta.util.images.RoundedImageView;

public class Profile extends BaseActivity implements ProfileView {

    private final static String TAG = Profile.class.getSimpleName();

    public EditText mNameView;
    public RoundedImageView mProfileImageView;
    public ProgressBar mProfileImageLoader;
    public Bitmap oldProfileImage = null, updatedProfileImage = null;
    private ProgressDialog mProgressDialog;
    private EditText phoneNumberView;
    private TextView countryCodeTextView;
    private Spinner countryCodeSpinner;
    private Button register;
    private String isoCode;
    private Target profileImageDownloadTarget;
    private File profileImage;
    private IProfilePresenter<ProfileView> presenter = new ProfilePresenter();
    private boolean showSkip = true;

    private TextView.OnEditorActionListener mNameEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                phoneNumberView.requestFocus();
                return true;
            }

            return false;
        }
    };

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSignInButtonClicked();
                return true;
            }

            return false;
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(mNameView.getText().toString()) && TextUtils.isEmpty(phoneNumberView.getText().toString())) {
                showSkip = true;
                supportInvalidateOptionsMenu();
            } else if (showSkip && !TextUtils.isEmpty(s.toString())) {
                showSkip = false;
                supportInvalidateOptionsMenu();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Toolbar
        initToolbar(getString(R.string.title_activity_signup_profile), false);

        // Attach View Presenter to View
        presenter.attachView(this);

        initUIViews();
    }

    private void initUIViews() {
        // Initialize UI Views before Attaching View Presenter
        mNameView = (EditText) findViewById(R.id.profile_name);
        mProfileImageView = (RoundedImageView) findViewById(R.id.profile_image_view);
        mProfileImageLoader = (ProgressBar) findViewById(R.id.profile_image_loader);
        register = (Button) findViewById(R.id.profile_register);
        phoneNumberView = (EditText) findViewById(R.id.register_phone_number);
        countryCodeTextView = (TextView) findViewById(R.id.register_country_code);
        countryCodeSpinner = (Spinner) findViewById(R.id.register_country_codes_spinner);
        LinearLayout countryCodeLayout = (LinearLayout) findViewById(R.id.register_country_code_layout);
        countryCodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryCodeSpinner.performClick();
            }
        });

        // Initialize UI Action Listeners
        mNameView.setOnEditorActionListener(mNameEditorActionListener);
        phoneNumberView.setOnEditorActionListener(mEditorActionListener);
        mNameView.addTextChangedListener(mTextWatcher);
        phoneNumberView.addTextChangedListener(mTextWatcher);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInButtonClicked();
            }
        });
        initCountryFlagSpinner();
    }

    private void initCountryFlagSpinner() {
        CountryMaster cm = CountryMaster.getInstance(this);
        final ArrayList<Country> countries = cm.getCountries();

        CountrySpinnerAdapter adapter = new CountrySpinnerAdapter(this, R.layout.view_country_list_item, countries);
        countryCodeSpinner.setAdapter(adapter);

        String isoCountryCode = Utils.getCountryRegionFromPhone(this);
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

    public void onProfileImageViewClicked(View view) {
        // Create Image Chooser Intent if READ_EXTERNAL_STORAGE permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            EasyImage.openChooser(Profile.this, "Please select", true);

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

    public void onNextButtonClicked(MenuItem menuItem) {
        this.onSignInButtonClicked();
    }

    private void onSignInButtonClicked() {
        showProgress(true);
        String name = mNameView.getText().toString();
        String number = phoneNumberView.getText().toString();
        Utils.hideKeyboard(Profile.this, register);
        presenter.attemptLogin(name, number, isoCode, Utils.getDeviceId(this), profileImage, oldProfileImage, updatedProfileImage);
    }

    @Override
    public void updateViews(String name, String phone, String ISOCode, String profileURL) {
        String nameFromAccount = getName();
        if (name != null) {
            mNameView.setText(nameFromAccount);
            showSkip = false;
            supportInvalidateOptionsMenu();

        }
        if (!TextUtils.isEmpty(name)) {
            mNameView.setText(name);
        }

        if (profileURL != null && !profileURL.isEmpty()) {
            profileImageDownloadTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d("Profile", "onBitmapLoaded called!");

                    oldProfileImage = bitmap;

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
                    .into(profileImageDownloadTarget);
            mProfileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private String getName() {
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Account[] list = manager.getAccounts();

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                return account.name;
            }
        }
        return null;
    }

    @Override
    public void showProfilePicUploadSuccess() {
        Toast.makeText(Profile.this, R.string.profile_upload_success, Toast.LENGTH_SHORT).show();
        if (updatedProfileImage != null) {
            setToolbarIcon(updatedProfileImage);
        }
    }

    @Override
    public void showProfilePicUploadError() {
        Toast.makeText(Profile.this, ErrorMessages.PROFILE_PIC_UPLOAD_FAILED, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToHomeScreen() {
        showProgress(false);

        // Clear Existing running trip on Registration Successful
        SharedPreferenceManager.deleteAction();
        SharedPreferenceManager.deletePlace();
        HTLog.i(TAG, "User Registration successful: Clearing Active Trip, if any");

        TaskStackBuilder.create(Profile.this)
                .addNextIntentWithParentStack(new Intent(Profile.this, Home.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .startActivities();
        finish();
    }


    @Override
    public void showErrorMessage() {
        showProgress(false);
        Toast.makeText(Profile.this, R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(Profile.this, R.string.profile_pic_choose_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source) {
                if (imageFile == null) {
                    return;
                }

                // Cancel Profile Pic Download Request from Server & Hide Image Download Loader
                Picasso.with(Profile.this).cancelRequest(profileImageDownloadTarget);
                mProfileImageLoader.setVisibility(View.GONE);
                profileImage = ImageUtils.getScaledFile(imageFile);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Handle Read external storage permission successfully granted response
                onProfileImageViewClicked(null);

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Handle Read external storage permission request denied error
                PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.READ_EXTERNAL_STORAGE,
                        getString(R.string.read_external_storage_permission_never_allow));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return showSkip && super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        // Detach View Presenter from View
        presenter.detachView();
        super.onDestroy();
    }
}