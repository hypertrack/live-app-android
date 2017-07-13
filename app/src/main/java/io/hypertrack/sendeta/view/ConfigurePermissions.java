
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;

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

        final ImageView locationRipple = (ImageView) findViewById(R.id.location_ripple);

        final ScaleAnimation growAnim = new ScaleAnimation(0.9f, 1.05f, 0.9f, 1.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        final ScaleAnimation shrinkAnim = new ScaleAnimation(1.05f, 0.9f, 1.05f, 0.9f,  Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        growAnim.setDuration(800);
        shrinkAnim.setDuration(800);

        locationRipple.setAnimation(growAnim);
        growAnim.start();

        growAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){}

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                locationRipple.setAnimation(shrinkAnim);
                shrinkAnim.start();
            }
        });
        shrinkAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){}

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                locationRipple.setAnimation(growAnim);
                growAnim.start();
            }
        });
    }

    private void checkForLocationSettings() {
        // Check for Location permission
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this,null);
            return;
        }

        // Check for Location settings
        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
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
                    Snackbar.LENGTH_INDEFINITE).setAction("Allow", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkForLocationSettings();
                }
            }).show();

        } else if (HyperTrack.checkLocationServices(this)) {
            // Handle Location services request denied error
            Snackbar.make(findViewById(R.id.parent_layout), R.string.location_services_snackbar_msg,
                    Snackbar.LENGTH_INDEFINITE).setAction("Enable", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkForLocationSettings();
                }
            }).show();
        }
    }
}
