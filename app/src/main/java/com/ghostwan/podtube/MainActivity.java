package com.ghostwan.podtube;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<FeedInfo> feeds;
    private PrefManager prefManager;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefManager = new PrefManager(this);
        listView = (ListView) findViewById(R.id.listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        feeds = prefManager.loadFeedInfo();
        FeedAdapter feedAdapter = new FeedAdapter(this, feeds);
        listView.setAdapter(feedAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                FeedInfo value = (FeedInfo) adapter.getItemAtPosition(position);
                Intent activityIntent = new Intent(MainActivity.this, FeedActivity.class);
                activityIntent.putExtra(Intent.EXTRA_TEXT, value.getUrl());
                startActivity(activityIntent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        prefManager.saveFeedInfo(feeds);
    }


    private class FeedAdapter extends ArrayAdapter<FeedInfo> {

        public FeedAdapter(Context context, List<FeedInfo> feedsInfo) {
            super(context, 0, feedsInfo );
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            FeedInfo feed = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.name);
            // Populate the data into the template view using the data object
            tvName.setText(feed != null ? feed.getName() : "None");
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
