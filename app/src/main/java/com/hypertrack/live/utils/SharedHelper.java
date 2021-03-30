package com.hypertrack.live.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.live.R;
import com.hypertrack.live.models.PlaceModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SharedHelper {

    private static final String PUB_KEY = "pub_key";
    private static final String USER_EMAIL_KEY = "user_email";
    public static final String USER_NAME_KEY = "user_name";
    private static final String USER_PHONE_KEY = "user_phone";
    private static final String HOME_PLACE_KEY = "home_place";
    private static final String USER_HOME_ADDRESS_KEY = "user_home_address";
    private static final String USER_HOME_LATLON_KEY = "user_home_latlon";

    private static final String RECENT = "recent";
    private static final String SELECTED_TRIP_ID = "selected_trip_id";
    private static final String CREATED_TRIP_ID = "created_trip_id";
    private static final String CREATED_TRIP_SHARE_URL = "created_trip_share_url";
    private static final String LOGIN_TYPE = "login_type";

    public static final String LOGIN_TYPE_DEEPLINK = "com.hypertrack.live.utils.LOGIN_TYPE_DEEPLINK";
    public static final String LOGIN_TYPE_COGNITO = "com.hypertrack.live.utils.LOGIN_TYPE_COGNITO";
    private static final String INVITE_LINK_KEY = "com.hypertrack.live.utils.INVITE_LINK_KEY";

    @StringDef({LOGIN_TYPE_COGNITO, LOGIN_TYPE_DEEPLINK}) @Retention(RetentionPolicy.SOURCE)
    public @interface LoginType {}

    private static SharedHelper instance;

    private final Gson gson = new Gson();
    private final SharedPreferences preferences;

    public static synchronized SharedHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedHelper(context);
        }
        return instance;
    }

    private SharedHelper(Context context) {
        preferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }

    @NonNull
    public String getAccountEmail() {
        return preferences.getString(USER_EMAIL_KEY, "");
    }

    public void setAccountEmail(@NonNull String email) {
        preferences.edit().putString(USER_EMAIL_KEY, email).apply();
    }

    @NonNull
    public String getInviteLink() {
        return preferences.getString(INVITE_LINK_KEY, "");
    }

    public void setInviteLink(@NonNull String inviteLink) {
        preferences.edit().putString(INVITE_LINK_KEY, inviteLink).apply();
    }

    @NonNull
    public String getHyperTrackPubKey() {
        return preferences.getString(PUB_KEY, "");
    }

    public void setHyperTrackPubKey(@NonNull String hyperTrackPubKey) {
        preferences.edit()
                .putString(SharedHelper.PUB_KEY, hyperTrackPubKey)
                .apply();
    }

    public void removeHyperTrackPubKey() {
        preferences.edit().remove(PUB_KEY).apply();
    }

    public void setUserNameAndPhone(@Nullable String name, @Nullable String phone) {
        preferences.edit()
                .putString(SharedHelper.USER_NAME_KEY, name)
                .putString(SharedHelper.USER_PHONE_KEY, phone)
                .apply();
    }

    @NonNull public Map<String, Object> getDeviceMetadata() {

        Map<String, Object> map = new HashMap<>(2);
        map.put(USER_NAME_KEY, preferences.getString(USER_NAME_KEY, ""));
        map.put(USER_PHONE_KEY, preferences.getString(USER_PHONE_KEY, ""));
        return map;
    }

    @Nullable public String getUserName() { return preferences.getString(USER_NAME_KEY, null); }

    public boolean isHomePlaceSet() {
        return preferences.contains(HOME_PLACE_KEY);
    }

    @Nullable
    public PlaceModel getHomePlace() {
        try {
            return gson.fromJson(
                    preferences.getString(HOME_PLACE_KEY, null),
                    new TypeToken<PlaceModel>() {}.getType()
            );
        } catch (JsonSyntaxException ignored) { }
        return null;
    }

    public void setHomePlace(@Nullable PlaceModel home) {
        String homeJson = gson.toJson(home);
        preferences
                .edit()
                .putString(SharedHelper.HOME_PLACE_KEY, homeJson)
                .apply();
    }

    @NonNull
    public Set<PlaceModel> getRecentPlaces() {
        String recentJson = preferences.getString(RECENT, "[]");
        Type listType = new TypeToken<Set<PlaceModel>>() {}.getType();
        try {
            return gson.fromJson(recentJson, listType);
        } catch (JsonSyntaxException ignored) { }

        return Collections.emptySet();
    }

    public void setRecentPlaces(@Nullable Collection<PlaceModel> recentPlaces) {
        if (recentPlaces == null) recentPlaces = Collections.emptySet();
        String recentJson = gson.toJson(recentPlaces);
        preferences.edit().putString(RECENT, recentJson).apply();
    }

    @Nullable
    public String getSelectedTripId() {
        return preferences.getString(SELECTED_TRIP_ID, null);
    }

    public void setSelectedTripId(@NonNull String tripId) {
        preferences.edit().putString(SELECTED_TRIP_ID, tripId).apply();
    }

    public void clearSelectedTripId() {
        preferences.edit().remove(SELECTED_TRIP_ID).apply();
    }

    @Nullable
    public String getCreatedTripId() {
        return preferences.getString(CREATED_TRIP_ID, null);
    }

    public void setCreatedTripId(@Nullable String tripId) {
        preferences.edit().putString(CREATED_TRIP_ID, tripId).apply();
    }

    @Nullable
    public String getShareUrl() {
        return preferences.getString(CREATED_TRIP_SHARE_URL, null);
    }

    public void setShareUrl(@Nullable String shareUrl) {
        preferences.edit().putString(CREATED_TRIP_SHARE_URL, shareUrl).apply();
    }

    @LoginType public String getLoginType() {
        return preferences.getString(LOGIN_TYPE, LOGIN_TYPE_COGNITO);
    }

    public void setLoginType(@LoginType String loginType) {
        preferences.edit().putString(LOGIN_TYPE, loginType).apply();
    }

    public void logout() {
        preferences.edit().clear().apply();
    }
}
