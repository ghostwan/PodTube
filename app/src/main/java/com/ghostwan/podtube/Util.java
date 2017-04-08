package com.ghostwan.podtube;

import android.content.Context;

/**
 * Created by erwan on 28/03/2017.
 */

public class Util {

    public static String getString(Context ctx, int resID, Object... data ) {
        String strMeatFormat = ctx.getString(resID);
        return String.format(strMeatFormat, data);
    }

}
