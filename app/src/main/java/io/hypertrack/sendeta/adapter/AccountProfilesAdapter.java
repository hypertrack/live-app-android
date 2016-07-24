package io.hypertrack.sendeta.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.callback.AccountProfileOnClickListener;
import io.hypertrack.sendeta.model.AccountProfile;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.UserStore;

/**
 * Created by piyush on 22/07/16.
 */
public class AccountProfilesAdapter extends RecyclerView.Adapter<AccountProfilesAdapter.AccountProfileViewHolder> {

    private List<AccountProfile> accountProfiles = new ArrayList<>();
    private AccountProfileOnClickListener listener;

    private AccountProfilesAdapter() {
    }

    public AccountProfilesAdapter(List<AccountProfile> accountProfiles, AccountProfileOnClickListener listener) {
        this.accountProfiles = accountProfiles;
        this.listener = listener;
    }

    @Override
    public AccountProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_account_profile, parent, false);
        return new AccountProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AccountProfileViewHolder holder, int position) {
        // Check if this row is the first in the list
        if (this.isFirstRow(position)) {
            AccountProfile profile = this.accountProfiles.get(position);
            holder.mAccountProfileName.setText(profile.getName());
            holder.mAccountProfileActionIcon.setVisibility(View.GONE);

            // Check if this row is the last in the list
        } else if (this.isLastRow(position)) {
            holder.mAccountProfileName.setText("Add Business Profile");
            holder.mAccountProfileActionIcon.setVisibility(View.VISIBLE);
            holder.mAccountProfileActionIcon.setImageResource(R.drawable.ic_action_add);

        } else {
            AccountProfile profile = this.accountProfiles.get(position);
            holder.mAccountProfileName.setText(profile.getName());

            if (profile.isAccepted()) {
                holder.mAccountProfileActionIcon.setImageResource(R.drawable.ic_action_delete);
            } else {
                holder.mAccountProfileActionIcon.setImageResource(R.drawable.ic_action_add);
            }

            holder.mAccountProfileActionIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return 0;
        }

        int defaultRow = 1;

        return this.accountProfiles.size() + defaultRow;
    }

    private boolean isFirstRow(int position) {
        if (position == 0)
            return true;

        return false;
    }

    private boolean isLastRow(int position) {
        if (accountProfiles.size() == position) {
            return true;
        }

        return false;
    }

    public class AccountProfileViewHolder extends RecyclerView.ViewHolder {

        private TextView mAccountProfileName;
        private ImageView mAccountProfileActionIcon;

        public AccountProfileViewHolder(View view) {
            super(view);
            mAccountProfileName = (TextView) view.findViewById(R.id.item_account_profile_name);
            mAccountProfileActionIcon = (ImageView) view.findViewById(R.id.item_account_profile_action_icon);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Check if the row clicked had a valid position index
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {

                        if (isFirstRow(getAdapterPosition())) {
                            // Do nothing as Personal Account Profile cannot be modified.

                        } else if (isLastRow(getAdapterPosition())) {
                            // User clicked to set-up a Business Account Profile
                            listener.onAddBusinessProfile();

                        } else {
                            final AccountProfile businessProfile = accountProfiles.get(getAdapterPosition());

                            // Check if user clicked to remove an existing Business Account Profile or verify a pending invite
                            if (businessProfile.isAccepted()) {
                                listener.onDeleteBusinessProfile(businessProfile);
                            } else {
                                listener.onVerifyPendingBusinessProfile(businessProfile);
                            }
                        }
                    }
                }
            });
        }
    }
}
