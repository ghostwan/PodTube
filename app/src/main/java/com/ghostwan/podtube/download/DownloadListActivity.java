package com.ghostwan.podtube.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ghostwan.podtube.R;
import com.ghostwan.podtube.library.us.giga.service.PodTubeService;

import java.io.File;

public class DownloadListActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private PodTubeService.DMBinder mBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloading);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Bind the service
        Intent intent = new Intent(this, PodTubeService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mBinder = (PodTubeService.DMBinder) binder;
            updateList(mBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void updateList(PodTubeService.DMBinder mBinder) {
        DownloadItemAdapter adapter = new DownloadItemAdapter(this, mBinder);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mBinder != null)
            updateList(mBinder);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_goto_directory) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            File folder = new File(Environment.getExternalStorageDirectory() + "/PodTube/");
            intent.setDataAndType(Uri.fromFile(folder), "*/*");
            startActivity(Intent.createChooser(intent, "Open Folder"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
}
