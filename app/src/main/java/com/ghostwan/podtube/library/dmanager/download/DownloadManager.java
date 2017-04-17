package com.ghostwan.podtube.library.dmanager.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.ghostwan.podtube.BuildConfig;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.library.dmanager.db.DaoManager;
import com.ghostwan.podtube.library.dmanager.utils.Constants;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.ghostwan.podtube.library.dmanager.download.TaskStatus.TASK_STATUS_FINISH;
import static com.ghostwan.podtube.library.dmanager.download.TaskStatus.TASK_STATUS_INIT;

public class DownloadManager {

    // manager instance
    private static DownloadManager mInstance;

    // quess
    private LinkedBlockingQueue<Runnable> mQueue;

    // ok http
    private OkHttpClient mClient;

    // ThreadPoolExecutor
    private ThreadPoolExecutor mExecutor;

    // the thread count
    private int mThreadCount = 1;

    // task list
    private Map<String, DownloadTask> mCurrentTaskList;

    // greenDao seesion
    private DaoSession mDaoSession;

    private Context ctx;
    private NotificationManager notificationManager;

    private DownloadManager() {

    }

    public static synchronized DownloadManager getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadManager();
        }
        return mInstance;
    }

    public boolean isTasks() {
        return !DaoManager.instance().queryAll().isEmpty();
    }

    /**
     * @param context Application
     */
    public void init(@NonNull Context context) {
        init(context, getAppropriateThreadCount());
    }

    /**
     * @param context     Application
     * @param threadCount the max download count
     */
    public void init(@NonNull Context context, int threadCount) {
        init(context, threadCount, getOkHttpClient());
    }

    /**
     * @param context     Application
     * @param threadCount the max download count
     * @param client      okhttp client
     */

    public void init(@NonNull Context context, int threadCount, @NonNull OkHttpClient client) {
        setupDatabase(context);

        recoveryTaskState();
        ctx = context;
        notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mClient = client;
        mThreadCount = threadCount < 1 ? 1 : threadCount <= Constants.MAX_THREAD_COUNT ? threadCount : Constants.MAX_THREAD_COUNT;
        mExecutor = new ThreadPoolExecutor(mThreadCount, mThreadCount, 20, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        mCurrentTaskList = new HashMap<>();
        mQueue = (LinkedBlockingQueue<Runnable>) mExecutor.getQueue();


    }

    private void setupDatabase(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "download.db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);
        mDaoSession = master.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }


    /**
     * generate default client
     */
    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
    }


    /**
     * @return generate the appropriate thread count.
     */
    private int getAppropriateThreadCount() {
        return Runtime.getRuntime().availableProcessors() * 2 + 1;
    }

    /**
     * add task
     */
    public void addTask(@NonNull DownloadTask task) {

        TaskEntity taskEntity = task.getTaskEntity();

        if (taskEntity != null && taskEntity.getTaskStatus() != TaskStatus.TASK_STATUS_DOWNLOADING) {
            task.setClient(mClient);
            task.addListener("manager", new DownloadTaskListener() {
                @Override
                public void onQueue(DownloadTask downloadTask) {
                    displayNotification(R.string.queue_download, downloadTask);
                }

                @Override
                public void onConnecting(DownloadTask downloadTask) {
                    displayNotification(R.string.start_download, downloadTask);
                }

                @Override
                public void onStart(DownloadTask downloadTask) {
                }

                @Override
                public void onPause(DownloadTask downloadTask) {

                }

                @Override
                public void onCancel(DownloadTask downloadTask) {

                }

                @Override
                public void onFinish(DownloadTask downloadTask) {
                    displayNotification(R.string.download_finished, downloadTask);
                }

                @Override
                public void onError(DownloadTask downloadTask, int code) {
                    displayNotification(R.string.error_download, downloadTask);
                }
            });
            mCurrentTaskList.put(taskEntity.getTaskId(), task);
            if (!mQueue.contains(task)) {
                mExecutor.execute(task);
            }

            if (mExecutor.getTaskCount() > mThreadCount) {
                task.queue();
            }
        }
    }

    private void displayNotification (int title, DownloadTask downloadTask) {
        // Build the notification and add the action.
        Notification newMessageNotification =
                new Notification.Builder(ctx)
                        .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                        .setContentTitle(ctx.getString(title))
                        .setContentText(downloadTask.getTaskEntity().getTitle())
                        .build();
        int id = downloadTask.getTaskEntity().getHashCode();
        notificationManager.notify(id, newMessageNotification);
    }

    public List<TaskEntity> getTaskEntities() {
        return DaoManager.instance().queryAll();
    }

    /**
     * pauseTask task
     */
    public void pauseTask(@NonNull DownloadTask task) {
        if (mQueue.contains(task)) {
            mQueue.remove(task);
        }
        task.pause();
    }

    /**
     * resumeTask task
     */
    public void resumeTask(@NonNull DownloadTask task) {
        addTask(task);
    }


    public void resetTask(DownloadTask task) {
        if(task == null) return;
        TaskEntity taskEntity = task.getTaskEntity();
        if (taskEntity != null) {
            File file = new File(taskEntity.getFilePath(), taskEntity.getFilePath());
            if (file.exists())
                file.delete();
            taskEntity.setCompletedSize(0);
            taskEntity.setTaskStatus(TASK_STATUS_INIT);
        }
    }

    /**
     * cancel task
     */
    public void cancelTask(DownloadTask task) {
        if(task == null) return;
        TaskEntity taskEntity = task.getTaskEntity();
        if (taskEntity != null) {
            if(task.getTaskEntity().getTaskStatus() == TaskStatus.TASK_STATUS_DOWNLOADING){
                pauseTask(task);
                mExecutor.remove(task);
            }

            if (mQueue.contains(task)) {
                mQueue.remove(task);
            }
            mCurrentTaskList.remove(taskEntity.getTaskId());
            task.cancel();
            if (!TextUtils.isEmpty(taskEntity.getFilePath()) && !TextUtils.isEmpty(taskEntity.getFileName())) {
                File temp = new File(taskEntity.getFilePath(), taskEntity.getFileName());
                if (temp.exists()) {
                    if (temp.delete()) {
                        if (BuildConfig.DEBUG) Log.d("DownloadManager", "delete file : "+taskEntity.getFileName());
                    }
                }
            }
        }
    }

    /**
     * @return task
     */
    public DownloadTask getTask(String id) {
        DownloadTask currTask = mCurrentTaskList.get(id);
        if (currTask == null) {
            TaskEntity entity = DaoManager.instance().queryWidthId(id);
            if (entity != null) {
                int status = entity.getTaskStatus();
                currTask = new DownloadTask(entity);
                if (status != TaskStatus.TASK_STATUS_FINISH) {
                    mCurrentTaskList.put(id, currTask);
                }
            }
        }
        return currTask;
    }


    public boolean isPauseTask(String id) {
        TaskEntity entity = DaoManager.instance().queryWidthId(id);
        if (entity != null) {
            File file = new File(entity.getFilePath(), entity.getFilePath());
            if (file.exists()) {
                long totalSize = entity.getTotalSize();
                return totalSize > 0 && file.length() < totalSize;
            }
        }
        return false;
    }

    public boolean isFinishTask(String id) {
        TaskEntity entity = DaoManager.instance().queryWidthId(id);
        if (entity != null) {
            File file = new File(entity.getFilePath(), entity.getFileName());
            if (file.exists()) {
                return file.length() == entity.getTotalSize();
            }
        }
        return false;
    }

    public boolean isFileError(String id) {
        TaskEntity entity = DaoManager.instance().queryWidthId(id);
        if (entity != null) {
            File file = new File(entity.getFilePath(), entity.getFileName());
            if(entity.getTaskStatus() == TASK_STATUS_FINISH && !file.exists()) {
                return true;
            }
        }
        return false;
    }

    private void recoveryTaskState() {
        List<TaskEntity> entities = DaoManager.instance().queryAll();
        for (TaskEntity entity : entities) {
            long completedSize = entity.getCompletedSize();
            long totalSize = entity.getTotalSize();
            if (completedSize > 0 && completedSize != totalSize && entity.getTaskStatus() != TaskStatus.TASK_STATUS_PAUSE) {
                entity.setTaskStatus(TaskStatus.TASK_STATUS_PAUSE);
            }
            DaoManager.instance().update(entity);
        }
    }

}