package com.robocat.android.rc.activities.base;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.robocat.android.rc.R;
import com.robocat.android.rc.base.ui.Modal;
import com.robocat.android.rc.services.BluetoothLEService;
import com.robocat.android.rc.services.BluetoothConfiguration;
import com.robocat.android.rc.services.BluetoothService;
import com.robocat.android.rc.services.BluetoothStatus;

import java.util.ArrayList;
import java.util.UUID;

public abstract class BluetoothActivity extends Activity {

    private static final String TAG = BluetoothActivity.class.getSimpleName();

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 10;
    private static final int REQUEST_BLUETOOTH_ENABLE = 20;

    protected BluetoothService mBluetoothService;
    protected ArrayList<BluetoothDevice> mFoundDevices;
    protected Runnable mBlockedBluetoothAction;

    protected long mLastActionTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLastActionTime = System.currentTimeMillis();
        mFoundDevices = new ArrayList<>();
        BluetoothConfiguration config = new BluetoothConfiguration();
        config.uuid = UUID.fromString("ebc8e850-3d0c-4231-42c5-3031a583d47f");
        config.deviceName = this.getResources().getString(R.string.app_name);
        config.bluetoothServiceClass = BluetoothLEService.class;
        config.context = getApplicationContext();
        config.callListenersInMainThread = true;
        config.characterDelimiter = '\n';
        config.bufferSize = 1024;
        BluetoothService.init(config);
        mBluetoothService = BluetoothService.getDefaultInstance();
        if (mBluetoothService == null) {
            // TODO Throw error
            return;
        }
        mBluetoothService.setOnScanCallback(mBluetoothScanCallback);
        mBluetoothService.setOnEventCallback(mBluetoothEventCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothService.stopService();
    }

    protected BluetoothStatus getBluetoothStatus() {
        return mBluetoothService.getStatus();
    }

    protected void onBluetoothEnabled() {
        Log.i(TAG, "Bluetooth enabled!");
        if (mBlockedBluetoothAction != null) {
            mBlockedBluetoothAction.run();
            mBlockedBluetoothAction = null;
        }
    }

    protected void onBluetoothPermissionGranted() {
        Log.i(TAG, "Bluetooth permission granted!");
        if (mBlockedBluetoothAction != null) {
            mBlockedBluetoothAction.run();
            mBlockedBluetoothAction = null;
        }
    }

    protected void onDeviceFound(BluetoothDevice device, int rssi) {
        Log.i(TAG, "Bluetooth device found!");
        Log.i(TAG, String.valueOf(rssi));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No permission to view the device!");
            requestBluetoothPermission();
            mBlockedBluetoothAction = new Runnable() {
                @Override
                public void run() {
                    onDeviceFound(device, rssi);
                }
            };
            return;
        }
        Log.i(TAG, device.getName() == null ? "Nameless" : device.getName());
        Log.i(TAG, device.getAlias() == null ? "No Alias" : device.getAlias());
        Log.i(TAG, device.getAddress());
        mFoundDevices.add(device);
    }

    protected void requestBluetoothPermission() {
        Log.i(TAG, "Requesting bluetooth permission...");
        mLastActionTime = System.currentTimeMillis();
        this.requestPermissions(BluetoothService.PERMISSIONS, REQUEST_BLUETOOTH_PERMISSIONS);
    }

    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT
    })
    protected void requestBluetoothEnabled() {
        Log.i(TAG, "Requesting bluetooth enabled...");
        mLastActionTime = System.currentTimeMillis();
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_BLUETOOTH_ENABLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i(TAG, "Bluetooth activity result: " + String.valueOf(resultCode));
        boolean success = (Activity.RESULT_OK == resultCode);
        if (REQUEST_BLUETOOTH_ENABLE == requestCode) {
            if (success) {
                onBluetoothEnabled();
            } else {
                long elapsedTime = System.currentTimeMillis() - mLastActionTime;
                Log.d(TAG, String.valueOf(elapsedTime));
                if (elapsedTime < 100) {
                    Log.i(TAG, "Show modal!");
                    Modal modal = new Modal(this, new Modal.Handler() {
                        @Override
                        public void onCancel() {
                            Log.i(TAG, "Cancel button!");
                        }

                        @Override
                        public void onBack() {
                            Log.i(TAG, "Back button!");
                        }

                        @Override
                        public void onNext(boolean isFinished) {
                            Log.i(TAG, "next button!");
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        }
                    });
                    modal.setTitle("Enable Bluetooth Permissions");
                    modal.setMessage("Please enable bluetooth permissions to use this feature");
                    modal.show();
                }
                Log.i( TAG, "enable denied");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Modal modal = new Modal(this, new Modal.Handler() {
                        @Override public void onCancel() {}
                        @Override public void onBack() {}
                        @Override
                        public void onNext(boolean isFinished) {
                            openApplicationSettings();
                        }
                    });
                    modal.setTitle("Enable Bluetooth Permissions");
                    modal.setMessage("Please enable bluetooth permissions to use this app.");
                    modal.setNextButton("Settings");
                    modal.setCancelButton("Close");
                    modal.show();
                    return;
                }
            }
        }
        onBluetoothPermissionGranted();
    }

    protected void openApplicationSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {}
    }

    protected void openBluetoothSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
        } catch (ActivityNotFoundException ignored) {}
    }

    private void attachService() {
        Intent serviceIntent = new Intent(this, BluetoothLEService.class);
        bindService(serviceIntent, mConnection, Service.BIND_AUTO_CREATE);
    }

    private void detachService() {
        unbindService(mConnection);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = BluetoothService.getDefaultInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final BluetoothService.OnBluetoothScanCallback mBluetoothScanCallback = new BluetoothService.OnBluetoothScanCallback() {
        @Override
        public void onDeviceDiscovered(BluetoothDevice device, int rssi) {
            Log.d(TAG, "Device found!!!");
            onDeviceFound(device, rssi);
        }
        @Override
        public void onStartScan() {
            Log.d(TAG, "Start scan (Bluetooth activity)");
        }
        @Override
        public void onStopScan() {

        }
    };

    private final BluetoothService.OnBluetoothEventCallback mBluetoothEventCallback = new BluetoothService.OnBluetoothEventCallback() {
        @Override
        public void onDataRead(byte[] buffer, int length) {

        }
        @Override
        public void onStatusChange(BluetoothStatus status) {

        }
        @Override
        public void onDeviceName(String deviceName) {

        }
        @Override
        public void onToast(String message) {

        }
        @Override
        public void onDataWrite(byte[] buffer) {

        }
    };
}
