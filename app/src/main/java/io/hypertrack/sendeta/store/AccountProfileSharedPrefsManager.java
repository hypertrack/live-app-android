package io.hypertrack.sendeta.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import io.hypertrack.sendeta.model.AccountProfile;
import io.hypertrack.sendeta.util.Constants;

/**
 * Created by piyush on 23/07/16.
 */
public class AccountProfileSharedPrefsManager {

    private static final String PREF_NAME = Constants.ACCOUNT_PROFILE_SHARED_PREFERENCES_NAME;
    private static final String ACCOUNT_PROFILES = "io.hypertrack.meta.account_profile:AccountProfiles";
    private static final String ACCOUNT_PROFILE_SELECTED = "io.hypertrack.meta.account_profile:AccountProfileSelected";

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

    public static ArrayList<AccountProfile> getAccountProfilesList(Context context) {
        String accountProfilesJSON = getSharedPreferences(context).getString(ACCOUNT_PROFILES, null);
        if (TextUtils.isEmpty(accountProfilesJSON)) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<AccountProfile>>() {}.getType();

        return gson.fromJson(accountProfilesJSON, type);
    }

    public static ArrayList<String> getAccountProfileNamesList(Context context) {
        ArrayList<AccountProfile> accountProfilesList = getAccountProfilesList(context);

        if (accountProfilesList == null || accountProfilesList.size() == 0)
            return null;

        ArrayList<String> accountProfileNames = new ArrayList<>();
        for (AccountProfile model : accountProfilesList) {
            if (model != null && !TextUtils.isEmpty(model.getName()))
                accountProfileNames.add(model.getName());
        }

        return accountProfileNames;
    }

    public static AccountProfile getAccountProfileForName(Context context, String name) {
        if (TextUtils.isEmpty(name))
            return null;

        // Fetch AccountProfiles From SharedPreferences
        ArrayList<AccountProfile> accountProfilesList = getAccountProfilesList(context);

        // Check if there are any AccountProfiles saved in SharedPreferences
        if (accountProfilesList == null || accountProfilesList.size() == 0)
            return null;

        // Check if any saved AccountProfiles matches the given name
        for (AccountProfile model : accountProfilesList) {
            if (name.equalsIgnoreCase(model.getName())) {
                return model;
            }
        }

        return null;
    }

    public static boolean addAccountProfile(Context context, AccountProfile accountProfile) {
        if (accountProfile == null || TextUtils.isEmpty(accountProfile.getName()))
            return false;

        // Fetch AccountProfiles From SharedPreferences
        ArrayList<AccountProfile> accountProfilesList = getAccountProfilesList(context);

        // Check if accountProfilesList is empty
        if (accountProfilesList == null || accountProfilesList.size() == 0) {
            accountProfilesList = new ArrayList<>();
        }

        // Add the accountProfile instance in the accountProfilesList
        accountProfilesList.add(accountProfile);

        SharedPreferences.Editor editor = getEditor(context);
        // Remove existing ACCOUNT_PROFILES Key from SharedPreferences
        editor.remove(ACCOUNT_PROFILES);

        Gson gson = new Gson();
        String accountProfilesJSON = gson.toJson(accountProfilesList);

        // Add updated accountProfilesList in the SharedPreferences
        editor.putString(ACCOUNT_PROFILES, accountProfilesJSON);
        editor.apply();

        return true;
    }

    public static boolean deleteBusinessProfile(Context context, AccountProfile businessProfile) {
        if (businessProfile == null || TextUtils.isEmpty(businessProfile.getName()))
            return false;

        // Fetch AccountProfiles From SharedPreferences
        ArrayList<AccountProfile> accountProfilesList = getAccountProfilesList(context);

        // Check if accountProfilesList is empty
        if (accountProfilesList == null || accountProfilesList.size() == 0) {
            return false;
        }

        // Check if businessProfile exists in the accountProfilesList & delete it
        for (AccountProfile model : accountProfilesList) {
            if (model != null && model.getName().equalsIgnoreCase(businessProfile.getName())) {
                accountProfilesList.remove(model);

                SharedPreferences.Editor editor = getEditor(context);
                // Remove existing ACCOUNT_PROFILES Key from SharedPreferences
                editor.remove(ACCOUNT_PROFILES);

                Gson gson = new Gson();
                String accountProfilesJSON = gson.toJson(accountProfilesList);

                // Add updated accountProfilesList in the SharedPreferences
                editor.putString(ACCOUNT_PROFILES, accountProfilesJSON);
                editor.apply();

                return true;
            }
        }

        return false;
    }
}
