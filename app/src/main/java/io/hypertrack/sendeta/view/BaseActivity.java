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

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hypertrack.lib.internal.common.util.HTTextUtils;

import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.util.images.RoundedImageView;

/**
 * Created by piyush on 30/06/16.
 */
public class BaseActivity extends AppCompatActivity {

    public LinearLayout retryContainer;

    //to be only accessed through methods, do not change access modifier
    private Toolbar toolbar;
    private TextView loadingLayoutMessage;
    private FrameLayout loadingLayout;
    private CardView loadingLayoutMessageContainer;
    private TextView textView;
    private RoundedImageView toolbarIcon;

    public Toolbar getToolbar() {
        return toolbar;
    }

    // Call this on very start of every activity

    /**
     * Default toolbar with no title and app's primary color as background
     */
    public void initToolbar() {
        setupToolbar(null, null, false);
    }

    /**
     * Default toolbar with no title, app's primary color as background and param to Enable/Disable
     * Home Button
     */
    public void initToolbar(boolean homeButtonEnabled) {
        setupToolbar(null, null, homeButtonEnabled);
    }

    /**
     * Default toolbar with title and app's primary color as background
     *
     * @param title
     */
    public void initToolbar(String title) {
        setupToolbar(title, null, true);
    }

    /**
     * Default toolbar with title, app's primary color as background and param to Enable/Disable
     * Home Button
     *
     * @param title
     */
    public void initToolbar(String title, boolean homeButtonEnabled) {
        setupToolbar(title, null, homeButtonEnabled);
    }


    private void setupToolbar(String title, String subTitle, boolean homeButtonEnabled) {

        toolbar = (Toolbar) findViewById(R.id.toolbar_layout);
        textView = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        toolbarIcon = (RoundedImageView) toolbar.findViewById(R.id.toolbar_icon);

        if (!HTTextUtils.isEmpty(title)) {
            textView.setText(title);
        }

        if (!HTTextUtils.isEmpty(subTitle)) {
            textView.setText(subTitle);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(homeButtonEnabled);
        getSupportActionBar().setHomeButtonEnabled(homeButtonEnabled);
    }

    public void setToolbarIcon(Bitmap profileImage) {
        if (toolbarIcon != null && profileImage != null) {
            toolbarIcon.setImageBitmap(profileImage);
            toolbarIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }


    /**
     * [IMPORTANT] use this method only after initToolbar
     * Required if in between title alterations are required
     *
     * @param title
     */
    public void setTitle(String title) {
        if (toolbar != null) {
            if (textView != null)
                textView.setText(title);
            else {
                textView = (TextView) toolbar.findViewById(R.id.toolbar_title);
                textView.setText(title);
            }

        }
    }

    /**
     * [IMPORTANT] use this method only after initToolbar
     * Required if in between subTitle alterations are required
     *
     * @param subTitle
     */
    public void setSubTitle(String subTitle) {
        if (toolbar != null) {
            toolbar.setSubtitle(subTitle);
        }
    }

    /**
     * In case toolbar is not required, then use this directly without init call
     */
    public void hideToolbar() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar_layout);
        }
        toolbar.setVisibility(View.GONE);
    }

    /**
     * used to display a loader within content section
     * [IMPORTANT] include "layout_loading" inside main data container of the view
     *
     * @param isEnabled true to enable/show loader, false to disable/hide
     */
    public void displayLoader(boolean isEnabled) {
        displayLoader(isEnabled, null);
    }

    /**
     * used to display a loader within content section
     * [IMPORTANT] include "layout_loading" inside main data container of the view
     *
     * @param isEnabled true to enable/show loader, false to disable/hide
     * @param message   any message if applicable, will be shown above loader icon
     */
    public void displayLoader(boolean isEnabled, String message) {

        //check if view are inflated or not
        if (loadingLayout == null && loadingLayoutMessage == null) {
            loadingLayout = (FrameLayout) findViewById(R.id.loading_container);
            loadingLayoutMessage = (TextView) findViewById(R.id.loading_message);
            loadingLayoutMessageContainer = (CardView) findViewById(R.id.loading_message_container);
        }

        //check if layouts available or not
        if (loadingLayout != null && loadingLayoutMessage != null) {
            loadingLayoutMessageContainer.setVisibility(View.GONE);

            if (isEnabled) {

                //if any message to be shown
                if (message != null && message.trim().length() > 0) {
                    loadingLayoutMessage.setText(message);
                    loadingLayoutMessageContainer.setVisibility(View.VISIBLE);
                }
                loadingLayout.setVisibility(View.VISIBLE);
            } else {
                loadingLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Hide retry layout, default is hidden
     *
     * @return true or false depending on that view was successfully inflated or not
     */
    public boolean hideRetryLayout() {

        if (retryContainer != null) {
            retryContainer.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (HTTextUtils.isEmpty(BuildConfig.HYPERTRACK_PK)
                || BuildConfig.HYPERTRACK_PK.equalsIgnoreCase("YOUR_HYPERTRACK_PUBLISHABLE_KEY")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("HyperTrack Publishable Key is not configured!")
                    .setMessage("Add HyperTrack Publishable Key to keys.properties file")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //back button inside toolbar
        if (item.getItemId() == android.R.id.home) {
            //onBackPressed();
            return true;
        } else
            return false;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
