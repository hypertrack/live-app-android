package io.hypertrack.sendeta.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import io.hypertrack.sendeta.model.BusinessProfileModel;
import io.hypertrack.sendeta.util.Constants;

/**
 * Created by piyush on 23/07/16.
 */
public class BusinessProfileSharedPrefsManager {

    private static final String PREF_NAME = Constants.BUSINESS_PROFILE_SHARED_PREFERENCES_NAME;
    private static final String BUSINESS_PROFILES = "io.hypertrack.meta.business_profile:BusinessProfiles";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public static ArrayList<BusinessProfileModel> getBusinessProfiles(Context context) {
        String businessProfilesJSON = getSharedPreferences(context).getString(BUSINESS_PROFILES, null);
        if (TextUtils.isEmpty(businessProfilesJSON)) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<BusinessProfileModel>>() {
        }.getType();

        return gson.fromJson(businessProfilesJSON, type);
    }

    public static boolean addBusinessProfile(Context context, BusinessProfileModel businessProfile) {
        if (businessProfile == null || TextUtils.isEmpty(businessProfile.getCompanyName()))
            return false;

        // Fetch BusinessProfiles From SharedPreferences
        ArrayList<BusinessProfileModel> businessProfilesList = BusinessProfileSharedPrefsManager.getBusinessProfiles(context);

        // Check if businessProfilesList is empty
        if (businessProfilesList == null || businessProfilesList.size() == 0) {
            businessProfilesList = new ArrayList<>();
        }

        // Add the businessProfile instance in the businessProfilesList
        businessProfilesList.add(businessProfile);

        SharedPreferences.Editor editor = getEditor(context);
        // Remove existing BUSINESS_PROFILES Key from SharedPreferences
        editor.remove(BUSINESS_PROFILES);

        Gson gson = new Gson();
        String businessProfileJSON = gson.toJson(businessProfilesList);

        // Add updated businessProfilesList in the SharedPreferences
        editor.putString(BUSINESS_PROFILES, businessProfileJSON);
        editor.apply();

        return true;
    }

    public static boolean deleteBusinessProfile(Context context, BusinessProfileModel businessProfile) {
        if (businessProfile == null || TextUtils.isEmpty(businessProfile.getCompanyName()))
            return false;

        // Fetch BusinessProfiles From SharedPreferences
        ArrayList<BusinessProfileModel> businessProfilesList = BusinessProfileSharedPrefsManager.getBusinessProfiles(context);

        // Check if businessProfilesList is empty
        if (businessProfilesList == null || businessProfilesList.size() == 0) {
            return false;
        }

        // Check if businessProfile exists in the businessProfilesList & delete it
        for (BusinessProfileModel model : businessProfilesList) {
            if (model != null && model.getCompanyName().equalsIgnoreCase(businessProfile.getCompanyName())) {
                businessProfilesList.remove(model);

                SharedPreferences.Editor editor = getEditor(context);
                // Remove existing BUSINESS_PROFILES Key from SharedPreferences
                editor.remove(BUSINESS_PROFILES);

                Gson gson = new Gson();
                String businessProfileJSON = gson.toJson(businessProfilesList);

                // Add updated businessProfilesList in the SharedPreferences
                editor.putString(BUSINESS_PROFILES, businessProfileJSON);
                editor.apply();

                return true;
            }
        }

        return false;
    }
}
