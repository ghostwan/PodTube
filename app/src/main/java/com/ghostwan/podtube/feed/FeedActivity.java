package com.ghostwan.podtube.feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.download.DownloadActivity;
import com.ghostwan.podtube.parser.Feed;
import com.ghostwan.podtube.parser.FeedEntry;
import com.ghostwan.podtube.settings.PrefManager;
import teaspoon.annotations.OnBackground;

import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = "FeedActivity";
    private ListView listView;
    private Context ctx;
    private List<FeedInfo> feeds;
    private FloatingActionButton fab;
    private FeedInfo currentInfo;
    private View mainView;
    private List<String> markedAsReadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        listView = (ListView) findViewById(R.id.listView);
        ctx = this;
        mainView = getWindow().getDecorView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedName = "";
                Util.showSnack(view, R.string.feed_add_library, feedName, new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        FeedActivity.this.finish();
                    }

                    @Override
                    public void onShown(Snackbar transientBottomBar) {
                        super.onShown(transientBottomBar);
                        feeds.add(currentInfo);
                    }
                });
            }
        });
        fab.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        feeds = PrefManager.loadFeedInfo(this);
        markedAsReadList = PrefManager.loadMarkedAsReadList(this);
        int position = getIntent().getIntExtra(Util.ID, -1);
        String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if(position != -1) {
            currentInfo = feeds.get(position);
            ytLink = currentInfo.getUrl();
        }
        fetchFeed(ytLink);
    }

    @OnBackground
    protected void fetchFeed(String url) {
        Log.i(TAG, "Url to fetch : "+url);
        final Feed feed = Util.getFeedFromYoutubeUrl(url);
        if (feed == null) {
            Util.showSnack(mainView, R.string.feed_error, url, new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    FeedActivity.this.finish();
                }
            });
            return;
        }

        try {
            Log.i(TAG, "Processing feed: " + feed.title);
            String name = feed.author.name +" - "+feed.title;
            if(feed.title.contains(feed.author.name))
                name = feed.title;
            if(currentInfo == null)
                currentInfo = new FeedInfo(name, url);

            final String finalName = name;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle(finalName);
                    FeedItemAdapter feedItemAdapter = new FeedItemAdapter(FeedActivity.this, feed.entries);
                    listView.setAdapter(feedItemAdapter);
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final FeedEntry item = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
            }

            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.name);
            ImageView imageView= (ImageView) convertView.findViewById(R.id.list_icon);
            // Populate the data into the template view using the data object
            tvName.setText(item.title);
            if(markedAsReadList.contains(Util.getVideoID(item.url)))
                tvName.setAlpha(0.5f);
            else
                tvName.setAlpha(1f);
            if(item.mediaMetadata.thumbnailUrl != null)
                Glide.with(FeedActivity.this)
                        .load(item.mediaMetadata.thumbnailUrl)
                        .placeholder(R.drawable.background)
                        .into(imageView);
            // Return the completed view to render on screen
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.description_title);
                    builder.setCancelable(true);
                    builder.setMessage(item.mediaMetadata.description);
                    builder.show();
                    return false;
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent activityIntent = new Intent(FeedActivity.this, DownloadActivity.class);
                    activityIntent.putExtra(Intent.EXTRA_TEXT, item.url);
                    if(currentInfo.isSettingSet(FeedInfo.SETTING_FOLDER))
                        activityIntent.putExtra(DownloadActivity.EXTRA_PATH, currentInfo.getSettingValue(FeedInfo.SETTING_FOLDER));
                    startActivity(activityIntent);
                    finish();
                }
            });
            return convertView;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        PrefManager.saveFeedInfo(this, feeds);
    }
}
