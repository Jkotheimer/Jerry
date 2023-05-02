package com.robocat.android.rc.activities;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.robocat.android.rc.MainApplication;
import com.robocat.android.rc.R;
import com.robocat.android.rc.persistence.ApplicationDatabase;
import com.robocat.android.rc.persistence.entities.RemoteDevice;
import com.robocat.android.rc.persistence.entities.RemoteDeviceDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;

public class StartupActivity extends Activity {

    private static final String TAG = StartupActivity.class.getSimpleName();

    private final long TIMEOUT_MILLIS = 10000;

    private ProgressBar mSpinner;
    private RemoteDeviceDao mDAO;
    private Disposable mDatabaseDisposable;

    private final Thread mDatabaseThread = new Thread() {
        @Override
        public void run() {
            mDAO = ApplicationDatabase.getInstance(StartupActivity.this.getApplicationContext()).remoteDeviceDao();
            mDatabaseDisposable = mDAO.getAllRemoteDevices().subscribeWith(new DisposableSingleObserver<List<RemoteDevice>>() {
                @Override
                public void onSuccess(List<RemoteDevice> remoteDevices) {
                    Log.i(TAG, "Successfully got disposable");
                    try {
                        mSpinner.incrementProgressBy(1);
                        Intent intent;
                        if (remoteDevices.isEmpty()) {
                            intent = new Intent(StartupActivity.this, DeviceSetupActivity.class);
                            intent.setAction(DeviceSetupActivity.ACTION_GETTING_STARTED);
                        } else {
                            intent = new Intent(StartupActivity.this, HomepageActivity.class);
                            intent.putParcelableArrayListExtra(
                                    HomepageActivity.EXTRA_REMOTE_DEVICES,
                                    (ArrayList<RemoteDevice>) remoteDevices
                            );
                            intent.setAction(HomepageActivity.ACTION_CONTINUE_PROGRESS);
                        }
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG,"Failed :(");
                        e.printStackTrace();
                    }
                }
                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "Got an Error!");
                    e.printStackTrace();
                    startActivity(new Intent(HomepageActivity.ACTION_GETTING_STARTED));
                }
            });
            mDatabaseDisposable.dispose();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "protected onCreate");
        init();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        this.setContentView(R.layout.spinner);

        mSpinner = (ProgressBar) findViewById(R.id.overlaySpinner);
        mSpinner.setVisibility(View.VISIBLE);
        mSpinner.setMin(0);
        mSpinner.setMax(1);

        MainApplication app = (MainApplication) getApplication();
        app.mExecutorService.execute(mDatabaseThread);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseThread.interrupt();
        mDatabaseDisposable.dispose();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabaseThread.interrupt();
        mDatabaseDisposable.dispose();
    }
}
