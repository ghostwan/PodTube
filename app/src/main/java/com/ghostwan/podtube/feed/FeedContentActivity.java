package com.ghostwan.podtube.feed;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.parser.Feed;
import com.ghostwan.podtube.settings.PrefManager;
import teaspoon.annotations.OnBackground;

import java.util.List;

public class FeedContentActivity extends AppCompatActivity {

    private static final String TAG = "FeedContentActivity";
    private List<FeedInfo> feeds;
    private FeedInfo currentInfo;
    private View mainView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainView = getWindow().getDecorView();

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedName = "";
                Util.showSnack(view, R.string.feed_add_library, feedName, new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        FeedContentActivity.this.finish();
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
                    FeedContentActivity.this.finish();
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
                    FeedItemAdapter feedItemAdapter = new FeedItemAdapter(FeedContentActivity.this, feed.entries, currentInfo);
                    recyclerView.setAdapter(feedItemAdapter);
                    if(!feeds.contains(currentInfo))
                        fab.setVisibility(View.VISIBLE);
                }
            });


        }  catch (Exception e) {
            Log.i(TAG, "error : ", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        PrefManager.saveFeedInfo(this, feeds);
    }
}
