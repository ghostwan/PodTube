package com.ghostwan.podtube.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.feed.FeedInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by erwan on 28/03/2017.
 */

public class PrefManager {

    public static String PREFERENCE_FEED_LIST = "com.ghostwan.podtube.PREFERENCE_FEED_LIST";
    public static String PREFERENCE_MARKED_AS_READ_LIST = "com.ghostwan.podtube.PREFERENCE_MARKED_AS_READ_LIST";

    public static final String PREFERENCE_THREADS = "preference_threads";
    public static final String PREFERENCE_THREADS_DEFAULT = "5";
    public static final String PREFERENCE_DOWNLOAD_AUDIO_FOLDER = "preference_download_audio_folder";
    public static final String PREFERENCE_DOWNLOAD_VIDEO_FOLDER = "preference_download_video_folder";



    public static void clearPref(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(PREFERENCE_FEED_LIST);
        editor.remove(PREFERENCE_MARKED_AS_READ_LIST);
        editor.remove(PREFERENCE_THREADS);
        editor.remove(PREFERENCE_DOWNLOAD_AUDIO_FOLDER);
        editor.remove(PREFERENCE_DOWNLOAD_VIDEO_FOLDER);
        editor.apply();
    }
    public static List<FeedInfo> loadFeedInfo(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(PREFERENCE_FEED_LIST, null); //Retrieve previously saved data
        Type type = new TypeToken<List<FeedInfo>>() {}.getType();
        if (json != null) {
            return gson.fromJson(json, type); //Restore previous data
        }
        return new ArrayList<>();
    }

    public static void saveFeedInfo(Context context, List<FeedInfo> data) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(data); //Convert the array to json
        editor.putString(PREFERENCE_FEED_LIST, json); //Put the variable in memory
        editor.apply();
    }


    public static List<String> loadMarkedAsReadList(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(PREFERENCE_MARKED_AS_READ_LIST, null); //Retrieve previously saved data
        Type type = new TypeToken<List<String>>() {}.getType();
        if (json != null) {
            return gson.fromJson(json, type); //Restore previous data
        }
        return new ArrayList<>();
    }

    public static void saveMarkedAsReadList(Context context, List<String> data) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(data); //Convert the array to json
        editor.putString(PREFERENCE_MARKED_AS_READ_LIST, json); //Put the variable in memory
        editor.apply();
    }

    public static int getThreadCount(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPrefs.getString(PREFERENCE_THREADS, PREFERENCE_THREADS_DEFAULT);
        return Integer.parseInt(value);
    }

    public static String getAudioPath(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPrefs.getString(PREFERENCE_DOWNLOAD_AUDIO_FOLDER, Util.getDefaultPath());
        File folder = new File(value);
        if(!folder.exists())
            folder.mkdir();
        return value;
    }
    public static String getVideoPath(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPrefs.getString(PREFERENCE_DOWNLOAD_VIDEO_FOLDER, Util.getDefaultPath());
        File folder = new File(value);
        if(!folder.exists())
            folder.mkdir();
        return value;
    }


}
