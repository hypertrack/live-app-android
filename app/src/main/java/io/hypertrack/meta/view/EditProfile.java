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

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.R;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.UserStore;
import io.hypertrack.meta.util.images.DefaultCallback;
import io.hypertrack.meta.util.images.EasyImage;
import io.realm.annotations.PrimaryKey;

public class EditProfile extends AppCompatActivity {

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

        this.mFirstNameView.setText(lastName);
    }

    private void updateImage() {
//        this.mProfileImageButton.setImageDrawable(R.drawable.default_profile_pic);
    }

    public void doneButtonClicked(MenuItem menuItem) {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
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
}
