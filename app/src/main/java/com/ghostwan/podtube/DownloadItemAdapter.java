package com.ghostwan.podtube;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ghostwan.podtube.library.dmanager.download.DownloadManager;
import com.ghostwan.podtube.library.dmanager.download.DownloadTask;
import com.ghostwan.podtube.library.dmanager.download.DownloadTaskListener;
import com.ghostwan.podtube.library.dmanager.download.TaskEntity;


import java.io.File;

import static com.ghostwan.podtube.library.dmanager.download.TaskStatus.*;

/**
 * Created by erwan on 08/04/2017.
 */

public class DownloadItemAdapter extends RecyclerView.Adapter<DownloadItemAdapter.CViewHolder> {


    private static final String TAG = "DownloadItemAdapter";
    private Context mContext;

    private DownloadManager mDownloadManager;
    private CharSequence dialogActions[];

    private static final int DIALOG_ACTION_DETAIL = 0;
    private static final int DIALOG_ACTION_PLAY = 1;
    private static final int DIALOG_ACTION_DELETE = 2;

    DownloadItemAdapter(Context context) {
        mContext = context;
        mDownloadManager = DownloadManager.getInstance();

        dialogActions = new CharSequence[]{
                mContext.getString(R.string.detail),
                mContext.getString(R.string.play),
                mContext.getString(R.string.delete)
        };
    }

