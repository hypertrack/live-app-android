package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.presenter.BusinessProfilePresenter;
import io.hypertrack.sendeta.presenter.IBusinessProfilePresenter;

/**
 * Created by piyush on 22/07/16.
 */
public class BusinessProfile extends BaseActivity implements BusinessProfileView{

    public static final String TAG = BusinessProfile.class.getSimpleName();
    public static final String KEY_MEMBERSHIP = "membership";
    public static final String KEY_MEMBERSHIP_ID = "membership_id";

    private TextView businessProfileTitle;
    private TextView businessProfileMessage;
    private TextView businessProfileInviteTnC;
    private TextView businessProfilePrimaryBtn;
    private TextView businessProfileSecondaryBtn;

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

        // Attach View Presenter to View
        presenter.attachView(this);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(KEY_MEMBERSHIP_ID)) {
                double accountId = intent.getDoubleExtra(KEY_MEMBERSHIP_ID, -1);

                // Get Membership from Network Call
                presenter.getMembershipForAccountId((int) accountId);
            }

            if (intent.hasExtra(KEY_MEMBERSHIP)) {
                Membership businessProfile = (Membership) intent.getSerializableExtra(KEY_MEMBERSHIP);
                if (businessProfile == null)
                    updateViewsToSetUpBusinessProfile();
                else
                    updateViewsForPendingInvite(businessProfile);
            } else {
                updateViewsToSetUpBusinessProfile();
            }
        }
    }

    @Override
    public void showGetMembershipSuccess(Membership membership) {
        Toast.makeText(BusinessProfile.this, "Membership fetched: " + membership.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showGetMembershipError() {
        Toast.makeText(BusinessProfile.this, "Membership fetched error!", Toast.LENGTH_SHORT).show();
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

    public void updateViewsForPendingInvite(Membership businessProfile) {
        String pendingInviteCompanyName = businessProfile.getName();

        // Check if the pending invite was for a valid company name
        if (TextUtils.isEmpty(pendingInviteCompanyName)) {
            businessProfileTitle.setText(pendingInviteCompanyName);
            businessProfileMessage.setText(getString(R.string.business_profile_invite_message));

            // Show Business Profile Pending Invite TnC View with companyName as a parameter
            businessProfileInviteTnC.setText(getString(R.string.business_profile_invite_tnc, pendingInviteCompanyName));
            businessProfileInviteTnC.setVisibility(View.VISIBLE);

            businessProfilePrimaryBtn.setText(getString(R.string.business_profile_invite_primary_btn));
            businessProfilePrimaryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Handle Business Profile "Accept" CTA here
                    presenter.attemptVerifyPendingBusinessProfile();
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
}
