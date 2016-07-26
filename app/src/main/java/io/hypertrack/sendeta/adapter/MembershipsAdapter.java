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
import io.hypertrack.sendeta.adapter.callback.MembershipOnClickListener;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.UserStore;

/**
 * Created by piyush on 22/07/16.
 */
public class MembershipsAdapter extends RecyclerView.Adapter<MembershipsAdapter.MembershipViewHolder> {

    private List<Membership> memberships = new ArrayList<>();
    private MembershipOnClickListener listener;

    private MembershipsAdapter() {
    }

    public MembershipsAdapter(List<Membership> memberships, MembershipOnClickListener listener) {
        if (memberships != null) {
            this.memberships = memberships;
        }
        this.listener = listener;
    }

    public void setMembershipsList(List<Membership> memberships) {
        if (memberships == null)
            memberships = new ArrayList<>();

        this.memberships = memberships;
    }
    @Override
    public MembershipViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_membership, parent, false);
        return new MembershipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MembershipViewHolder holder, int position) {
        // Check if this row is the first in the list
        if (this.isDefaultRow(position)) {
            Membership profile = this.memberships.get(position);
            holder.mMembershipName.setText(profile.getAccountName());
            holder.mMembershipActionIcon.setVisibility(View.GONE);

            // Check if this row is the last in the list
        } else if (this.isLastRow(position)) {
            holder.mMembershipName.setText("Add Business Profile");
            holder.mMembershipActionIcon.setVisibility(View.VISIBLE);
            holder.mMembershipActionIcon.setImageResource(R.drawable.ic_action_add);

        } else {
            Membership profile = this.memberships.get(position);
            holder.mMembershipName.setText(profile.getAccountName());

            if (profile.isAccepted()) {
                holder.mMembershipActionIcon.setImageResource(R.drawable.ic_action_delete);
            } else {
                holder.mMembershipActionIcon.setImageResource(R.drawable.ic_action_add);
            }

            holder.mMembershipActionIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return 0;
        }

        int defaultRow = 1;

        return this.memberships.size() + defaultRow;
    }

    private boolean isDefaultRow(int position) {
        if (position < memberships.size() && memberships.get(position) != null
                && memberships.get(position).isDefault())
            return true;

        return false;
    }

    private boolean isLastRow(int position) {
        if (memberships.size() == position) {
            return true;
        }

        return false;
    }

    public class MembershipViewHolder extends RecyclerView.ViewHolder {

        private TextView mMembershipName;
        private ImageView mMembershipActionIcon;

        public MembershipViewHolder(View view) {
            super(view);
            mMembershipName = (TextView) view.findViewById(R.id.item_membership_name);
            mMembershipActionIcon = (ImageView) view.findViewById(R.id.item_membership_action_icon);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Check if the row clicked had a valid position index
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {

                        if (isDefaultRow(getAdapterPosition())) {
                            // Do nothing as Personal Membership cannot be modified.

                        } else if (isLastRow(getAdapterPosition())) {
                            // User clicked to set-up a Business Membership
                            listener.onAddMembership();

                        } else {
                            final Membership membership = memberships.get(getAdapterPosition());

                            // Check if user clicked to remove an existing Business Membership or verify a pending invite
                            if (membership.isAccepted()) {
                                listener.onDeleteMembership(membership);
                            } else {
                                listener.onVerifyPendingMembership(membership);
                            }
                        }
                    }
                }
            });
        }
    }
}
