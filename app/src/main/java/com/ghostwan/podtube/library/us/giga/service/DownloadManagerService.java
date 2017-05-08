package com.ghostwan.podtube.library.us.giga.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.download.DownloadActivity;
import com.ghostwan.podtube.download.DownloadingActivity;
import com.ghostwan.podtube.feed.FeedInfo;
import com.ghostwan.podtube.library.us.giga.get.DownloadDataSource;
import com.ghostwan.podtube.library.us.giga.get.DownloadManager;
import com.ghostwan.podtube.library.us.giga.get.DownloadManagerImpl;
import com.ghostwan.podtube.library.us.giga.get.DownloadMission;
import com.ghostwan.podtube.library.us.giga.get.sqlite.SQLiteDownloadDataSource;
import com.ghostwan.podtube.settings.PrefManager;

import java.util.ArrayList;
import java.util.List;

import static com.ghostwan.podtube.Util.DEBUG;


public class DownloadManagerService extends Service
{

	private static final String TAG = DownloadManagerService.class.getSimpleName();

	/**
	 * Message code of update messages stored as {@link Message#what}.
	 */
	private static final int UPDATE_MESSAGE = 0;
	private static final int NOTIFICATION_ID = 1000;
	private static final String EXTRA_NAME = "DownloadManagerService.extra.name";
	private static final String EXTRA_LOCATION = "DownloadManagerService.extra.location";
	private static final String EXTRA_THREADS = "DownloadManagerService.extra.threads";
	private static final String EXTRA_TYPE = "DownloadManagerService.extra.type";


	private DMBinder mBinder;
	private DownloadManager mManager;
	private Notification mNotification;
	private Handler mHandler;
	private long mLastTimeStamp = System.currentTimeMillis();
	private DownloadDataSource mDataSource;



	private MissionListener missionListener = new MissionListener();


	private void notifyMediaScanner(DownloadMission mission) {
		Uri uri = Uri.parse("file://" + mission.location + "/" + mission.name);
		// notify media scanner on downloaded media file ...
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
	}

	@Override
	public void onCreate() {
		super.onCreate();

		if (DEBUG) {
			Log.d(TAG, "onCreate");
		}

		mBinder = new DMBinder();
		if(mDataSource == null) {
			mDataSource = new SQLiteDownloadDataSource(this);
		}
		if (mManager == null) {

			ArrayList<String> paths = getMissionsPath();
			mManager = new DownloadManagerImpl(paths, mDataSource);
			if (DEBUG) {
				Log.d(TAG, "mManager == null");
				Log.d(TAG, "Download directory: " + paths);
			}
		}

		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.setClass(this, DownloadActivity.class);

		Drawable icon = this.getResources().getDrawable(R.mipmap.ic_launcher);

		Builder builder = new Builder(this)
				.setContentIntent(PendingIntent.getActivity(this, 0, i, 0))
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setLargeIcon(((BitmapDrawable) icon).getBitmap())
				.setContentTitle(getString(R.string.msg_running))
				.setContentText(getString(R.string.msg_running_detail));

		PendingIntent pendingIntent =
				PendingIntent.getActivity(this, 0, new Intent(this, DownloadingActivity.class), PendingIntent.FLAG_UPDATE_CURRENT
				);

		builder.setContentIntent(pendingIntent);

		mNotification = builder.build();

		HandlerThread thread = new HandlerThread("ServiceMessenger");
		thread.start();
			
		mHandler = new Handler(thread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case UPDATE_MESSAGE: {
						int runningCount = 0;

						for (int i = 0; i < mManager.getCount(); i++) {
							if (mManager.getMission(i).running) {
								runningCount++;
							}
						}
						updateState(runningCount);
						break;
					}
				}
			}
		};
		
	}

	private ArrayList<String> getMissionsPath() {
		ArrayList<String> paths = new ArrayList<>();
		paths.add(PrefManager.getAudioPath(this));
		String path = PrefManager.getVideoPath(this);
		if(!paths.contains(path))
			paths.add(path);
		List<FeedInfo> feeds = PrefManager.loadFeedInfo(this);
		for (FeedInfo feed : feeds) {
			if(feed.isSettingSet(FeedInfo.SETTING_FOLDER)) {
				path = feed.getSettingValue(FeedInfo.SETTING_FOLDER);
				if(!paths.contains(path))
					paths.add(path);
			}
		}
		return paths;
	}

	private void startMissionAsync(final String url, final String location, final String name,
								   final String type, final int threads) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				int missionId = mManager.startMission(url, location, name, type, threads);
				mBinder.onMissionAdded(mManager.getMission(missionId));
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (DEBUG) {
			Log.d(TAG, "Starting");
		}
		Log.i(TAG, "Got intent: " + intent);
		String action = intent.getAction();
		if(action != null && action.equals(Intent.ACTION_RUN)) {
			String name = intent.getStringExtra(EXTRA_NAME);
			String location = intent.getStringExtra(EXTRA_LOCATION);
			int threads = intent.getIntExtra(EXTRA_THREADS, 1);
			String type= intent.getStringExtra(EXTRA_TYPE);
			String url = intent.getDataString();
			startMissionAsync(url, location, name, type, threads);
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (DEBUG) {
			Log.d(TAG, "Destroying");
		}
		
		for (int i = 0; i < mManager.getCount(); i++) {
			mManager.pauseMission(i);
		}

		stopForeground(true);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private void postUpdateMessage() {
		mHandler.sendEmptyMessage(UPDATE_MESSAGE);
	}
	
	private void updateState(int runningCount) {
		if (runningCount == 0) {
			stopForeground(true);
		} else {
			startForeground(NOTIFICATION_ID, mNotification);
		}
	}

	public static void startMission(Context context, String url, String location, String name, String type, int threads) {
		Log.i(TAG, "Start mission : \n"
				+"URL :"+url + "\n"
				+"PATH: "+ location + "\n"
				+"NAME: "+ name+ "\n"
				+"TYPE: "+ type+ "\n"
				+"THREADS: "+ threads+ "\n");
		Intent intent = new Intent(context, DownloadManagerService.class);
		intent.setAction(Intent.ACTION_RUN);
		intent.setData(Uri.parse(url));
		intent.putExtra(EXTRA_NAME, name);
		intent.putExtra(EXTRA_LOCATION, location);
		intent.putExtra(EXTRA_TYPE, type);
		intent.putExtra(EXTRA_THREADS, threads);
		context.startService(intent);
	}


	class MissionListener implements DownloadMission.MissionListener {
		@Override
		public void onProgressUpdate(DownloadMission downloadMission, long done, long total) {
			long now = System.currentTimeMillis();
			long delta = now - mLastTimeStamp;
			if (delta > 2000) {
				postUpdateMessage();
				mLastTimeStamp = now;
			}
		}

		@Override
		public void onFinish(DownloadMission downloadMission) {
			postUpdateMessage();
			notifyMediaScanner(downloadMission);
		}

		@Override
		public void onError(DownloadMission downloadMission, int errCode) {
			postUpdateMessage();
		}
	}


	// Wrapper of DownloadManager
	public class DMBinder extends Binder {
		public DownloadManager getDownloadManager() {
			return mManager;
		}
		
		public void onMissionAdded(DownloadMission mission) {
			mission.addListener(missionListener);
			postUpdateMessage();
		}
		
		public void onMissionRemoved(DownloadMission mission) {
			mission.removeListener(missionListener);
			postUpdateMessage();
		}
	}
}
