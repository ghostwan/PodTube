package com.ghostwan.podtube;

import android.content.Context;
import android.util.Log;
import com.ghostwan.podtube.parser.Feed;
import com.ghostwan.podtube.parser.FeedParser;

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
}
