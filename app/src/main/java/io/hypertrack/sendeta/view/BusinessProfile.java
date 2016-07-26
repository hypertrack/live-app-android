package io.hypertrack.sendeta.view;

import android.content.Intent;
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

/**
 * Created by piyush on 22/07/16.
 */
public class BusinessProfile extends BaseActivity implements BusinessProfileView {

    public static final String TAG = BusinessProfile.class.getSimpleName();
    public static final String KEY_MEMBERSHIP = "membership";
    public static final String KEY_MEMBERSHIP_ID = "membership_id";

    private TextView businessProfileTitle;
    private TextView businessProfileMessage;
    private TextView businessProfileInviteTnC;
    private TextView businessProfilePrimaryBtn;
    private TextView businessProfileSecondaryBtn;
    private TextView businessProfileLoaderText;
    private LinearLayout businessProfileLoaderLayout;

    private IBusinessProfilePresenter<BusinessProfileView> presenter = new BusinessProfilePresenter();

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

        // Attach View Presenter to View
        presenter.attachView(this);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(KEY_MEMBERSHIP_ID)) {
                businessProfileLoaderLayout.setVisibility(View.VISIBLE);

                // Get Membership from Network Call
                presenter.getMembershipForAccountId();

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

        if (membership != null) {
            updateViewsForMembershipInvite(membership);
        } else {
            updateViewsForNoMembershipInvite();
        }

        showGetMembershipSuccess();
    }

    @Override
    public void showGetMembershipSuccess() {
        businessProfileLoaderLayout.setVisibility(View.GONE);
        Toast.makeText(BusinessProfile.this, "Membership success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showGetMembershipError() {
        updateViewsForNoMembershipInvite();

        businessProfileLoaderLayout.setVisibility(View.GONE);
        Toast.makeText(BusinessProfile.this, "Membership fetched error!", Toast.LENGTH_SHORT).show();
    }

    public void updateViewsForMembershipInvite(final Membership membership) {
        String accountName = membership.getAccountName();
        // Check if the pending invite was for a valid company name
        if (!TextUtils.isEmpty(accountName)) {
            businessProfileTitle.setText(accountName);
            businessProfileMessage.setText(getString(R.string.business_profile_invite_message));

            // Show Business Profile Pending Invite TnC View with companyName as a parameter
            businessProfileInviteTnC.setText(getString(R.string.business_profile_invite_tnc, accountName));
            businessProfileInviteTnC.setVisibility(View.VISIBLE);

            businessProfilePrimaryBtn.setText(getString(R.string.business_profile_invite_primary_btn));
            businessProfilePrimaryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Handle Business Profile "Accept" CTA here
                    presenter.attemptVerifyPendingBusinessProfile(membership);
                }
            });

            businessProfileSecondaryBtn.setText(getString(R.string.business_profile_invite_secondary_btn));
            businessProfileSecondaryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Handle Business Profile "Reject" CTA here

                }
            });
        } else {
            // Fallback to handle an empty company name
            updateViewsToSetUpBusinessProfile();
        }
    }

    public void updateViewsToSetUpBusinessProfile() {
        businessProfileTitle.setText(getString(R.string.business_profile_setup_title));
        businessProfileMessage.setText(getString(R.string.business_profile_setup_message));

        // Hide Business Profile Pending Invite TnC View
        businessProfileInviteTnC.setVisibility(View.GONE);

        businessProfilePrimaryBtn.setText(getString(R.string.business_profile_setup_primary_btn));
        businessProfilePrimaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle Business Profile "Set-Up Now" CTA here
            }
        });

        businessProfileSecondaryBtn.setText(getString(R.string.business_profile_setup_secondary_btn));
        businessProfileSecondaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Handle Business Profile "Share" CTA here
            }
        });
    }

    public void updateViewsForNoMembershipInvite() {
        businessProfileTitle.setText(R.string.business_profile_no_invite_title);
        businessProfileMessage.setText(R.string.business_profile_no_invite_message);
        businessProfilePrimaryBtn.setText(R.string.business_profile_okay_primary_btn);
        businessProfileSecondaryBtn.setVisibility(View.GONE);

        businessProfilePrimaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
