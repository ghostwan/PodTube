package com.ghostwan.podtube.feed;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.download.DownloadActivity;
import com.ghostwan.podtube.parser.FeedEntry;
import com.ghostwan.podtube.settings.PrefManager;

import java.util.List;

/**
 * Created by erwan on 07/05/2017.
 */

public class FeedItemAdapter extends RecyclerView.Adapter<CViewHolder> {

    private static final String TAG = "FeedItemAdapter";
    private final Context context;
    private final List<FeedEntry> entries;
    private final List<String> markedAsReadList;
    private final FeedInfo feedInfo;

    public FeedItemAdapter(Context context, List<FeedEntry> entries, FeedInfo feedInfo){
        this.context = context;
        this.entries = entries;
        this.feedInfo = feedInfo;
        markedAsReadList = PrefManager.loadMarkedAsReadList(context);
    }

    @Override
    public CViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new CViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CViewHolder holder, int position) {
        final FeedEntry item = entries.get(position);

        holder.name.setText(item.title);

        if(markedAsReadList.contains(Util.getVideoID(item.url)))
            holder.name.setAlpha(0.4f);
        else
            holder.name.setAlpha(1f);
        if(item.mediaMetadata.thumbnailUrl != null)
            Glide.with(context)
                    .load(item.mediaMetadata.thumbnailUrl)
                    .placeholder(R.drawable.background)
                    .into(holder.image);
        // Return the completed view to render on screen
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.description_title);
                builder.setCancelable(true);
                builder.setMessage(item.mediaMetadata.description);
                builder.show();
                return false;
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityIntent = new Intent(context, DownloadActivity.class);
                activityIntent.putExtra(Intent.EXTRA_TEXT, item.url);
                if(feedInfo.isSettingSet(FeedInfo.SETTING_FOLDER))
                    activityIntent.putExtra(DownloadActivity.EXTRA_PATH, feedInfo.getSettingValue(FeedInfo.SETTING_FOLDER));
                context.startActivity(activityIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

}
