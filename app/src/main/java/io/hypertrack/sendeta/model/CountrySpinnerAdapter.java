
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.hypertrack.sendeta.R;

/**
 * Created by suhas on 19/01/16.
 */
public class CountrySpinnerAdapter extends ArrayAdapter<Country> {

    private LayoutInflater inflater;
    private ArrayList<Country> countries;

    public CountrySpinnerAdapter(Context context, int resource, ArrayList<Country> list) {
        super(context, resource, list);
        inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        countries = list;
    }

    private View getCustomDialogView(int position, ViewGroup parent) {
        View layout = inflater.inflate(R.layout.view_country_list_item, parent, false);

        TextView countryName = (TextView)layout.findViewById(R.id.tv_country_name);
        ImageView countryFlag = (ImageView)layout.findViewById(R.id.iv_flag);

        countryName.setText(countries.get(position).mCountryName);
        countryName.setVisibility(View.VISIBLE);
        countryFlag.setImageResource(countries.get(position).mImageId);

        return layout;
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView,
                                @NonNull ViewGroup parent) {
        return getCustomDialogView(position, parent);
    }

    // It gets a View that displays the data at the specified position
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomSelectionView(position, parent);
    }

    private View getCustomSelectionView(int position, ViewGroup parent) {
        View layout = inflater.inflate(R.layout.view_country_list_item, parent, false);

        TextView countryName = (TextView)layout.findViewById(R.id.tv_country_name);
        ImageView countryFlag = (ImageView)layout.findViewById(R.id.iv_flag);

        countryName.setVisibility(View.GONE);
        countryFlag.setImageResource(countries.get(position).mImageId);

        return layout;
    }
}