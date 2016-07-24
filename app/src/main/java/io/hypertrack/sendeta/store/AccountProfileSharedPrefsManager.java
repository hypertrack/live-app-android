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
public class AccountProfileSharedPrefsManager {

    private static final String PREF_NAME = Constants.BUSINESS_PROFILE_SHARED_PREFERENCES_NAME;
    private static final String BUSINESS_PROFILES = "io.hypertrack.meta.business_profile:BusinessProfiles";
    private static final String ACCOUNT_PROFILE_SELECTED = "io.hypertrack.meta.business_profile:AccountProfileSelected";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public static final String getAccountProfileSelected(Context context) {
        String accountProfileName = getSharedPreferences(context).getString(ACCOUNT_PROFILE_SELECTED, null);
        return accountProfileName;
    }

    public static void saveAccountProfileSelected(Context context, String accountProfileName) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(ACCOUNT_PROFILE_SELECTED, accountProfileName);
        editor.apply();
    }

    public static ArrayList<BusinessProfileModel> getBusinessProfilesList(Context context) {
        String businessProfilesJSON = getSharedPreferences(context).getString(BUSINESS_PROFILES, null);
        if (TextUtils.isEmpty(businessProfilesJSON)) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<BusinessProfileModel>>() {}.getType();

        return gson.fromJson(businessProfilesJSON, type);
    }

    public static ArrayList<String> getBusinessProfileNamesList(Context context) {
        ArrayList<BusinessProfileModel> businessProfiles = getBusinessProfilesList(context);

        if (businessProfiles == null || businessProfiles.size() == 0)
            return null;

        ArrayList<String> businessProfileNames = new ArrayList<>();
        for (BusinessProfileModel model : businessProfiles) {
            if (model != null && !TextUtils.isEmpty(model.getCompanyName()))
                businessProfileNames.add(model.getCompanyName());
        }

        return businessProfileNames;
    }

    public static BusinessProfileModel getBusinessProfileForCompanyName(Context context, String companyName) {
        if (TextUtils.isEmpty(companyName))
            return null;

        // Fetch BusinessProfiles From SharedPreferences
        ArrayList<BusinessProfileModel> businessProfilesList = getBusinessProfilesList(context);

        // Check if there are any BusinessProfiles saved in SharedPreferences
        if (businessProfilesList == null || businessProfilesList.size() == 0)
            return null;

        // Check if any saved BusinessProfiles matches the given companyName
        for (BusinessProfileModel model : businessProfilesList) {
            if (companyName.equalsIgnoreCase(model.getCompanyName())) {
                return model;
            }
        }

        return null;
    }

    public static boolean addBusinessProfile(Context context, BusinessProfileModel businessProfile) {
        if (businessProfile == null || TextUtils.isEmpty(businessProfile.getCompanyName()))
            return false;

        // Fetch BusinessProfiles From SharedPreferences
        ArrayList<BusinessProfileModel> businessProfilesList = getBusinessProfilesList(context);

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
        ArrayList<BusinessProfileModel> businessProfilesList = getBusinessProfilesList(context);

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
