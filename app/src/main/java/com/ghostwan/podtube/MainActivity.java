package com.ghostwan.podtube;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.ghostwan.podtube.library.us.giga.service.DownloadManagerService;
import com.ghostwan.podtube.settings.PrefManager;
import com.ghostwan.podtube.settings.SettingsActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<FeedInfo> feeds;
    private PrefManager prefManager;
    private ListView listView;
    private FloatingActionButton fab;

    private DownloadManagerService.DMBinder mBinder;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBinder = (DownloadManagerService.DMBinder) binder;
            displayDownloadList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // What to do?
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activityIntent = new Intent(MainActivity.this, DownloadingActivity.class);
                startActivity(activityIntent);
            }
        });
        fab.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Bind the service
        Intent intent = new Intent(this, DownloadManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        feeds = PrefManager.loadFeedInfo(this);
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
    protected void onResume() {
        super.onResume();
        if(mBinder != null) {
            displayDownloadList();
        }
    }

    private void displayDownloadList(){
        if( mBinder.getDownloadManager().getCount() > 0) {
            fab.setVisibility(View.VISIBLE);
        }
        else {
            fab.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        PrefManager.saveFeedInfo(this, feeds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    private class FeedAdapter extends ArrayAdapter<FeedInfo> {

        public FeedAdapter(Context context, List<FeedInfo> feedsInfo) {
            super(context, 0, feedsInfo);
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
