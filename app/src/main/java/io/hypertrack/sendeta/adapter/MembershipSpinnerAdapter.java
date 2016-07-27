package io.hypertrack.sendeta.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Membership;

/**
 * Created by piyush on 24/07/16.
 */
public class MembershipSpinnerAdapter extends ArrayAdapter<Membership> {
    private Context mContext;
    private String userName;
    private List<Membership> membershipsList;
    private int dropdownResourceId, spinnerResourceId;

    public MembershipSpinnerAdapter(Context mContext, int spinnerResourceId, int dropdownResourceId,
                                    String userName, List<Membership> membershipsList) {
        super(mContext, dropdownResourceId, membershipsList);
        this.mContext = mContext;
        this.userName = userName;
        this.membershipsList = membershipsList;
        this.spinnerResourceId = spinnerResourceId;
        this.dropdownResourceId = dropdownResourceId;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("DROPDOWN")) {
            convertView = LayoutInflater.from(mContext).inflate(dropdownResourceId, parent, false);
            convertView.setTag("DROPDOWN");
        }

        TextView accountProfileName = (TextView) convertView.findViewById(R.id.spinner_dropdown_item_name);
        accountProfileName.setText(membershipsList.get(position).getAccountName());

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("NON_DROPDOWN")) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(spinnerResourceId, parent, false);
            convertView.setTag("NON_DROPDOWN");
        }

        TextView userNameView = (TextView) convertView.findViewById(R.id.spinner_user_name);
        TextView selectedAccountNameView = (TextView) convertView.findViewById(R.id.spinner_selected_account_name);
        ImageView membershipIconView = (ImageView) convertView.findViewById(R.id.spinner_selected_account_icon);

        if (userNameView != null && !TextUtils.isEmpty(userName)) {
            userNameView.setVisibility(View.VISIBLE);
            userNameView.setText(userName);
        }

        if (membershipIconView != null && membershipsList.get(position) != null) {
            if (membershipsList.get(position).isPersonal()) {
                membershipIconView.setImageResource(R.drawable.ic_home);
            } else {
                membershipIconView.setImageResource(R.drawable.ic_work);
            }
        }

        selectedAccountNameView.setText(membershipsList.get(position).getAccountName());

        return convertView;
    }
}