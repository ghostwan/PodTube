package com.ghostwan.podtube;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class DispatchActivity extends AppCompatActivity {

    private static final String TAG = "DispatchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {

            String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            Log.i(TAG, "Link retrieved : " + ytLink);

            if (ytLink != null
                    && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="))) {

                Intent activityIntent = new Intent(this, DownloadActivity.class);
                activityIntent.putExtra(Intent.EXTRA_TEXT, ytLink);
                startActivity(activityIntent);

            } else if (ytLink != null
                    && (ytLink.contains("www.youtube.com/channel") || ytLink.contains("www.youtube.com/user"))) {

                Intent activityIntent = new Intent(this, FeedActivity.class);
                activityIntent.putExtra(Intent.EXTRA_TEXT, ytLink);
                startActivity(activityIntent);
            } else {
                Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }
}
