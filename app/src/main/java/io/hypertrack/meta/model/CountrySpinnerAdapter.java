package io.hypertrack.meta.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.meta.R;

/**
 * Created by suhas on 19/01/16.
 */
public class CountrySpinnerAdapter extends ArrayAdapter<Country> {

    LayoutInflater inflater;
    ArrayList<Country> countries;

    public CountrySpinnerAdapter(Context context, int resource, ArrayList<Country> list) {
        super(context, resource, list);
        inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        countries = list;
    }

    public View getCustomView(int position, View convertView,
                              ViewGroup parent) {

        View layout = inflater.inflate(R.layout.view_country_list_item, parent, false);

        TextView countryName = (TextView)layout.findViewById(R.id.tv_country_name);
        ImageView countryFlag = (ImageView)layout.findViewById(R.id.iv_flag);

        countryName.setText(countries.get(position).mCountryIso);
        countryFlag.setImageResource(countries.get(position).mImageId);

        return layout;
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // It gets a View that displays the data at the specified position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
}
