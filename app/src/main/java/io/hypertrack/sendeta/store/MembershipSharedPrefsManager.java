package io.hypertrack.sendeta.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.util.Constants;

/**
 * Created by piyush on 23/07/16.
 */
public class MembershipSharedPrefsManager {

    private static final String PREF_NAME = Constants.MEMBERSHIP_SHARED_PREFERENCES_NAME;
    private static final String MEMBERSHIPS = "io.hypertrack.meta.membership:Membership";
    private static final String MEMBERSHIP_SELECTED = "io.hypertrack.meta.membership:MembershipSelected";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }

    public static final Integer getMembershipSelected(Context context) {
        String membershipAccountId = getSharedPreferences(context).getString(MEMBERSHIP_SELECTED, null);

        if (TextUtils.isEmpty(membershipAccountId))
            return null;

        return Integer.valueOf(membershipAccountId);
    }

    public static void saveMembershipSelected(Context context, int membershipAccountId) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(MEMBERSHIP_SELECTED, String.valueOf(membershipAccountId));
        editor.apply();
    }

    public static ArrayList<Membership> getMembershipsList(Context context) {
        String membershipsJSON = getSharedPreferences(context).getString(MEMBERSHIPS, null);
        if (TextUtils.isEmpty(membershipsJSON)) {
            return null;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Membership>>() {}.getType();

        return gson.fromJson(membershipsJSON, type);
    }

    public static ArrayList<String> getMembershipNamesList(Context context) {
        ArrayList<Membership> membershipsList = getMembershipsList(context);

        if (membershipsList == null || membershipsList.size() == 0)
            return null;

        ArrayList<String> membershipNames = new ArrayList<>();
        for (Membership model : membershipsList) {
            if (model != null && !TextUtils.isEmpty(model.getAccountName()))
                membershipNames.add(model.getAccountName());
        }

        return membershipNames;
    }

    public static Membership getMembershipForName(Context context, String name) {
        if (TextUtils.isEmpty(name))
            return null;

        // Fetch Memberships From SharedPreferences
        ArrayList<Membership> membershipsList = getMembershipsList(context);

        // Check if there are any Membership saved in SharedPreferences
        if (membershipsList == null || membershipsList.size() == 0)
            return null;

        // Check if any saved Membership matches the given name
        for (Membership model : membershipsList) {
            if (name.equalsIgnoreCase(model.getAccountName())) {
                return model;
            }
        }

        return null;
    }

    public static boolean addMembership(Context context, Membership membership) {
        if (membership == null || TextUtils.isEmpty(membership.getAccountName()))
            return false;

        // Fetch Memberships From SharedPreferences
        ArrayList<Membership> membershipsList = getMembershipsList(context);

        // Check if membershipsList is empty
        if (membershipsList == null || membershipsList.size() == 0) {
            membershipsList = new ArrayList<>();
        }

        // Add the membership instance in the membershipsList
        membershipsList.add(membership);

        SharedPreferences.Editor editor = getEditor(context);
        // Remove existing MEMBERSHIPS Key from SharedPreferences
        editor.remove(MEMBERSHIPS);

        Gson gson = new Gson();
        String membershipsJSON = gson.toJson(membershipsList);

        // Add updated membershipsList in the SharedPreferences
        editor.putString(MEMBERSHIPS, membershipsJSON);
        editor.apply();

        return true;
    }

    public static boolean deleteBusinessProfile(Context context, Membership businessProfile) {
        if (businessProfile == null || TextUtils.isEmpty(businessProfile.getAccountName()))
            return false;

        // Fetch Memberships From SharedPreferences
        ArrayList<Membership> membershipsList = getMembershipsList(context);

        // Check if membershipsList is empty
        if (membershipsList == null || membershipsList.size() == 0) {
            return false;
        }

        // Check if businessProfile exists in the membershipsList & delete it
        for (Membership model : membershipsList) {
            if (model != null && model.getAccountName().equalsIgnoreCase(businessProfile.getAccountName())) {
                membershipsList.remove(model);

                SharedPreferences.Editor editor = getEditor(context);
                // Remove existing MEMBERSHIPS Key from SharedPreferences
                editor.remove(MEMBERSHIPS);

                Gson gson = new Gson();
                String membershipsJSON = gson.toJson(membershipsList);

                // Add updated membershipsList in the SharedPreferences
                editor.putString(MEMBERSHIPS, membershipsJSON);
                editor.apply();

                return true;
            }
        }

        return false;
    }
}
