package com.ghostwan.podtube;

import android.content.Context;

/**
 * Created by erwan on 28/03/2017.
 */

public class Util {

    public static final String AUDIO_TYPE = "audio";
    public static final String VIDEO_TYPE = "video";

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

}
