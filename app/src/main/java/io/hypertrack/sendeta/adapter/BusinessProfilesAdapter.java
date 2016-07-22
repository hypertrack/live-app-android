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
import io.hypertrack.sendeta.adapter.callback.BusinessProfileOnClickListener;
import io.hypertrack.sendeta.model.BusinessProfileModel;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.UserStore;

/**
 * Created by piyush on 22/07/16.
 */
public class BusinessProfilesAdapter extends RecyclerView.Adapter<BusinessProfilesAdapter.BusinessProfileViewHolder> {

    private List<BusinessProfileModel> businessProfiles = new ArrayList<>();
    private BusinessProfileOnClickListener listener;

    private BusinessProfilesAdapter() {
    }

    public BusinessProfilesAdapter(List<BusinessProfileModel> businessProfiles, BusinessProfileOnClickListener listener) {
        this.businessProfiles = businessProfiles != null ? businessProfiles : new ArrayList<BusinessProfileModel>();
        this.listener = listener;
    }

    @Override
    public BusinessProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_business_profile, parent, false);
        return new BusinessProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BusinessProfileViewHolder holder, int position) {
        if (this.isLastRow(position)) {
            holder.mBusinessProfileName.setText("Add Business Profile");
            holder.mBusinessProfileActionIcon.setImageResource(R.drawable.ic_action_add);
        } else {
            BusinessProfileModel profile = this.businessProfiles.get(position);
            holder.mBusinessProfileName.setText(profile.getCompanyName());
            if (profile.isVerified()) {
                holder.mBusinessProfileActionIcon.setImageResource(R.drawable.ic_action_delete);
            } else {
                holder.mBusinessProfileActionIcon.setImageResource(R.drawable.ic_action_add);
            }
        }
    }

    @Override
    public int getItemCount() {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return 0;
        }

        int defaultRow = 1;

        return this.businessProfiles.size() + defaultRow;
    }

    private boolean isLastRow(int position) {
        if (businessProfiles.size() == position) {
            return true;
        }

        return false;
    }

    public class BusinessProfileViewHolder extends RecyclerView.ViewHolder {

        private TextView mBusinessProfileName;
        private ImageView mBusinessProfileActionIcon;

        public BusinessProfileViewHolder(View view) {
            super(view);
            mBusinessProfileName = (TextView) view.findViewById(R.id.item_business_profile_name);
            mBusinessProfileActionIcon = (ImageView) view.findViewById(R.id.item_business_profile_action_icon);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Check if the row clicked had a valid position index
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {

                        // User clicked to set-up Business Profile
                        if (isLastRow(getAdapterPosition())) {
                            listener.onAddBusinessProfile();

                        } else {
                            final BusinessProfileModel businessProfile = businessProfiles.get(getAdapterPosition());

                            // Check if user clicked to remove an existing Business Profile or verify a pending invite
                            if (businessProfile.isVerified()) {
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
