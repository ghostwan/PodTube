package com.ghostwan.podtube.feed;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.parser.Feed;
import teaspoon.annotations.OnBackground;
import teaspoon.annotations.OnUi;

import java.util.List;

/**
 * Created by erwan on 07/05/2017.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.CViewHolder> {

    private static final String TAG = "FeedAdapter";
    private Context context;
    private List<FeedInfo> feeds;

    public FeedAdapter(Context context, List<FeedInfo> feeds) {
        this.context = context;
        this.feeds = feeds;
    }

    @OnBackground
    private void getFeedInfo(FeedInfo info, ImageView imageView) {
        Log.i(TAG, "Loading info for : " + info.getName());
        try {
            final Feed feed = Util.getFeedFromYoutubeUrl(info.getUrl());
            updateImageViews(feed, imageView);
        } catch (Exception e) {
            Log.i(TAG, "error : ", e);
        }
    }

    public List<FeedInfo> getFeeds() {
        return feeds;
    }

    @OnUi
    private void updateImageViews(Feed feed, ImageView imageView ) {
        if (feed.entries.get(0) != null)
            Glide.with(context)
                    .load(feed.entries.get(0).mediaMetadata.thumbnailUrl)
                    .placeholder(R.drawable.background)
                    .into(imageView);
    }

    private void removeFeed(final FeedInfo feed, View view) {
        Util.showSnack(view, R.string.feed_remove_library, feed.getName(), new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onShown(Snackbar transientBottomBar) {
                super.onShown(transientBottomBar);
                feeds.remove(feed);
                FeedAdapter.this.notifyDataSetChanged();
            }
        });
    }

    @Override
    public CViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new CViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CViewHolder holder, final int position) {

        final FeedInfo feed = feeds.get(position);

        holder.name.setText(feed != null ? feed.getName() : "None");

        getFeedInfo(feed, holder.image);

        // Return the completed view to render on screen
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.action_title);
                final CharSequence[] optionDialogActions = {
                        context.getString(R.string.delete), //0
                        context.getString(R.string.settings), //0
                };
                builder.setItems(optionDialogActions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "Choice is " + optionDialogActions[which]);
                        switch (which) {
                            case 0:
                                removeFeed(feed, v);
                                break;
                            case 1:
                                showFeedSettings(position);
                                break;
                        }
                    }
                });
                builder.show();
                return false;
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityIntent = new Intent(context, FeedActivity.class);
                activityIntent.putExtra(Util.ID, position);
                context.startActivity(activityIntent);
            }
        });
    }

    private void showFeedSettings(int feedPosition) {
        Intent intent = new Intent(context, FeedSettingsActivity.class);
        intent.putExtra(Util.ID, feedPosition);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return feeds.size();
    }


    public class CViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name)
        TextView name;

        @BindView(R.id.list_icon)
        ImageView image;

        @BindView(R.id.item_main_layout)
        RelativeLayout layout;

        public CViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
