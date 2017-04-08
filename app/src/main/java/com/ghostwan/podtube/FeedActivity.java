package com.ghostwan.podtube;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.Item;
import org.xmlpull.v1.XmlPullParserException;
import teaspoon.annotations.OnBackground;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.DataFormatException;

public class FeedActivity extends AppCompatActivity {

    public static final String RSS_URL_CHANNEL = "https://www.youtube.com/feeds/videos.xml?channel_id=";
    public static final String RSS_URL_USER = "https://www.youtube.com/feeds/videos.xml?user=";
    private static final String TAG = "FeedActivity";
    private ListView listView;
    private Context ctx;
    private List<FeedInfo> feeds;
    private FloatingActionButton fab;
    private FeedInfo currentInfo;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        listView = (ListView) findViewById(R.id.listView);
        ctx = this;
        prefManager = new PrefManager(this);

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
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        feeds.add(currentInfo);
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
        feeds = prefManager.loadFeedInfo();
    }

    @OnBackground
    protected void fetchFeed(String url) {
        String[] splits = url.split("/");
        String feedID = splits[splits.length - 1];
        String rootURL = url.contains("www.youtube.com/channel") ? RSS_URL_CHANNEL : RSS_URL_USER;
        final String feedURL = rootURL + feedID;
        InputStream inputStream = null;
        try {


            inputStream = new URL(feedURL).openConnection().getInputStream();

            /*SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed2 = input.build(new XmlReader(inputStream));
            List entries = feed2.getEntries();
            for( int i = 0; i < entries.size(); i++ ){
                Log.i(TAG, ""+((SyndEntry) entries.get(i)).getModule( MediaModule.URI ) );
            }*/

            final Feed feed = EarlParser.parseOrThrow(inputStream, 0);
            Log.i(TAG, "Processing feed: " + feed.getTitle());

            currentInfo = new FeedInfo(feed.getTitle(), url);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setTitle(feed.getTitle());
                    FeedItemAdapter feedItemAdapter = new FeedItemAdapter(FeedActivity.this, feed.getItems());
                    listView.setAdapter(feedItemAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                            Item value = (Item) adapter.getItemAtPosition(position);
                            Intent activityIntent = new Intent(FeedActivity.this, DownloadActivity.class);
                            activityIntent.putExtra(Intent.EXTRA_TEXT, value.getLink());
                            startActivity(activityIntent);
                            finish();
                        }
                    });
                    if(!feeds.contains(currentInfo))
                        fab.setVisibility(View.VISIBLE);
                }
            });


        } catch (IOException | DataFormatException | XmlPullParserException e) {
            Log.i(TAG, "error : ", e);
        } /*catch (FeedException e) {
            Log.i(TAG, "error : ", e);
        }*/
    }


    private class FeedItemAdapter extends ArrayAdapter<Item> {

        public FeedItemAdapter(Context context, List<? extends Item> items) {
            super(context, 0, (List<Item>) items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Item item = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.name);
            // Populate the data into the template view using the data object
            tvName.setText(item.getTitle());
            // Return the completed view to render on screen
            return convertView;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        prefManager.saveFeedInfo(feeds);
    }
}
