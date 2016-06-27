package io.hypertrack.meta.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.R;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.UserStore;
import io.hypertrack.meta.util.SuccessErrorCallback;
import io.hypertrack.meta.util.images.DefaultCallback;
import io.hypertrack.meta.util.images.EasyImage;

public class EditProfile extends AppCompatActivity {

    public static final int EDIT_PROFILE_RESULT_CODE = 101;

    @Bind(R.id.firstName)
    public AutoCompleteTextView mFirstNameView;

    @Bind(R.id.lastName)
    public AutoCompleteTextView mLastNameView;

    @Bind(R.id.profileImageView)
    public ImageButton mProfileImageButton;

    private File profileImage;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        this.setupViews();
    }

    private void setupViews() {
        this.updateFirstName();
        this.updateLastName();
        this.updateImage();
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

        this.mFirstNameView.setText(firstName);
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

        this.mLastNameView.setText(lastName);
    }

    private void updateImage() {
        User user = UserStore.sharedStore.getUser();
        if (user != null) {
            Bitmap bitmap = user.getImageBitmap();
            if (bitmap != null) {
                mProfileImageButton.setImageBitmap(bitmap);
            }
        }
    }

    public void doneButtonClicked(MenuItem menuItem) {
        if (this.mFirstNameView.getText().length() == 0) {
            this.mFirstNameView.setError("First Name cannot be blank");
            return;
        }

        if (this.mLastNameView.getText().length() == 0) {
            this.mLastNameView.setError("Last Name cannot be blank");
            return;
        }

        this.updateUserInfo();
    }

    private void updateUserInfo() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Updating profile");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        UserStore.sharedStore.updateInfo(this.mFirstNameView.getText().toString(), this.mLastNameView.getText().toString(), new SuccessErrorCallback() {
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
        Intent intent=new Intent();
        setResult(EDIT_PROFILE_RESULT_CODE, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @OnClick(R.id.profileImageView)
    public void onImageButtonClicked() {
        EasyImage.openChooser(EditProfile.this, "Please select", true);
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

                profileImage = getScaledFile(imageFile);
                mProfileImageButton.setImageBitmap(getRotatedBitMap(imageFile));
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
}
