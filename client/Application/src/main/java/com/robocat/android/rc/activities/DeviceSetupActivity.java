package com.robocat.android.rc.activities;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.robocat.android.rc.R;
import com.robocat.android.rc.activities.base.BluetoothActivity;
import com.robocat.android.rc.adapters.DeviceListAdapter;
import com.robocat.android.rc.base.ui.Modal;
import com.robocat.android.rc.persistence.entities.RemoteDevice;
import com.robocat.android.rc.services.BluetoothService;
import com.robocat.android.rc.services.BluetoothStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class DeviceSetupActivity extends BluetoothActivity {

    private static final String TAG = DeviceSetupActivity.class.getSimpleName();

    public static final String ACTION_GETTING_STARTED = "ACTION_GETTING_STARTED";
    public static final String ACTION_CONNECT_NEW_DEVICE = "ACTION_CONNECT_NEW_DEVICE";

    ListView mListView;
    private DeviceListAdapter mListAdapter;
    private ArrayList<RemoteDevice> mDevices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevices = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.device_list_view);
        mListAdapter = new DeviceListAdapter(getApplicationContext(), R.layout.device_list_item, mDevices);

        String action = this.getIntent().getAction();
        Log.d(TAG, action);
        if (ACTION_GETTING_STARTED.equals(action)) {
            setContentView(R.layout.getting_started);
        } else {
            setContentView(R.layout.device_list);
        }
    }

    public void onClickGetStarted(View v) {
        Log.d(TAG, "Clicked getting started");
        BluetoothStatus bluetoothStatus = getBluetoothStatus();
        Log.d(TAG, bluetoothStatus.name());
        if (BluetoothStatus.ON.equals(bluetoothStatus)) {
            startScanAndPopulate();
            return;
        } else if (BluetoothStatus.CONNECTING.equals(bluetoothStatus) || BluetoothStatus.CONNECTED.equals(bluetoothStatus)) {
            mBluetoothService.disconnect();
            startScanAndPopulate();
        } else if (BluetoothStatus.NOT_SUPPORTED.equals(bluetoothStatus)) {
            return;
        }
        Log.d(TAG, "Setting blocked action");
        mBlockedBluetoothAction = new Runnable() {
            @Override
            public void run() {
                startScanAndPopulate();
            }
        };
        if (BluetoothStatus.REQUIRES_PERMISSION.equals(bluetoothStatus) ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Requesting permission");
            requestBluetoothPermission();
        } else if (BluetoothStatus.OFF.equals(bluetoothStatus)) {
            Log.i(TAG, "Requesting enable");
            requestBluetoothEnabled();
        }
    }

    private void startScanAndPopulate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermission();
            mBlockedBluetoothAction = new Runnable() {
                @Override
                public void run() {
                    startScanAndPopulate();
                }
            };
            return;
        }
        setContentView(R.layout.device_list);
        mBluetoothService.startScan();
    }

    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT
    })
    private void addDevice(BluetoothDevice btDevice) {
        RemoteDevice device = new RemoteDevice(btDevice);
        if (mListView == null) {
            initializeListView();
        }
        mListAdapter.addDevice(device);
    }

    private void initializeListView() {
        mListView = (ListView) findViewById(R.id.device_list_view);
        if (mListView == null) {
            Log.e(TAG, "List view component cant be found!");
            return;
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (ActivityCompat.checkSelfPermission(DeviceSetupActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothPermission();
                    mBlockedBluetoothAction = new Runnable() {
                        @Override
                        public void run() {
                            onItemClick(parent, view, position, id);
                            RemoteDevice selectedRemoteDevice = mListAdapter.getDevice(position);
                            onSelectDevice(mBluetoothService.getDevice(selectedRemoteDevice.getAddress()));
                        }
                    };
                    return;
                }
                Log.i(TAG, "Item clicked!");
                RemoteDevice selectedRemoteDevice = mListAdapter.getDevice(position);
                Log.i(TAG, selectedRemoteDevice.getName());
                onSelectDevice(mBluetoothService.getDevice(selectedRemoteDevice.getAddress()));
            }
        });
        mListView.setAdapter(mListAdapter);
    }

    private void onSelectDevice(BluetoothDevice device) {
        mBluetoothService.connect(device);
    }

    @Override
    protected void onDeviceFound(BluetoothDevice device, int rssi) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermission();
            mBlockedBluetoothAction = new Runnable() {
                @Override
                public void run() {
                    onDeviceFound(device, rssi);
                }
            };
            return;
        }
        super.onDeviceFound(device, rssi);
        if (device.getName() != null || device.getAlias() != null) {
            addDevice(device);
        }
    }

    /**
     * Called when pointer capture is enabled or disabled for the current window.
     *
     * @param hasCapture True if the window has pointer capture.
     */
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
