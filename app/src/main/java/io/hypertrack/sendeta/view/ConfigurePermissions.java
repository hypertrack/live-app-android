package io.hypertrack.sendeta.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;

import com.hypertrack.lib.HyperTrack;

import io.hypertrack.sendeta.R;

/**
 * Created by Aman Jain on 09/03/17.
 */

public class ConfigurePermissions extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_permission);

        initUI();
    }

    public void initUI() {
        Button letsGO = (Button) findViewById(R.id.lets_go);
        letsGO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForLocationSettings();
            }
        });
    }

    private void checkForLocationSettings() {
        // Check for Location permission
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this);
            return;
        }

        // Check for Location settings
        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this, null);
            return;
        }

        // Location Permissions and Settings have been enabled
        // Proceed with your app logic here
        proceedToProfileScreen();
    }

    private void proceedToProfileScreen() {
        startActivity(new Intent(ConfigurePermissions.this, Profile.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Handle Location permission successfully granted response
                checkForLocationSettings();

            } else {
                // Handle Location permission request denied error
                showSnackBar();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Handle Location services successfully enabled response
                checkForLocationSettings();

            } else {
                // Handle Location services request denied error
                showSnackBar();
            }
        }
    }

    private void showSnackBar() {
        if (!HyperTrack.checkLocationPermission(this)) {
            // Handle Location permission request denied error
            Snackbar.make(findViewById(R.id.parent_layout), R.string.location_permission_snackbar_msg,
                    Snackbar.LENGTH_INDEFINITE).setAction("Allow Permission", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkForLocationSettings();
                }
            }).show();

        } else if (HyperTrack.checkLocationServices(this)) {
            // Handle Location services request denied error
            Snackbar.make(findViewById(R.id.parent_layout), R.string.location_services_snackbar_msg,
                    Snackbar.LENGTH_INDEFINITE).setAction("Enable Location", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkForLocationSettings();
                }
            }).show();
        }
    }
}
