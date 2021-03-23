package com.hypertrack.live.views;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hypertrack.live.R;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class SignupInfoPage {
    private static final String TAG = "SignupInfoPage";
    public static final String CUSTOM_STATE = "custom:state";
    public static final String CUSTOM_COMPANY = "custom:company";
    public static final String CUSTOM_USE_CASE = "custom:use_case";
//    public static final String CUSTOM_SCALE = "custom:scale";


    public static View getSignupInfoPageView(
            LayoutInflater inflater, ViewGroup collection,
            Resources resources, final Map<String, String> cognitoUserAttributes,
            final TextView incorrect
    ) {
        View view = inflater.inflate(R.layout.view_pager_signup_info, collection, false);
        Spinner useCaseSelector = view.findViewById(R.id.categories);
        Spinner scaleSelector = view.findViewById(R.id.scale);
        Spinner stateSelector = view.findViewById(R.id.state);
        final List<String> categories = Arrays.asList(resources.getStringArray(R.array.category_names));
        final List<String> scale = Arrays.asList(resources.getStringArray(R.array.scale_names));
        final List<String> state = Arrays.asList(resources.getStringArray(R.array.state_names));

        useCaseSelector.setSelection(categories.indexOf(cognitoUserAttributes.get(CUSTOM_USE_CASE)));
        useCaseSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                incorrect.setVisibility(View.INVISIBLE);
                if (i == 0) {
                    cognitoUserAttributes.remove(CUSTOM_USE_CASE);
                    Log.d(TAG, "use case unselected");
                } else {
                    cognitoUserAttributes.put(CUSTOM_USE_CASE, categories.get(i));
                    Log.d(TAG, "Selected use case " + categories.get(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
//        scaleSelector.setSelection(scale.indexOf(cognitoUserAttributes.get(CUSTOM_SCALE)));
//        scaleSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                incorrect.setVisibility(View.INVISIBLE);
//                if (i == 0) {
//                    cognitoUserAttributes.remove(CUSTOM_SCALE);
//                    Log.d(TAG, "scale unselected");
//                } else {
//                    cognitoUserAttributes.put(CUSTOM_SCALE, scale.get(i));
//                    Log.d(TAG, "scale selection: " + scale.get(i));
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//            }
//        });
        stateSelector.setSelection(state.indexOf(cognitoUserAttributes.get(CUSTOM_STATE)));
        stateSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                incorrect.setVisibility(View.INVISIBLE);
                if (i == 0) {
                    cognitoUserAttributes.remove(CUSTOM_STATE);
                    Log.d(TAG, "state unselected");
                } else {
                    cognitoUserAttributes.put(CUSTOM_STATE, state.get(i));
                    Log.d(TAG, "state selected: " + state.get(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        return view;
    }
}
