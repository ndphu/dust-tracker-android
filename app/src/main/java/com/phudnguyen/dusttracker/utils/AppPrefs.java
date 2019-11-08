package com.phudnguyen.dusttracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.phudnguyen.dusttracker.MainActivity;
import com.phudnguyen.dusttracker.model.LoginResponse;

public class AppPrefs {

    private static final String PREF_NAME = "appPrefs";

    private static final String PREF_KEY_GROUP_ID = "groupId";
    private static final String PREF_KEY_JWT = "jwt";
    private static final String PREF_CURRENT_USER = "currentUser";
    public static final String PREF_USER_ID = "userId";
    public static final String PREF_USER_NAME = "username";

    public static int getInt(Context context, String key, int defaultValue) {
        return prefs(context).getInt(key, defaultValue);
    }

    public static <T> T get(Context context, String key, Class<T> clazz) {
        String value = prefs(context).getString(key, null);
        if (value == null) return null;
        return GsonUtils.GSON.fromJson(value, clazz);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor edit(Context context) {
        return prefs(context).edit();
    }

    public static void setGroupId(Context context, String groupId) {
        edit(context).putString(PREF_KEY_GROUP_ID, groupId).apply();
    }

    public static String getGroupId(Context context) {
        return prefs(context).getString(PREF_KEY_GROUP_ID, null);
    }

    public static String getJWT(Context context) {
        return prefs(context).getString(PREF_KEY_JWT, null);
    }

    public static void saveLoginSuccess(Context context, LoginResponse response) {
        edit(context)
                .putString(PREF_KEY_JWT, response.getToken())
                .putString(PREF_USER_ID, response.getUser().getId())
                .putString(PREF_USER_NAME, response.getUser().getUsername())
                .apply();
    }

    public static String getUserId(Context context) {
        return prefs(context).getString(PREF_USER_ID, null);
    }

    public static String getUsername(Context context) {
        return prefs(context).getString(PREF_USER_NAME, null);
    }
}
