package com.ghostwan.podtube.feed;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.download.DownloadListActivity;
import com.ghostwan.podtube.library.us.giga.service.PodTubeService;
import com.ghostwan.podtube.settings.PrefManager;
import com.ghostwan.podtube.settings.SettingsActivity;

public class LibraryActivity extends AppCompatActivity {

    private static final String TAG = "LibraryActivity";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.waringMessage)
    TextView waringMessage;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    private PodTubeService.DMBinder mBinder;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBinder = (PodTubeService.DMBinder) binder;
            displayDownloadList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // What to do?
        }
    };
    private FeedAdapter feedAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Util.checkPermissions(this, 0);
        waringMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchYouTube = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                startActivity(launchYouTube);
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activityIntent = new Intent(LibraryActivity.this, DownloadListActivity.class);
                startActivity(activityIntent);
            }
        });
        fab.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Bind the service
        Intent intent = new Intent(this, PodTubeService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateViews(){
        if(feedAdapter.getFeeds().isEmpty()) {
            waringMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else {
            waringMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        feedAdapter = new FeedAdapter(this, PrefManager.loadFeedInfo(this));
        updateViews();
        recyclerView.setAdapter(feedAdapter);
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
        PrefManager.saveFeedInfo(this, feedAdapter.getFeeds());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

}
