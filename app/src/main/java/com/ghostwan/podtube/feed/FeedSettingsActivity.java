package com.ghostwan.podtube.feed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.settings.PrefManager;
import com.nononsenseapps.filepicker.Utils;

import java.util.List;

public class FeedSettingsActivity extends AppCompatActivity {


    public static final int REQUEST_FILE_PICKER = 0;

    @BindView(R.id.buttonClear)
    ImageButton buttonClear;

    @BindView(R.id.buttonBrowse)
    Button buttonBrowse;

    private List<FeedInfo> feeds;
    private FeedInfo feed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_settings);
        ButterKnife.bind(this);
        int position = getIntent().getIntExtra(Util.ID, -1);
        feeds = PrefManager.loadFeedInfo(this);
        if(position < 0 && position > feeds.size()-1) {
            Util.showSnack(getWindow().getDecorView(), R.string.feed_settings_error, new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    finish();
                }
            });
        }
        else {
            feed = feeds.get(position);
            setTitle(getString(R.string.settings_title)+ feed.getName());
            if(feed.isSettingSet(FeedInfo.SETTING_FOLDER)) {
                buttonBrowse.setText(feed.getSettingValue(FeedInfo.SETTING_FOLDER));
                buttonClear.setVisibility(View.VISIBLE);
            }
            else {
                buttonClear.setVisibility(View.INVISIBLE);
                buttonBrowse.setText(R.string.default_value);
            }
        }
    }

    public void onResetToDefaultPath(View view) {
        buttonClear.setVisibility(View.INVISIBLE);
        buttonBrowse.setText(R.string.default_value);
        feed.deleteSetting(FeedInfo.SETTING_FOLDER);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PrefManager.saveFeedInfo(this, feeds);
    }

    public void onBrowserFolders(View view) {
        Util.showFilePicker(this, REQUEST_FILE_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_FILE_PICKER && resultCode == Activity.RESULT_OK ) {
            String path = Utils.getFileForUri(data.getData()).getAbsolutePath();
            feed.setSettingValue(FeedInfo.SETTING_FOLDER, path);
            buttonBrowse.setText(path);
            buttonClear.setVisibility(View.VISIBLE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
