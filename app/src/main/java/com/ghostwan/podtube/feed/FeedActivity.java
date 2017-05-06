package com.ghostwan.podtube.feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.download.DownloadActivity;
import com.ghostwan.podtube.parser.Feed;
import com.ghostwan.podtube.parser.FeedEntry;
import com.ghostwan.podtube.parser.FeedParser;
import com.ghostwan.podtube.settings.PrefManager;
import teaspoon.annotations.OnBackground;

import java.net.URL;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    public static final String RSS_URL_CHANNEL = "https://www.youtube.com/feeds/videos.xml?channel_id=";
    public static final String RSS_URL_USER = "https://www.youtube.com/feeds/videos.xml?user=";
    public static final String RSS_URL_PLAYLIST = "https://www.youtube.com/feeds/videos.xml?playlist_id=";
    private static final String TAG = "FeedActivity";
    private ListView listView;
    private Context ctx;
    private List<FeedInfo> feeds;
    private FloatingActionButton fab;
    private FeedInfo currentInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        listView = (ListView) findViewById(R.id.listView);
        ctx = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedName = "";
                Snackbar snack = Snackbar.make(view, Util.getString(ctx, R.string.feed_add_library, feedName ), Snackbar.LENGTH_LONG);
                snack.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {

                    @Override
                    public void onShown(Snackbar transientBottomBar) {
                        super.onShown(transientBottomBar);
                        feeds.add(currentInfo);
                    }

                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        FeedActivity.this.finish();
                    }
                });
                snack.show();
            }
        });
        fab.setVisibility(View.GONE);
        String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        fetchFeed(ytLink);
    }

    @Override
    protected void onStart() {
        super.onStart();
        feeds = PrefManager.loadFeedInfo(this);
    }

    @OnBackground
    protected void fetchFeed(String url) {
        Log.i(TAG, "Url to fetch : "+url);

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
            Log.e(TAG, "This kind of url is not supported : "+url);
            Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_LONG).show();
            return;
        }

        final String feedURL = rootURL + feedID;
        try {

            final Feed feed = FeedParser.parse(new URL(feedURL));
            Log.i(TAG, "Processing feed: " + feed.title);

            currentInfo = new FeedInfo(feed.title, url);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle(feed.title);
                    FeedItemAdapter feedItemAdapter = new FeedItemAdapter(FeedActivity.this, feed.entries);
                    listView.setAdapter(feedItemAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                            FeedEntry value = (FeedEntry) adapter.getItemAtPosition(position);
                            Intent activityIntent = new Intent(FeedActivity.this, DownloadActivity.class);
                            activityIntent.putExtra(Intent.EXTRA_TEXT, value.url);
                            startActivity(activityIntent);
                            finish();
                        }
                    });
                    if(!feeds.contains(currentInfo))
                        fab.setVisibility(View.VISIBLE);
                }
            });


        }  catch (Exception e) {
            Log.i(TAG, "error : ", e);
        }
    }


    private class FeedItemAdapter extends ArrayAdapter<FeedEntry> {

        public FeedItemAdapter(Context context, List<? extends FeedEntry> items) {
            super(context, 0, (List<FeedEntry>) items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            FeedEntry item = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.name);
            // Populate the data into the template view using the data object
            tvName.setText(item.title);
            // Return the completed view to render on screen
            return convertView;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        PrefManager.saveFeedInfo(this, feeds);
    }
}