    @Override
    public CViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.download_item, parent, false);
        return new CViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CViewHolder holder, final int position) {

        final TaskEntity taskEntity = mDownloadManager.getTaskEntities().get(holder.getAdapterPosition());
        holder.titleView.setText(taskEntity.getTitle());
        holder.itemView.setTag(taskEntity.getUrl());

//        holder.progressBar.setProgressTintList();
        if(taskEntity.getType().equals("video")) {
            int color = Color.parseColor("#377be8"); //The color u want
            holder.progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            holder.downloadButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        int status = taskEntity.getTaskStatus();
        DownloadTask itemTask = mDownloadManager.getTask(taskEntity.getTaskId());
        responseUIListener(itemTask, holder);
        String progress = getPercent(taskEntity.getCompletedSize(), taskEntity.getTotalSize());
        switch (status) {
            case TASK_STATUS_INIT:
                boolean isPause = mDownloadManager.isPauseTask(taskEntity.getTaskId());
                boolean isFinish = mDownloadManager.isFinishTask(taskEntity.getTaskId());
                holder.downloadButton.setImageResource(isFinish ? R.drawable.ic_play : !isPause ? R.drawable.ic_start: R.drawable.ic_resume);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_QUEUE:
                holder.downloadButton.setImageResource(R.drawable.ic_queue);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_CONNECTING:
                holder.downloadButton.setImageResource(R.drawable.ic_connecting);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_DOWNLOADING:
                holder.downloadButton.setImageResource(R.drawable.ic_pause);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_PAUSE:
                holder.downloadButton.setImageResource(R.drawable.ic_resume);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_FINISH:
                holder.downloadButton.setImageResource(R.drawable.ic_play);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_REQUEST_ERROR:
                holder.downloadButton.setImageResource(R.drawable.ic_retry);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
            case TASK_STATUS_STORAGE_ERROR:
                holder.downloadButton.setImageResource(R.drawable.ic_retry);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
        }


        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = taskEntity.getUrl();
                String taskId = String.valueOf(url.hashCode());
                DownloadTask itemTask = mDownloadManager.getTask(taskId);

                if (itemTask == null) {
                    itemTask = new DownloadTask(new TaskEntity.Builder().url(taskEntity.getUrl()).build());
                    responseUIListener(itemTask, holder);
                    mDownloadManager.addTask(itemTask);
                } else {
                    responseUIListener(itemTask, holder);
                    TaskEntity taskEntity = itemTask.getTaskEntity();
                    int status = taskEntity.getTaskStatus();
                    switch (status) {
                        case TASK_STATUS_QUEUE:
                            mDownloadManager.pauseTask(itemTask);
                            break;
                        case TASK_STATUS_INIT:
                            mDownloadManager.addTask(itemTask);
                            break;
                        case TASK_STATUS_CONNECTING:
                            mDownloadManager.pauseTask(itemTask);
                            break;
                        case TASK_STATUS_DOWNLOADING:
                            mDownloadManager.pauseTask(itemTask);
                            break;
                        case TASK_STATUS_CANCEL:
                            mDownloadManager.addTask(itemTask);
                            break;
                        case TASK_STATUS_PAUSE:
                            mDownloadManager.resumeTask(itemTask);
                            break;
                        case TASK_STATUS_FINISH:
                            play(itemTask);
                            break;
                        case TASK_STATUS_REQUEST_ERROR:
                            mDownloadManager.addTask(itemTask);
                            break;
                        case TASK_STATUS_STORAGE_ERROR:
                            mDownloadManager.addTask(itemTask);
                            break;
                    }
                }
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String url = taskEntity.getUrl();
                String taskId = String.valueOf(url.hashCode());
                DownloadTask itemTask = mDownloadManager.getTask(taskId);
                showDialog(itemTask);
                return true;
            }
        });
    }

    private void play(DownloadTask itemTask) {
        TaskEntity entity = itemTask.getTaskEntity();
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Log.i(TAG, "Opening : "+entity.getFilePath()+"/"+entity.getFileName());
        File file = new File(entity.getFilePath(), entity.getFileName());
        Uri uri = Uri.fromFile(file);
        String mimetype = getMimeType(uri);
        if (mimetype == null) {
            mimetype = "audio/*";
        }
        intent.setDataAndType(uri,  mimetype );
        mContext.startActivity(intent);
    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = mContext.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private void delete(DownloadTask itemTask) {
        mDownloadManager.cancelTask(itemTask);
        notifyDataSetChanged();
    }


    private void showDialog(final DownloadTask itemTask) {
        final

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.action_title);
        builder.setItems(dialogActions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "Choice is "+ dialogActions[which]);
                switch (which) {
                    case DIALOG_ACTION_DELETE : delete(itemTask);break;
                }
            }
        });
        builder.show();
    }


    private void responseUIListener(@NonNull final DownloadTask itemTask, final CViewHolder holder) {

        final TaskEntity taskEntity = itemTask.getTaskEntity();

        itemTask.addListener("ui", new DownloadTaskListener() {

            @Override
            public void onQueue(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setImageResource(R.drawable.ic_queue);
                }
            }

            @Override
            public void onConnecting(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setImageResource(R.drawable.ic_connecting);
                }
            }

            @Override
            public void onStart(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setImageResource(R.drawable.ic_pause);
                    holder.progressBar.setProgress(Integer.parseInt(getPercent(taskEntity.getCompletedSize(), taskEntity.getTotalSize())));
                    holder.progressView.setText(getPercent(taskEntity.getCompletedSize(), taskEntity.getTotalSize()));
                }
            }

            @Override
            public void onPause(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setImageResource(R.drawable.ic_resume);
                }
            }

            @Override
            public void onCancel(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setImageResource(R.drawable.ic_start);
                    holder.progressView.setText("0");
                    holder.progressBar.setProgress(0);
                }
            }

            @Override
            public void onFinish(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setImageResource(R.drawable.ic_play);
                }
            }

            @Override
            public void onError(DownloadTask downloadTask, int codeError) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {

                    holder.downloadButton.setImageResource(R.drawable.ic_retry);
                    switch (codeError) {
                        case TASK_STATUS_REQUEST_ERROR:
                            Toast.makeText(mContext, R.string.request_error, Toast.LENGTH_SHORT).show();
                            break;
                        case TASK_STATUS_STORAGE_ERROR:
                            Toast.makeText(mContext, R.string.storage_error, Toast.LENGTH_SHORT).show();
                            break;

                    }

                }
            }
        });

    }

    private String getPercent(long completed, long total) {

        if (total > 0) {
            double fen = ((double) completed / (double) total) * 100;
            DecimalFormat df1 = new DecimalFormat("0");
            return df1.format(fen);
        }
        return "0";
    }

    @Override
    public int getItemCount() {
        return mDownloadManager.getTaskEntities().size();
    }

    class CViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_item_title)
        TextView titleView;

        @BindView(R.id.list_item_progress_bar)
        ProgressBar progressBar;

        @BindView(R.id.list_item_progress_text)
        TextView progressView;

        @BindView(R.id.list_item_state_button)
        FloatingActionButton downloadButton;

        @BindView(R.id.card_view)
        CardView cardView;

        CViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }


}
