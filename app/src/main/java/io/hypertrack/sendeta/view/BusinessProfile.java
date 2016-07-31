package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.presenter.BusinessProfilePresenter;
import io.hypertrack.sendeta.presenter.IBusinessProfilePresenter;
import io.hypertrack.sendeta.util.Constants;

/**
 * Created by piyush on 22/07/16.
 */
public class BusinessProfile extends BaseActivity implements BusinessProfileView {

    public static final String TAG = BusinessProfile.class.getSimpleName();
    public static final String KEY_MEMBERSHIP = "membership";
    public static final String KEY_MEMBERSHIP_INVITE = "membership_invite";
    public static final String KEY_MEMBERSHIP_ACCOUNT_ID = "membership_id";

    private TextView businessProfileTitle;
    private TextView businessProfileMessage;
    private TextView businessProfileInviteTnC;
    private TextView businessProfilePrimaryBtn;
    private TextView businessProfileSecondaryBtn;
    private TextView businessProfileLoaderText;
    private LinearLayout businessProfileLoaderLayout;
    private LinearLayout businessProfileBtnLoader;

    private IBusinessProfilePresenter<BusinessProfileView> presenter = new BusinessProfilePresenter();
    private boolean businessProfilePrimaryBtnClicked = false, businessProfileSecondaryBtnClicked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile);

        // Initialize Toolbar
        initToolbar(getString(R.string.title_activity_business_profile));

        // Initialize UI
        businessProfileTitle = (TextView) findViewById(R.id.business_profile_title);
        businessProfileMessage = (TextView) findViewById(R.id.business_profile_message);
        businessProfileInviteTnC = (TextView) findViewById(R.id.business_profile_invite_tnc);
        businessProfilePrimaryBtn = (TextView) findViewById(R.id.business_profile_primary_btn);
        businessProfileSecondaryBtn = (TextView) findViewById(R.id.business_profile_secondary_btn);

        businessProfileLoaderText = (TextView) findViewById(R.id.business_profile_loader_text);
        businessProfileLoaderLayout = (LinearLayout) findViewById(R.id.business_profile_loader_layout);

        businessProfileBtnLoader = (LinearLayout) findViewById(R.id.business_profile_btn_loader);

        // Attach View Presenter to View
        presenter.attachView(this);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(KEY_MEMBERSHIP_INVITE) && intent.getBooleanExtra(KEY_MEMBERSHIP_INVITE, false)) {
                businessProfileLoaderLayout.setVisibility(View.VISIBLE);

                int membershipAccountId = 0;
                if (intent.hasExtra(KEY_MEMBERSHIP_ACCOUNT_ID)) {
                    membershipAccountId = intent.getIntExtra(KEY_MEMBERSHIP_ACCOUNT_ID, 0);
                }

                // Get Membership from Network Call
                presenter.getMembershipsForUser(membershipAccountId);

            } else if (intent.hasExtra(KEY_MEMBERSHIP)) {
                Membership membership = (Membership) intent.getSerializableExtra(KEY_MEMBERSHIP);
                if (membership == null)
                    updateViewsToSetUpBusinessProfile();
                else
                    updateViewsForMembershipInvite(membership);
            } else {
                updateViewsToSetUpBusinessProfile();
            }
        }
    }

    @Override
    public void handleGetMembershipSuccess(Membership membership) {
        businessProfileLoaderLayout.setVisibility(View.GONE);

        if (membership != null) {
            updateViewsForMembershipInvite(membership);
        } else {
            updateViewsForNoMembershipInvite();
        }
    }

    @Override
    public void handleGetMembershipError() {
        businessProfileLoaderLayout.setVisibility(View.GONE);

        updateViewsForNoMembershipInvite();
    }

    @Override
    public void showMembershipActionSuccess(boolean acceptInvite, String accountName) {

        // Hide the Loaders on screen if visible
        businessProfileLoaderLayout.setVisibility(View.GONE);
        businessProfileBtnLoader.setVisibility(View.GONE);

        if (acceptInvite) {
            Toast.makeText(BusinessProfile.this, getString(R.string.business_profile_accepted_success_msg, accountName),
                    Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(BusinessProfile.this, getString(R.string.business_profile_rejected_success_msg, accountName),
                    Toast.LENGTH_SHORT).show();
        }

        returnToSettingsScreen(true);
    }

    private void returnToSettingsScreen(boolean status) {
        Intent returnIntent = new Intent();

        if (status) {
            returnIntent.putExtra("success", true);
        }
        setResult(RESULT_OK, returnIntent);

        finish();
    }

    @Override
    public void showMembershipActionError(boolean acceptInvite, String accountName) {

        // Hide the Loaders on screen if visible
        businessProfileLoaderLayout.setVisibility(View.GONE);
        businessProfileBtnLoader.setVisibility(View.GONE);

        if (acceptInvite) {
            Toast.makeText(BusinessProfile.this, R.string.business_profile_accepted_error_msg,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BusinessProfile.this, R.string.business_profile_rejected_error_msg,
                    Toast.LENGTH_SHORT).show();
        }

        returnToSettingsScreen(false);
    }

    public void updateViewsForMembershipInvite(final Membership membership) {
        String accountName = membership.getAccountName();
        // Check if the pending invite was for a valid company name
        if (!TextUtils.isEmpty(accountName)) {
            businessProfileTitle.setText(accountName);
            businessProfileMessage.setText(R.string.business_profile_invite_msg);

            // Show Business Profile Pending Invite TnC View with companyName as a parameter
            businessProfileInviteTnC.setText(getString(R.string.business_profile_invite_tnc, accountName));
            businessProfileInviteTnC.setVisibility(View.VISIBLE);

            businessProfilePrimaryBtn.setText(R.string.business_profile_invite_primary_btn);
            businessProfilePrimaryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Handle Business Profile "Accept" CTA here

                    // Show Loader on Buttons
                    businessProfileBtnLoader.setVisibility(View.VISIBLE);

                    presenter.attemptAcceptPendingBusinessProfile(membership);
                }
            });

            businessProfileSecondaryBtn.setText(R.string.business_profile_invite_secondary_btn);
            businessProfileSecondaryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Handle Business Profile "Reject" CTA here

                    // Show Loader on Buttons
                    businessProfileBtnLoader.setVisibility(View.VISIBLE);

                    presenter.attemptRejectPendingBusinessProfile(membership);
                }
            });
        } else {
            // Fallback to handle an empty company name
            updateViewsToSetUpBusinessProfile();
        }
    }

    public void updateViewsToSetUpBusinessProfile() {
        businessProfileTitle.setText(R.string.business_profile_setup_title);
        businessProfileMessage.setText(R.string.business_profile_setup_msg);
        businessProfilePrimaryBtn.setText(R.string.business_profile_setup_primary_btn);
        businessProfileSecondaryBtn.setText(R.string.business_profile_setup_secondary_btn);

        businessProfilePrimaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!businessProfilePrimaryBtnClicked) {
                    businessProfilePrimaryBtnClicked = true;
                    //Handle Business Profile "Set-Up Now" CTA here
                    String url = getString(R.string.business_profile_setup_url);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
                businessProfilePrimaryBtnClicked = false;
            }
        });

        businessProfileSecondaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!businessProfileSecondaryBtnClicked) {
                    businessProfileSecondaryBtnClicked = true;
                    //Handle Business Profile "Share" CTA here
                    String shareMessage = getString(R.string.business_profile_share_msg);
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
                    startActivityForResult(Intent.createChooser(sharingIntent, "Share via"), Constants.SHARE_REQUEST_CODE);
                }
                businessProfileSecondaryBtnClicked = false;
            }
        });

        businessProfileInviteTnC.setVisibility(View.GONE);
        businessProfileLoaderLayout.setVisibility(View.GONE);
    }

    public void updateViewsForNoMembershipInvite() {
        businessProfileTitle.setText(R.string.business_profile_no_invite_title);
        businessProfileMessage.setText(R.string.business_profile_no_invite_msg);
        businessProfilePrimaryBtn.setText(R.string.business_profile_okay_primary_btn);
        businessProfileInviteTnC.setVisibility(View.GONE);
        businessProfileSecondaryBtn.setVisibility(View.GONE);

        businessProfilePrimaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
