package com.ghostwan.podtube.library.dmanager.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.ghostwan.podtube.library.dmanager.db.DaoManager;
import com.ghostwan.podtube.library.dmanager.utils.FileUtils;
import com.ghostwan.podtube.library.dmanager.utils.IOUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static com.ghostwan.podtube.library.dmanager.download.TaskStatus.*;

/**
 * Created by Yuan on 27/09/2016:10:44 AM.
 * <p/>
 * Description:com.yuan.library.dmanager.download.DownloadTask
 */

public class DownloadTask implements Runnable {

    private static final String TAG = "DownloadTask";
    private OkHttpClient mClient;

    private TaskEntity mTaskEntity;

    private Map<String, DownloadTaskListener> listeners;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int code = msg.what;
            for (DownloadTaskListener listener : listeners.values()) {
                switch (code) {
                    case TASK_STATUS_QUEUE:
                        listener.onQueue(DownloadTask.this);
                        break;
                    case TASK_STATUS_CONNECTING:
                        listener.onConnecting(DownloadTask.this);
                        break;
                    case TASK_STATUS_DOWNLOADING:
                        listener.onStart(DownloadTask.this);
                        break;
                    case TASK_STATUS_PAUSE:
                        listener.onPause(DownloadTask.this);
                        break;
                    case TASK_STATUS_CANCEL:
                        listener.onCancel(DownloadTask.this);
                        break;
                    case TASK_STATUS_REQUEST_ERROR:
                        listener.onError(DownloadTask.this, TaskStatus.TASK_STATUS_REQUEST_ERROR);
                        break;
                    case TASK_STATUS_STORAGE_ERROR:
                        listener.onError(DownloadTask.this, TaskStatus.TASK_STATUS_STORAGE_ERROR);
                        break;
                    case TASK_STATUS_FINISH:
                        listener.onFinish(DownloadTask.this);
                        break;

                }
            }
        }
    };


    public DownloadTask(TaskEntity taskEntity) {
        mTaskEntity = taskEntity;
        listeners = new HashMap<>();
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        RandomAccessFile tempFile = null;

        try {


            String fileName = TextUtils.isEmpty(mTaskEntity.getFileName()) ? FileUtils.getFileNameFromUrl(mTaskEntity.getUrl()) : mTaskEntity.getFileName();
            String filePath = TextUtils.isEmpty(mTaskEntity.getFilePath()) ? FileUtils.getDefaultFilePath() : mTaskEntity.getFilePath();
            mTaskEntity.setFileName(fileName);
            mTaskEntity.setFilePath(filePath);
            tempFile = new RandomAccessFile(new File(filePath, fileName), "rwd");

            mTaskEntity.setTaskStatus(TASK_STATUS_CONNECTING);
            handler.sendEmptyMessage(TASK_STATUS_CONNECTING);

            if (DaoManager.instance().queryWidthId(mTaskEntity.getTaskId()) != null) {
                DaoManager.instance().update(mTaskEntity);
            }

            long completedSize = mTaskEntity.getCompletedSize();
            Request request;
            try {
                request = new Request.Builder().url(mTaskEntity.getUrl()).header("RANGE", "bytes=" + completedSize + "-").build();
            } catch (IllegalArgumentException e) {
                mTaskEntity.setTaskStatus(TaskStatus.TASK_STATUS_REQUEST_ERROR);
                handler.sendEmptyMessage(TaskStatus.TASK_STATUS_REQUEST_ERROR);
                Log.d("DownloadTask", e.getMessage());
                return;
            }

            if (tempFile.length() == 0) {
                completedSize = 0;
            }
            tempFile.seek(completedSize);

            Response response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    if (DaoManager.instance().queryWidthId(mTaskEntity.getTaskId()) == null) {
                        DaoManager.instance().insertOrReplace(mTaskEntity);
                        mTaskEntity.setTotalSize(responseBody.contentLength());
                    }
                    mTaskEntity.setTaskStatus(TASK_STATUS_DOWNLOADING);

                    double updateSize = mTaskEntity.getTotalSize() / 100;
                    inputStream = responseBody.byteStream();
                    bis = new BufferedInputStream(inputStream);
                    byte[] buffer = new byte[1024];
                    int length;
                    int buffOffset = 0;
                    while ((length = bis.read(buffer)) > 0 && mTaskEntity.getTaskStatus() != TaskStatus.TASK_STATUS_CANCEL && mTaskEntity.getTaskStatus() != TASK_STATUS_PAUSE) {
                        tempFile.write(buffer, 0, length);
                        completedSize += length;
                        buffOffset += length;
                        mTaskEntity.setCompletedSize(completedSize);

                        if (buffOffset >= updateSize) {
                            buffOffset = 0;
                            DaoManager.instance().update(mTaskEntity);
                            handler.sendEmptyMessage(TASK_STATUS_DOWNLOADING);
                        }

                        if (completedSize == mTaskEntity.getTotalSize()) {
                            handler.sendEmptyMessage(TASK_STATUS_DOWNLOADING);
                            mTaskEntity.setTaskStatus(TaskStatus.TASK_STATUS_FINISH);
                            handler.sendEmptyMessage(TaskStatus.TASK_STATUS_FINISH);
                            DaoManager.instance().update(mTaskEntity);
                        }
                    }
                }
            } else {
                mTaskEntity.setTaskStatus(TaskStatus.TASK_STATUS_REQUEST_ERROR);
                handler.sendEmptyMessage(TaskStatus.TASK_STATUS_REQUEST_ERROR);
            }


        } catch (FileNotFoundException e) {
            mTaskEntity.setTaskStatus(TaskStatus.TASK_STATUS_STORAGE_ERROR);
            handler.sendEmptyMessage(TaskStatus.TASK_STATUS_STORAGE_ERROR);
        } catch (SocketTimeoutException | ConnectException | SSLException | UnknownHostException e) {
            mTaskEntity.setTaskStatus(TaskStatus.TASK_STATUS_REQUEST_ERROR);
            handler.sendEmptyMessage(TaskStatus.TASK_STATUS_REQUEST_ERROR);
        } catch (IOException e) {
            Log.i(TAG, "error : ", e);
        } finally {
            IOUtils.close(bis, inputStream, tempFile);
        }
    }

    public TaskEntity getTaskEntity() {
        return mTaskEntity;
    }

    void pause() {
        mTaskEntity.setTaskStatus(TASK_STATUS_PAUSE);
        DaoManager.instance().update(mTaskEntity);
        handler.sendEmptyMessage(TASK_STATUS_PAUSE);
    }

    void queue() {
        mTaskEntity.setTaskStatus(TASK_STATUS_QUEUE);
        handler.sendEmptyMessage(TASK_STATUS_QUEUE);
    }

    void cancel() {
        mTaskEntity.setTaskStatus(TaskStatus.TASK_STATUS_CANCEL);
        DaoManager.instance().delete(mTaskEntity);
        handler.sendEmptyMessage(TaskStatus.TASK_STATUS_CANCEL);
    }

    void setClient(OkHttpClient mClient) {
        this.mClient = mClient;
    }

    public void addListener(String key, DownloadTaskListener listener) {
        listeners.put(key, listener);
    }

    public void removeListener(String key) {
        listeners.remove(key);
    }


}
