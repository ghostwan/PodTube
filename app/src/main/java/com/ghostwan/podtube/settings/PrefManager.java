package com.ghostwan.podtube.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import com.ghostwan.podtube.FeedInfo;
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

    public static final String PREFERENCE_THREADS = "preference_threads";
    public static final String PREFERENCE_THREADS_DEFAULT = "5";
    public static final String PREFERENCE_DOWNLOAD_AUDIO_FOLDER = "preference_download_audio_folder";
    public static final String PREFERENCE_DOWNLOAD_VIDEO_FOLDER = "preference_download_video_folder";


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

    public static int getThreadCount(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPrefs.getString(PREFERENCE_THREADS, PREFERENCE_THREADS_DEFAULT);
        return Integer.parseInt(value);
    }

    public static String getAudioPath(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPrefs.getString(PREFERENCE_DOWNLOAD_AUDIO_FOLDER, getDefaultPath());
        File folder = new File(value);
        if(!folder.exists())
            folder.mkdir();
        return value;
    }
    public static String getVideoPath(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = sharedPrefs.getString(PREFERENCE_DOWNLOAD_VIDEO_FOLDER, getDefaultPath());
        File folder = new File(value);
        if(!folder.exists())
            folder.mkdir();
        return value;
    }

    public static String getDefaultPath() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/PodTube");
        if(!folder.exists())
            folder.mkdir();
        return folder.getAbsolutePath();
    }


}
