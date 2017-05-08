package com.ghostwan.podtube;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import com.ghostwan.podtube.parser.Feed;
import com.ghostwan.podtube.parser.FeedParser;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by erwan on 28/03/2017.
 */

public class Util {

    private static final String TAG = "Util";
    public static boolean DEBUG = true;
    public static final String ID = "ID";
    public static final String AUDIO_TYPE = "audio";
    public static final String VIDEO_TYPE = "video";
    public static final String RSS_URL_CHANNEL = "https://www.youtube.com/feeds/videos.xml?channel_id=";
    public static final String RSS_URL_USER = "https://www.youtube.com/feeds/videos.xml?user=";
    public static final String RSS_URL_PLAYLIST = "https://www.youtube.com/feeds/videos.xml?playlist_id=";

    public static String getString(Context ctx, int resID, Object... data ) {
        String strMeatFormat = ctx.getString(resID);
        return String.format(strMeatFormat, data);
    }

    public static String getString(Context ctx, int resID) {
        return ctx.getString(resID);
    }

    public static boolean isAudio(String text){
        return text.equals(AUDIO_TYPE);
    }

    public static boolean isVideo(String text){
        return text.equals(VIDEO_TYPE);
    }

    public static Feed getFeedFromYoutubeUrl(String url) {
        String[] splits = url.split("/");
        String feedID = splits[splits.length - 1];

        String rootURL = null;
        if(url.contains("channel"))
            rootURL = RSS_URL_CHANNEL;
        else if(url.contains("user"))
            rootURL = RSS_URL_USER;
        else if(url.contains("playlist")) {
            feedID = feedID.replace("playlist?list=", "");
            rootURL = RSS_URL_PLAYLIST;
        }

        Log.i(TAG, "FeedID : "+feedID);
        if (rootURL == null) {
            return null;
        }

        final String feedURL = rootURL + feedID;
        try {
            Log.i(TAG, "Feed url xml  to fetch : "+feedURL);
            return  FeedParser.parse(feedURL);
        } catch (Exception e) {
            Log.i(TAG, "error : ", e);
        }
        return null;
    }

    public static String getVideoID(String url) {
        url = url.replace("https://youtu.be/", "");
        url = url.replace("https://www.youtube.com/watch?v=", "");
        return url;
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return String.format("%d B", bytes);
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f kB", (float) bytes / 1024);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", (float) bytes / 1024 / 1024);
        } else {
            return String.format("%.2f GB", (float) bytes / 1024 / 1024 / 1024);
        }
    }

    public static String formatSpeed(float speed) {
        if (speed < 1024) {
            return String.format("%.2f B/s", speed);
        } else if (speed < 1024 * 1024) {
            return String.format("%.2f kB/s", speed / 1024);
        } else if (speed < 1024 * 1024 * 1024) {
            return String.format("%.2f MB/s", speed / 1024 / 1024);
        } else {
            return String.format("%.2f GB/s", speed / 1024 / 1024 / 1024);
        }
    }

    public static void writeToFile(String fileName, String content) {
        try {
            writeToFile(fileName, content.getBytes("UTF-8"));
        } catch (Exception e) {

        }
    }

    public static void writeToFile(String fileName, byte[] content) {
        File f = new File(fileName);

        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {

            }
        }

        try {
            FileOutputStream opt = new FileOutputStream(f, false);
            opt.write(content, 0, content.length);
            opt.close();
        } catch (Exception e) {

        }
    }

    public static String readFromFile(String file) {
        try {
            File f = new File(file);

            if (!f.exists() || !f.canRead()) {
                return null;
            }

            BufferedInputStream ipt = new BufferedInputStream(new FileInputStream(f));

            byte[] buf = new byte[512];
            StringBuilder sb = new StringBuilder();

            while (ipt.available() > 0) {
                int len = ipt.read(buf, 0, 512);
                sb.append(new String(buf, 0, len, "UTF-8"));
            }

            ipt.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
    public static boolean checkPermissions(final Activity activity, final int requestCode) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle(R.string.permission_required_title);
                    alertBuilder.setMessage(R.string.permission_required_description);
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static void showFilePicker(Activity activity, int requestID, String rootPath) {
        Intent i = new Intent(activity, FilePickerActivity.class)
                .putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

        if(rootPath != null)
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, rootPath);
        activity.startActivityForResult(i, requestID);
    }

    public static void showFilePicker(Activity activity, int requestID) {
        showFilePicker(activity, requestID, getDefaultPath());
    }

    public static void showSnack(View view, String text, BaseTransientBottomBar.BaseCallback<Snackbar> baseCallback) {
        Snackbar snack = Snackbar.make(view, text , Snackbar.LENGTH_SHORT);
        if(baseCallback != null)
            snack.addCallback(baseCallback);
        snack.show();
    }

    public static void showSnack(View view, int resID, BaseTransientBottomBar.BaseCallback<Snackbar> baseCallback) {
        String strMeatFormat = view.getResources().getString(resID);
        showSnack(view, strMeatFormat, baseCallback);
    }

    public static void showSnack(View view, int resID, Object data, BaseTransientBottomBar.BaseCallback<Snackbar> baseCallback) {
        String strMeatFormat = view.getResources().getString(resID);
        showSnack(view, String.format(strMeatFormat, data), baseCallback);
    }

    public static String getDefaultPath() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/PodTube");
        if(!folder.exists())
            folder.mkdir();
        return folder.getAbsolutePath();
    }
}
