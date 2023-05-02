package com.robocat.android.rc.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.robocat.android.rc.base.ui.Modal;
import com.robocat.android.rc.persistence.entities.RemoteDevice;

import java.util.List;

public class HomepageActivity extends FragmentActivity {

    public static final String ACTION_GETTING_STARTED = "GETTING_STARTED";
    public static final String ACTION_CONTINUE_PROGRESS = "CONTINUE_PROGRESS";
    public static final String ACTION_ERROR_STATE = "ERROR_STATE";

    public static final String EXTRA_REMOTE_DEVICES = "REMOTE_DEVICES";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        String action = intent.getAction();
        Log.i("Homepage action: ", action);
        if (ACTION_GETTING_STARTED.equals(action)) {
            // Display Get Started button
        } else if (ACTION_CONTINUE_PROGRESS.equals(action)) {
            // Display available devices & attempt to connect to the default one
            List<RemoteDevice> devices = intent.getParcelableExtra(EXTRA_REMOTE_DEVICES);
        } else if (ACTION_ERROR_STATE.equals(action)) {
            // TODO : Show error modal
        }
    }
}
