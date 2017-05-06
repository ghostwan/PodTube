package com.ghostwan.podtube;

import android.content.Context;
import android.util.Log;
import com.ghostwan.podtube.parser.Feed;
import com.ghostwan.podtube.parser.FeedParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by erwan on 28/03/2017.
 */

public class Util {

    public static final String AUDIO_TYPE = "audio";
    public static final String VIDEO_TYPE = "video";
    public static final String RSS_URL_CHANNEL = "https://www.youtube.com/feeds/videos.xml?channel_id=";
    public static final String RSS_URL_USER = "https://www.youtube.com/feeds/videos.xml?user=";
    public static final String RSS_URL_PLAYLIST = "https://www.youtube.com/feeds/videos.xml?playlist_id=";
    private static final String TAG = "Util";

    public static String getString(Context ctx, int resID, Object... data ) {
        String strMeatFormat = ctx.getString(resID);
        return String.format(strMeatFormat, data);
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

    public static boolean DEBUG = false;

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
}
