package com.ghostwan.podtube.download;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.Util;
import com.ghostwan.podtube.library.us.giga.get.DownloadManager;
import com.ghostwan.podtube.library.us.giga.get.DownloadMission;
import com.ghostwan.podtube.library.us.giga.service.PodTubeService;
import teaspoon.annotations.OnBackground;
import teaspoon.annotations.OnUi;

import java.io.File;
import java.io.IOException;

import static com.ghostwan.podtube.download.TaskStatus.*;

/**
 * Created by erwan on 08/04/2017.
 */

public class DownloadItemAdapter extends RecyclerView.Adapter<DownloadItemAdapter.CViewHolder> {


    private static final String TAG = "DownloadItemAdapter";
    private final DownloadManager mDownloadManager;
    private Context mContext;
    private PodTubeService.DMBinder mBinder;
    private DecimalFormat df1 = new DecimalFormat("0");

    DownloadItemAdapter(Context context, PodTubeService.DMBinder binder) {
        mContext = context;
        mBinder = binder;
        mDownloadManager = mBinder.getDownloadManager();
    }

    @Override
    public CViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.download_item, parent, false);
        return new CViewHolder(itemView);
    }

    @Override
    public void onViewRecycled(CViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mission.removeListener(holder.observer);
        holder.mission = null;
        holder.observer = null;
        holder.position = -1;
        holder.lastTimeStamp = -1;
        holder.lastDone = -1;
        holder.colorId = 0;
    }

    @Override
    public void onBindViewHolder(final CViewHolder holder, final int position) {

        DownloadMission mission = mDownloadManager.getMission(position);
        holder.initMission(mContext, this, mission);
        holder.titleView.setText(holder.mission.name);
        holder.itemView.setTag(holder.mission.url);

        if (holder.mission.type.equals(Util.AUDIO_TYPE)) {
            int color = Color.parseColor("#FF4081");
            holder.progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            holder.downloadButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        else if(holder.mission.type.equals(Util.VIDEO_TYPE)) {
            int color = Color.parseColor("#377be8");
            holder.progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            holder.downloadButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        else if(holder.mission.type.equals(Util.VIDEO_PART_TYPE)) {
            int color = Color.parseColor("#fc9e1b");
            holder.progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            holder.downloadButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        else if(holder.mission.type.equals(Util.AUDIO_PART_TYPE)) {
            int color = Color.parseColor("#d68617");
            holder.progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            holder.downloadButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        else {
            holder.progressBar.getProgressDrawable().clearColorFilter();
            holder.downloadButton.getBackground().clearColorFilter();
        }

        int status = holder.mission.getStatus();

        switch (status) {
            case TASK_STATUS_INIT:
                holder.setImage(R.drawable.ic_start);
                break;
            case TASK_STATUS_DOWNLOADING:
                holder.setImage(R.drawable.ic_pause);
                break;
            case TASK_STATUS_PAUSE:
                holder.setImage(R.drawable.ic_resume);
                break;
            case TASK_STATUS_FINISH:
                if(holder.mission.type.equals(Util.VIDEO_PART_TYPE)) {
                    if(isAudioPartDone(holder.mission))
                        holder.setImage(R.drawable.ic_connecting);
                    else
                        holder.setImage(R.drawable.ic_queue);
                }
                else {
                    holder.setImage(R.drawable.ic_play);
                }
                break;
            case TASK_STATUS_MERGING:
                holder.setAnimatedDrawable(R.drawable.merging);
                break;
            case TASK_STATUS_REQUEST_ERROR:
            case TASK_STATUS_STORAGE_ERROR:
                holder.setImage(R.drawable.ic_error);
                break;

        }

        holder.downloadButton.setOnClickListener(v -> {

            try {
                switch (holder.getResource()) {
                    case R.drawable.ic_start:
                    case R.drawable.ic_resume:
                        resume(holder);
                        break;
                    case R.drawable.ic_pause:
                        pause(holder);
                        break;
                    case R.drawable.ic_play:
                        play(holder.mission);
                        break;
                    case R.drawable.ic_error:
                        showErrorDialog(holder);
                        break;
                    case R.drawable.ic_connecting:
                        mergeMp4(holder);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "error :", e);
            }
        });

        holder.cardView.setOnLongClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            if(!holder.mission.isFinished)
                pause(holder);
            showOptionDialog(holder.mission);
            return true;
        });

        holder.progressLayout.setOnClickListener(v -> {
            if(holder.progressBar.getVisibility() == View.VISIBLE) {
                holder.progressBar.setVisibility(View.GONE);
                holder.progressView.setVisibility(View.GONE);
                holder.speedView.setVisibility(View.GONE);
                holder.sizeText.setVisibility(View.VISIBLE);
            }
            else if (holder.sizeText.getVisibility() == View.VISIBLE){
                holder.progressBar.setVisibility(View.GONE);
                holder.progressView.setVisibility(View.GONE);
                holder.speedView.setVisibility(View.VISIBLE);
                holder.sizeText.setVisibility(View.GONE);
            }
            else if(holder.speedView.getVisibility() == View.VISIBLE) {
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressView.setVisibility(View.VISIBLE);
                holder.sizeText.setVisibility(View.GONE);
                holder.speedView.setVisibility(View.GONE);
            }
            updateProgress(holder);
        });

        updateProgress(holder);
    }

    private boolean isAudioPartDone(DownloadMission mission) {
        return new File(mission.getFileTokens()[0]+".m4a").exists() &&
                ! new File(mission.getFileTokens()[0]+".m4a.giga").exists();
    }


    private void updateProgress(CViewHolder holder) {
        updateProgress(holder, false);
    }

    private void updateProgress(CViewHolder h, boolean finished) {
        if (h.mission == null)
            return;

        long now = System.currentTimeMillis();

        if (h.lastTimeStamp == -1) {
            h.lastTimeStamp = now;
        }


        long deltaTime = now - h.lastTimeStamp;

        if (deltaTime == 0 || deltaTime > 1000) {
            h.lastTimeStamp = now;

            long deltaDone = h.mission.done - h.lastDone;
            if (h.lastDone == -1) {
                h.lastDone = h.mission.done;
            }
            h.lastDone = h.mission.done;

            if (h.mission.errCode > 0) {
                    h.progressView.setText(R.string.display_error);
            } else {
                String percent = getPercent(h.mission.done, h.mission.length);
                h.progressBar.setProgress(Integer.parseInt(percent));
                h.progressView.setText(percent);
            }

            float speed = (float) deltaDone / deltaTime;
            String speedStr = Util.formatSpeed(speed * 1000);

            h.speedView.setText(speedStr);

            if(h.mission.isFinished)
                h.sizeText.setText(Util.getString(mContext, R.string.done,  Util.formatBytes(h.mission.length)));
            else
                h.sizeText.setText(Util.formatBytes(h.mission.done)+" / "+Util.formatBytes(h.mission.length));
        }
        else {
            if(h.mission.isFinished) {
                h.speedView.setText(R.string.none);
                h.sizeText.setText(Util.getString(mContext, R.string.done,  Util.formatBytes(h.mission.length)));
            }
            else if(!h.mission.isRunning) {
                h.speedView.setText(R.string.none);
                h.sizeText.setText(Util.formatBytes(h.mission.done)+" / "+Util.formatBytes(h.mission.length));
            }
        }
    }

    private void pause(CViewHolder holder) {
        holder.setImage(R.drawable.ic_resume);
        mDownloadManager.pauseMission(holder.mission);
        mBinder.onMissionRemoved(holder.mission);
    }

    private void resume(CViewHolder holder) {
        holder.setImage(R.drawable.ic_pause);
        mDownloadManager.resumeMission(holder.mission);
        mBinder.onMissionAdded(holder.mission);
    }

    private void play(DownloadMission mission) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Log.i(TAG, "Opening : " + mission.getDownloadedFile().getPath());
        Uri uri = Uri.fromFile(mission.getDownloadedFile());
        String mimetype = getMimeType(uri);
        if (mimetype == null) {
            mimetype = "audio/*";
        }
        intent.setDataAndType(uri, mimetype);
        mContext.startActivity(intent);
    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = mContext.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private void delete(DownloadMission mission) {
        if(mission.isRunning)
            Toast.makeText(mContext, R.string.delete_error, Toast.LENGTH_SHORT).show();
        else {
            mDownloadManager.deleteMission(mission);
            notifyDataSetChanged();
        }
    }


    private void showOptionDialog(final DownloadMission mission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.action_title);
        final CharSequence[] optionDialogActions = {
                mContext.getString(R.string.play), // Option 0
                mContext.getString(R.string.delete) // Option 1
        };
        builder.setItems(optionDialogActions, (dialog, which) -> {
            Log.i(TAG, "Choice is " + optionDialogActions[which]);
            switch (which) {
                case 0:
                    play(mission);
                    break;
                case 1:
                    delete(mission);
                    break;
            }
        });
        builder.show();
    }




    @OnBackground
    private void mergeMp4(CViewHolder holder) {
        holder.setAnimatedDrawable(R.drawable.merging);
        notifyUI(holder.progressLayout,"Merging "+holder.mission.name + " ...");

        try {
            mDownloadManager.mergeMission(holder.mission);
            holder.setImage(R.drawable.ic_play);
            notifyUI(holder.progressLayout, "Merged completed for: "+holder.mission.name);
        } catch (Exception e) {
            Log.e(TAG, "error :", e);
            holder.setImage(R.drawable.ic_error);
            notifyUI(holder.progressLayout, "Merged failed !! for: "+holder.mission.name);
        }

        mDownloadManager.loadMissions();
        notifyDataSetChanged();
    }

    @OnUi
    private void notifyUI(View view, String message) {
        Util.showSnack(view, message, null);
    }

    private void showErrorDialog(final CViewHolder itemTask) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.action_title);
        final CharSequence[] optionDialogActions = {
                mContext.getString(R.string.retry), // Option 0
                mContext.getString(R.string.delete) // Option 1
        };
        builder.setItems(optionDialogActions, (dialog, which) -> {
            Log.i(TAG, "Choice is " + optionDialogActions[which]);
            switch (which) {
                case 0:
                    resume(itemTask);
                    break;
                case 1:
                    delete(itemTask.mission);
                    break;
            }
        });
        builder.show();
    }


    private String getPercent(long completed, long total) {

        if (total > 0) {
            double fen = ((double) completed / (double) total) * 100;
            return df1.format(fen);
        }
        return "0";
    }

    @Override
    public int getItemCount() {
        return mDownloadManager.getCount();
    }

    class CViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_item_title)
        TextView titleView;

        @BindView(R.id.list_item_progress_bar)
        ProgressBar progressBar;

        @BindView(R.id.list_item_progress_text)
        TextView progressView;

        @BindView(R.id.speed_text)
        TextView speedView;

        @BindView(R.id.size_text)
        TextView sizeText;

        @BindView(R.id.list_item_state_button)
        FloatingActionButton downloadButton;

        @BindView(R.id.card_view)
        CardView cardView;

        @BindView(R.id.progress_layout)
        LinearLayout progressLayout;

        int resource;
        public MissionObserver observer;
        public DownloadMission mission;
        public int position;
        public long lastTimeStamp = -1;
        public long lastDone = -1;
        public int colorId;

        CViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnUi
        public void setImage(int res) {
            resource = res;
            downloadButton.setImageResource(res);
        }

        @OnUi
        public void setAnimatedDrawable(int res) {
            resource = res;
            AnimatedVectorDrawableCompat drawable = AnimatedVectorDrawableCompat.create(mContext, res);
            downloadButton.setImageDrawable(drawable);
            drawable.start();
        }

        public void initMission(Context ctx, DownloadItemAdapter adapter, DownloadMission downloadMission) {
            mission = downloadMission;
            observer = new MissionObserver(adapter, this, ctx);
            mission.addListener(observer);
        }

        public int getResource() {
            return resource;
        }
    }


    static class MissionObserver implements DownloadMission.MissionListener {
        private final Context mContext;
        private DownloadItemAdapter mAdapter;
        private CViewHolder mHolder;

        public MissionObserver(DownloadItemAdapter adapter, CViewHolder holder, Context context) {
            mAdapter = adapter;
            mHolder = holder;
            mContext = context;
        }

        @Override
        public void onProgressUpdate(DownloadMission downloadMission, long done, long total) {
            mAdapter.updateProgress(mHolder);
        }

        @Override
        public void onFinish(DownloadMission downloadMission) {
            // TODO Notification
            mAdapter.notifyDataSetChanged();
            if (mHolder.mission != null) {
//                mHolder.size.setText(Utility.formatBytes(mHolder.mission.length));
                mAdapter.updateProgress(mHolder, true);
            }
        }

        @Override
        public void onError(DownloadMission downloadMission, int errCode) {
            mAdapter.updateProgress(mHolder);
            if (mHolder.itemView.getTag().equals(downloadMission.url)) {
                mHolder.setImage(R.drawable.ic_error);
                Util.showSnack(mHolder.cardView, R.string.request_error, null);
            }
        }

    }




}
