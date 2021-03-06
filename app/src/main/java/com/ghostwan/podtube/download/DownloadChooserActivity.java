package com.ghostwan.podtube.download;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.*;
import at.huber.youtubeExtractor.YtFile;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.library.us.giga.service.PodTubeService;
import com.ghostwan.podtube.settings.PrefManager;
import teaspoon.annotations.OnBackground;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DownloadChooserActivity extends Activity{

    private static final int ITAG_FOR_AUDIO = 140;
    private static final String TAG = "DownloadChooserActivity";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final String EXTRA_PATH = "EXTRA_PATH";

    private LinearLayout mainLayout;
    private ProgressBar mainProgressBar;
    private List<YtFragmentedVideo> formatsToShowList;
    private List<String> markedAsReadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_download);
        mainLayout = findViewById(R.id.main_layout);
        mainProgressBar = findViewById(R.id.prgrBar);
        markedAsReadList = PrefManager.loadMarkedAsReadList(this);
        if (Util.checkPermissions(this, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)) {
            getYoutubeDownloadUrl();
        }
    }

    @Override
    protected void onDestroy() {
        PrefManager.saveMarkedAsReadList(this, markedAsReadList);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getYoutubeDownloadUrl();
                } else {
                    Toast.makeText(this, R.string.error_permission_denied, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void getYoutubeDownloadUrl() {
        String url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        Log.i(TAG, "Try to download : "+url);

        if(!markedAsReadList.contains(Util.getVideoID(url)))
            markedAsReadList.add(Util.getVideoID(url));

        MyYoutubeExtractor myYoutubeExtractor = new MyYoutubeExtractor(this, (ytFiles, vMeta) -> {

            mainProgressBar.setVisibility(View.GONE);
            if (ytFiles == null) {
                TextView tv = new TextView(DownloadChooserActivity.this);
                tv.setText(R.string.app_update);
                mainLayout.addView(tv);
                Button button= new Button(DownloadChooserActivity.this);
                button.setText(R.string.retry);
                button.setOnClickListener(v -> getYoutubeDownloadUrl());
                mainLayout.addView(button);
                return;
            }
            formatsToShowList = new ArrayList<>();
            for (int i = 0, itag; i < ytFiles.size(); i++) {
                itag = ytFiles.keyAt(i);
                YtFile ytFile = ytFiles.get(itag);

                if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                    addFormatToList(ytFile, ytFiles);
                }
            }
            Collections.sort(formatsToShowList, Comparator.comparingInt(lhs -> lhs.height));
            for (YtFragmentedVideo files : formatsToShowList) {
                addButtonToMainLayout(vMeta.getTitle(), files);
            }
        });

        myYoutubeExtractor.extract(url, true, false);
    }

    private void addFormatToList(YtFile ytFile, SparseArray<YtFile> ytFiles) {
        int height = ytFile.getFormat().getHeight();
        if (height != -1) {
            for (YtFragmentedVideo frVideo : formatsToShowList) {
                if (frVideo.height == height && (frVideo.videoFile == null ||
                        frVideo.videoFile.getFormat().getFps() == ytFile.getFormat().getFps())) {
                    return;
                }
            }
        }
        YtFragmentedVideo frVideo = new YtFragmentedVideo();
        frVideo.height = height;
        if (ytFile.getFormat().isDashContainer()) {
            if (height > 0) {
                frVideo.videoFile = ytFile;
                frVideo.audioFile = ytFiles.get(ITAG_FOR_AUDIO);
            } else {
                frVideo.audioFile = ytFile;
            }
        } else {
            frVideo.videoFile = ytFile;
        }
        formatsToShowList.add(frVideo);
    }


    private void addButtonToMainLayout(final String mediaTitle, final YtFragmentedVideo ytFrVideo) {
        // Display some buttons and let the user choose the format
        String btnText;

        if (ytFrVideo.height == -1)
            btnText = "Audio " + ytFrVideo.audioFile.getFormat().getAudioBitrate() + " kbit/s";
        else
            btnText = "Video " +((ytFrVideo.videoFile.getFormat().getFps() == 60) ? ytFrVideo.height + "p60" : ytFrVideo.height + "p");
        Button btn = new Button(this);
        btn.setText(btnText);
        if (ytFrVideo.videoFile != null) {
            int color = Color.parseColor("#377be8"); //The color u want
            btn.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        else {
            int color = Color.parseColor("#FF4081"); //The color u want
            btn.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        btn.setTextColor(Color.parseColor("#FFFFFF"));
        if(PrefManager.needToDisplayFileSize(this))
            addSizeToButton(btn, ytFrVideo.videoFile != null ? ytFrVideo.videoFile.getUrl() : ytFrVideo.audioFile.getUrl());
        btn.setOnClickListener(v -> {
            String filename;
            if (mediaTitle.length() > 55) {
                filename = mediaTitle.substring(0, 55);
            } else {
                filename = mediaTitle;
            }
            filename = filename.replaceAll("\\\\|>|<|\"|\\||\\*|\\?|%|:|#|/", "");
            filename += (ytFrVideo.height == -1) ? "" : "-" + ytFrVideo.height + "p";

            if (ytFrVideo.videoFile != null) {
                String mediaType = Util.VIDEO_TYPE;
                if(ytFrVideo.audioFile != null) // If audioFile is not null it means it a two files video
                    mediaType = Util.VIDEO_PART_TYPE;

                downloadFromUrl(mediaType, ytFrVideo.videoFile.getUrl(), mediaTitle,
                        filename + "." + ytFrVideo.videoFile.getFormat().getExt());
            }
            if (ytFrVideo.audioFile != null) {
                String mediaType = Util.AUDIO_TYPE;
                if(ytFrVideo.videoFile != null) // If videoFile is not null it means it a two files video
                    mediaType = Util.AUDIO_PART_TYPE;

                downloadFromUrl(mediaType, ytFrVideo.audioFile.getUrl(), mediaTitle,
                        filename + "." + ytFrVideo.audioFile.getFormat().getExt());
            }
            finish();
        });
        mainLayout.addView(btn);
    }

    @OnBackground
    private void addSizeToButton(final Button button, String textUrl) {
        try {
            URL url = new URL(textUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            final int file_size = urlConnection.getContentLength();
            runOnUiThread(() -> button.setText(button.getText()+" ( "+Util.formatBytes(file_size)+" )"));
        } catch (IOException e) {
            Log.i(TAG, "error : ", e);
        }
    }

    private void downloadFromUrl(String type, String youtubeDlUrl, String downloadTitle, String fileName) {
        String path = getIntent().getStringExtra(EXTRA_PATH);
        if(path == null) {
            if(type.equals(Util.AUDIO_TYPE)) {
                path = PrefManager.getAudioPath(this);
            }
            else {
                path = PrefManager.getVideoPath(this);
            }
        }
        PodTubeService.startMission(this, youtubeDlUrl, path, fileName, type, PrefManager.getThreadCount(this));
    }

    private class YtFragmentedVideo {
        int height;
        YtFile audioFile;
        YtFile videoFile;
    }


}
