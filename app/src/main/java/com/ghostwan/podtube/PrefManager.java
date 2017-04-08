package com.ghostwan.podtube;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by erwan on 28/03/2017.
 */

public class PrefManager {

    public static String PREFERENCE_FEED_LIST = "com.ghostwan.podtube.PREFERENCE_FEED_LIST";

    private final SharedPreferences sharedPrefs;

    public PrefManager(Context context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public List<FeedInfo> loadFeedInfo() {

        Gson gson = new Gson();
        String json = sharedPrefs.getString(PREFERENCE_FEED_LIST, null); //Retrieve previously saved data
        Type type = new TypeToken<List<FeedInfo>>() {}.getType();
        if (json != null) {
            return gson.fromJson(json, type); //Restore previous data
        }
        return new ArrayList<>();
    }

    public void saveFeedInfo(List<FeedInfo> data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();

        String json = gson.toJson(data); //Convert the array to json

        editor.putString(PREFERENCE_FEED_LIST, json); //Put the variable in memory
        editor.apply();
    }


}
