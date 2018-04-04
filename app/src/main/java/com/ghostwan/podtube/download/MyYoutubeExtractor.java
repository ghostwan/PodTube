package com.ghostwan.podtube.download;

import android.content.Context;
import android.util.SparseArray;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

/**
 * Created by erwan on 18/11/2017.
 */

public class MyYoutubeExtractor extends YouTubeExtractor {

    private final Callback callback;

    public MyYoutubeExtractor(Context con, Callback callback) {
        super(con);
        this.callback = callback;
    }

    @Override
    public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
        callback.onExtractionComplete(ytFiles, vMeta);
    }

    public interface Callback {

        void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta);
    }
}
