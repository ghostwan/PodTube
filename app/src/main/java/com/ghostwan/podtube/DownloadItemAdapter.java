package com.ghostwan.podtube;

import android.content.Context;
import android.icu.text.DecimalFormat;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ghostwan.podtube.library.dmanager.download.DownloadManager;
import com.ghostwan.podtube.library.dmanager.download.DownloadTask;
import com.ghostwan.podtube.library.dmanager.download.DownloadTaskListener;
import com.ghostwan.podtube.library.dmanager.download.TaskEntity;

import java.util.ArrayList;
import java.util.List;

import static com.ghostwan.podtube.library.dmanager.download.TaskStatus.*;

/**
 * Created by erwan on 08/04/2017.
 */

public class DownloadItemAdapter extends RecyclerView.Adapter<DownloadItemAdapter.CViewHolder> {


    private Context mContext;

    private List<DownloadTask> mListData;

    private DownloadManager mDownloadManager;

    DownloadItemAdapter(Context context) {
        mContext = context;
        mDownloadManager = DownloadManager.getInstance();
        mListData = new ArrayList<>(mDownloadManager.getmCurrentTaskList().values());
    }

    @Override
    public CViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.download_item, parent, false);
        return new CViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CViewHolder holder, final int position) {

        final DownloadTask itemTask = mListData.get(holder.getAdapterPosition());
        final TaskEntity entity = itemTask.getTaskEntity();
        holder.titleView.setText(entity.getTitle());
        holder.itemView.setTag(entity.getUrl());

        TaskEntity taskEntity = itemTask.getTaskEntity();
        int status = taskEntity.getTaskStatus();
        responseUIListener(itemTask, holder);
        String progress = getPercent(taskEntity.getCompletedSize(), taskEntity.getTotalSize());
        switch (status) {
            case TASK_STATUS_INIT:
                boolean isPause = mDownloadManager.isPauseTask(taskEntity.getTaskId());
                boolean isFinish = mDownloadManager.isFinishTask(taskEntity.getTaskId());
                holder.downloadButton.setText(isFinish ? R.string.delete : !isPause ? R.string.start : R.string.resume);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_QUEUE:
                holder.downloadButton.setText(R.string.queue);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_CONNECTING:
                holder.downloadButton.setText(R.string.connecting);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_DOWNLOADING:
                holder.downloadButton.setText(R.string.pause);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_PAUSE:
                holder.downloadButton.setText(R.string.resume);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_FINISH:
                holder.downloadButton.setText(R.string.delete);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
            case TASK_STATUS_REQUEST_ERROR:
                holder.downloadButton.setText(R.string.retry);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
            case TASK_STATUS_STORAGE_ERROR:
                holder.downloadButton.setText(R.string.retry);
                holder.progressBar.setProgress(Integer.parseInt(progress));
                holder.progressView.setText(progress);
                break;
        }


        holder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = entity.getUrl();
                String taskId = String.valueOf(url.hashCode());
                DownloadTask itemTask = mDownloadManager.getTask(taskId);

                if (itemTask == null) {
                    itemTask = new DownloadTask(new TaskEntity.Builder().url(entity.getUrl()).build());
//                    responseUIListener(itemTask, holder);
                    mDownloadManager.addTask(itemTask);
                } else {
//                    responseUIListener(itemTask, holder);
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
                            mDownloadManager.cancelTask(itemTask);
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
    }


    private void responseUIListener(@NonNull final DownloadTask itemTask, final CViewHolder holder) {

        final TaskEntity taskEntity = itemTask.getTaskEntity();

        itemTask.addListener(new DownloadTaskListener() {

            @Override
            public void onQueue(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.queue);
                }
            }

            @Override
            public void onConnecting(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.connecting);
                }
            }

            @Override
            public void onStart(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.pause);
                    holder.progressBar.setProgress(Integer.parseInt(getPercent(taskEntity.getCompletedSize(), taskEntity.getTotalSize())));
                    holder.progressView.setText(getPercent(taskEntity.getCompletedSize(), taskEntity.getTotalSize()));
                }
            }

            @Override
            public void onPause(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.resume);
                }
            }

            @Override
            public void onCancel(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.start);
                    holder.progressView.setText("0");
                    holder.progressBar.setProgress(0);
                }
            }

            @Override
            public void onFinish(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {
                    holder.downloadButton.setText(R.string.delete);
                }
            }

            @Override
            public void onError(DownloadTask downloadTask, int codeError) {
                if (holder.itemView.getTag().equals(taskEntity.getUrl())) {

                    holder.downloadButton.setText(R.string.retry);
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
        return mListData.size();
    }

    class CViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_item_title)
        TextView titleView;

        @BindView(R.id.list_item_progress_bar)
        ProgressBar progressBar;

        @BindView(R.id.list_item_progress_text)
        TextView progressView;

        @BindView(R.id.list_item_state_button)
        Button downloadButton;

        CViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
