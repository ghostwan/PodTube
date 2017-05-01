package com.ghostwan.podtube;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.ghostwan.podtube.download.DownloadActivity;
import com.ghostwan.podtube.feed.FeedActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchActivity extends AppCompatActivity {

    private static final String TAG = "DispatchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {

            String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);

            List<String> urls = extractUrls(ytLink);
            if(urls.isEmpty()) {
                Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            ytLink = urls.get(0);

            Log.i(TAG, "Link retrieved : " + ytLink);

            if (ytLink != null
                    && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="))) {

                Intent activityIntent = new Intent(this, DownloadActivity.class);
                activityIntent.putExtra(Intent.EXTRA_TEXT, ytLink);
                startActivity(activityIntent);

            } else if (ytLink != null
                    && (ytLink.contains("www.youtube.com/channel") ||
                    ytLink.contains("www.youtube.com/user") ||
                    ytLink.contains("www.youtube.com/playlist")
            )) {
                Intent activityIntent = new Intent(this, FeedActivity.class);
                activityIntent.putExtra(Intent.EXTRA_TEXT, ytLink);
                startActivity(activityIntent);
            } else {
                Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }
}
