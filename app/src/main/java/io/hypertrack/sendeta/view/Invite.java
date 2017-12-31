package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.lib.HyperTrack;

import org.json.JSONException;
import org.json.JSONObject;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.presenter.IInvitePresenter;
import io.hypertrack.sendeta.presenter.InvitePresenter;
import io.hypertrack.sendeta.store.SharedPreferenceManager;

/**
 * Created by Aman on 19/07/17.
 */

public class Invite extends BaseActivity implements InviteView {

    private static final String TAG = Invite.class.getSimpleName();
    private TextView permissionText, cancel;
    private Button accept;
    private ProgressBar progressBar;
    private IInvitePresenter<InviteView> presenter = new InvitePresenter();
    public static final String ACCOUNT_ID_KEY = "account_id";
    public static final String USER_ID_KEY = "user_id";
    public static final String HAS_ACCEPTED_KEY = "has_accepted";
    public static final String ACCOUNT_NAME_KEY = "account_name";
    public static final String AUTO_ACCEPT_KEY = "auto_accept";
    private String accountID, userID;
    private boolean hasAccepted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        initView();
        setData();
        presenter.attachView(this);
    }

    private void setData() {
        if (getIntent() != null && getIntent().getStringExtra("branch_params") != null) {
            try {
                JSONObject branchParams = new JSONObject(getIntent().getStringExtra("branch_params"));
                hasAccepted = branchParams.getBoolean(HAS_ACCEPTED_KEY);
                accountID = branchParams.getString(ACCOUNT_ID_KEY);
                userID = HyperTrack.getUserId();
                String accountName = branchParams.getString(ACCOUNT_NAME_KEY);
                if (!hasAccepted) {
                    accept.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                    SpannableStringBuilder str = new SpannableStringBuilder(accountName +
                            " wants access to your location data collected on HyperTrack Live");
                    str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
                            accountName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    permissionText.setText(str);
                } else {
                    cancel.setVisibility(View.INVISIBLE);
                    accept.setText("Continue");
                    accept.setVisibility(View.VISIBLE);
                    String temp = "You are already sharing your location with ";
                    SpannableStringBuilder str = new SpannableStringBuilder(temp + accountName);
                    str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), temp.length(),
                            accountName.length() + temp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    permissionText.setText(str);
                }
                progressBar.setVisibility(View.GONE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initView() {
        permissionText = (TextView) findViewById(R.id.permission_text);
        cancel = (TextView) findViewById(R.id.cancel);
        accept = (Button) findViewById(R.id.accept);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskStackBuilder.create(Invite.this)
                        .addNextIntentWithParentStack(new Intent(Invite.this, Placeline.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        .startActivities();
                finish();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!hasAccepted) {
                    if (progressBar != null)
                        progressBar.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onClick: Accept");
                    presenter.acceptInvite(userID, accountID, Invite.this);
                } else {
                    Log.d(TAG, "onClick: Continue");
                    HyperTrack.setUserId(userID);
                    if (progressBar != null)
                        progressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void inviteAccepted() {
        SharedPreferenceManager.deleteAction(this);
        SharedPreferenceManager.deletePlace(this);
        HyperLog.i(TAG, "User Registration successful: Clearing Active Trip, if any");
        TaskStackBuilder.create(Invite.this)
                .addNextIntentWithParentStack(new Intent(Invite.this, Placeline.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .startActivities();
        finish();
    }

    @Override
    public void showError() {
        Toast.makeText(this, "There is some error occurred. Please try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        // Detach View from Presenter
        presenter.detachView();
        super.onDestroy();
    }
}
