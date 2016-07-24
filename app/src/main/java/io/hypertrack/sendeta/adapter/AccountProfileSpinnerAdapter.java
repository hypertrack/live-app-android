package io.hypertrack.sendeta.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 24/07/16.
 */
public class AccountProfileSpinnerAdapter extends ArrayAdapter<String>{
    private Context mContext;
    private String userName;
    private ArrayList<String> accountProfilesList;

    public AccountProfileSpinnerAdapter(Context mContext, int resource, String userName, ArrayList<String> accountProfilesList) {
        super(mContext, resource, accountProfilesList);
        this.mContext = mContext;
        this.userName = userName;
        this.accountProfilesList = accountProfilesList;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("DROPDOWN")) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_drawer_spinner_dropdown_item, parent, false);
            convertView.setTag("DROPDOWN");
        }

        ImageView accountProfileIcon = (ImageView) convertView.findViewById(R.id.spinner_dropdown_item_icon);
        TextView accountProfileName = (TextView) convertView.findViewById(R.id.spinner_dropdown_item_name);
        accountProfileIcon.setVisibility(View.VISIBLE);
        accountProfileName.setText(accountProfilesList.get(position));

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !convertView.getTag().toString().equals("NON_DROPDOWN")) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.layout_drawer_spinner, parent, false);
            convertView.setTag("NON_DROPDOWN");
        }

        TextView userNameView = (TextView) convertView.findViewById(R.id.drawer_spinner_user_name);
        TextView selectedProfileNameView = (TextView) convertView.findViewById(R.id.drawer_spiner_selected_profile_name);
        if (!TextUtils.isEmpty(userName)) {
            userNameView.setVisibility(View.VISIBLE);
            userNameView.setText(userName);
        }
        selectedProfileNameView.setText(accountProfilesList.get(position));

        return convertView;
    }
}