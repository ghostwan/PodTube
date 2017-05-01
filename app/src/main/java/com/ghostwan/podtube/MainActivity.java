package com.ghostwan.podtube;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.ghostwan.podtube.download.DownloadingActivity;
import com.ghostwan.podtube.feed.FeedActivity;
import com.ghostwan.podtube.feed.FeedInfo;
import com.ghostwan.podtube.library.us.giga.service.DownloadManagerService;
import com.ghostwan.podtube.settings.PrefManager;
import com.ghostwan.podtube.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<FeedInfo> feeds = new ArrayList<>();
    private ListView listView;
    private TextView waringMessage;
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
        waringMessage = (TextView) findViewById(R.id.waringMessage);
        waringMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchYouTube = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                startActivity(launchYouTube);
            }
        });
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

    private void updateViews(){
        if(feeds.isEmpty()) {
            waringMessage.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
        else {
            waringMessage.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        feeds = PrefManager.loadFeedInfo(this);
        updateViews();
        FeedAdapter feedAdapter = new FeedAdapter(this, feeds);
        listView.setAdapter(feedAdapter);
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

    //TODO extract on a file and use Recycle view instead
    private class FeedAdapter extends ArrayAdapter<FeedInfo> {

        public FeedAdapter(Context context, List<FeedInfo> feedsInfo) {
            super(context, 0, feedsInfo);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final FeedInfo feed = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.name);
            // Populate the data into the template view using the data object
            tvName.setText(feed != null ? feed.getName() : "None");
            // Return the completed view to render on screen
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.action_title);
                    final CharSequence[] optionDialogActions = {
                            getContext().getString(R.string.delete) // Option 1
                    };
                    builder.setItems(optionDialogActions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Choice is " + optionDialogActions[which]);
                            switch (which) {
                                case 0:
                                    removeFeed(feed, v);
                                    break;
                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent activityIntent = new Intent(MainActivity.this, FeedActivity.class);
                    activityIntent.putExtra(Intent.EXTRA_TEXT, feed.getUrl());
                    startActivity(activityIntent);
                }
            });
            return convertView;
        }

        private void removeFeed(final FeedInfo feed, View view) {
            Snackbar snack = Snackbar.make(view, Util.getString(getContext(), R.string.feed_remove_library, feed.getName()), Snackbar.LENGTH_LONG);
            snack.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onShown(Snackbar transientBottomBar) {
                    super.onShown(transientBottomBar);
                    feeds.remove(feed);
                    updateViews();
                    notifyDataSetInvalidated();
                }

            });
            snack.show();
        }

    }
}
