/*
 * MIT License
 *
 * Copyright (c) 2015 Douglas Nassif Roma Junior
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.robocat.android.rc.services;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Code adapted from Android Open Source Project
 */
public class BluetoothLEService extends Service {

    private static final String TAG = BluetoothLEService.class.getSimpleName();

    private static final String ACTION_STATUS_CHANGED = "ACTION_STATUS_CHANGED";
    private static final String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    private static final String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    private static final String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    private static final String ACTION_CHARACTERISTIC_AVAILABLE = "ACTION_CHARACTERISTIC_AVAILABLE";

    private static final String EXTRA_CHARACTERISTIC = "EXTRA_CHARACTERISTIC";
    private static final String EXTRA_DEVICE = "EXTRA_DEVICE";

    private static final UUID UUID_SERVICE = UUID.fromString("75cececa-bf0a-11ed-a712-f7b97125a3fb");
    private static final UUID UUID_HANDSHAKE = UUID.fromString("26e77f82-c090-11ed-bd7b-0752602678c8");
    private static final UUID UUID_NETWORK_ID = UUID.fromString("36960d20-c090-11ed-8d57-9b0594765e6b");
    private static final UUID UUID_NETWORK_SECRET = UUID.fromString("303bc6ce-c090-11ed-9eee-d346786b4c24");
    private static final UUID UUID_MOTOR_LEFT = UUID.fromString("6d6c");
    private static final UUID UUID_MOTOR_RIGHT = UUID.fromString("6d72");

    private static final String[] PERMISSIONS = new String[] {
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    };

    private final BluetoothConfiguration mConfig;
    private BluetoothAdapter mAdapter;
    private BluetoothLeScanner mScanner;
    private BluetoothStatus mStatus;

    private final Handler mHandler;

    private String mBluetoothDeviceAddress;

    private BluetoothGatt mBluetoothGatt;

    private ArrayList<Runnable> mBlockedCallStack;

    /**
     * -----------------------------------------------
     *                  CONSTRUCTORS
     * -----------------------------------------------
     */

    public BluetoothLEService(BluetoothConfiguration config) {
        Log.d(TAG, "Constructing Bluetooth Service");
        mConfig = config;
        mHandler = new Handler(Looper.getMainLooper());
        mBlockedCallStack = new ArrayList<>();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mStatus = BluetoothStatus.NOT_SUPPORTED;
            Log.e(TAG, "BLE Not available on this device");
            return;
        }

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null) {
            mStatus = BluetoothStatus.NOT_SUPPORTED;
            Log.e(TAG, "Unable to initialize BluetoothManager.");
            return;
        }

        mAdapter = manager.getAdapter();
        if (mAdapter == null) {
            mStatus = BluetoothStatus.NOT_SUPPORTED;
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return;
        }

        mScanner = mAdapter.getBluetoothLeScanner();
        if (null == mScanner) {
            mStatus = BluetoothStatus.NOT_SUPPORTED;
            Log.e(TAG, "Unable to obtain a BluetoothLeScanner.");
            return;
        }

        if (!this.hasPermission()) {
            mStatus = BluetoothStatus.REQUIRES_PERMISSION;
        } else if (!mAdapter.isEnabled()) {
            mStatus = BluetoothStatus.OFF;
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBluetoothReceiver, filter);
        } else {
            mStatus = BluetoothStatus.ON;
        }
    }

    /**
     * ------------------------------------------------
     *              BINDER REQUIREMENTS
     * ------------------------------------------------
     */

    public class LocalBinder extends Binder {
        BluetoothLEService getService() {
            return BluetoothLEService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        this.close();
        return super.onUnbind(intent);
    }

    /**
     * ------------------------------------------------
     *               STATUS CHECKERS
     * ------------------------------------------------
     */

    public boolean hasPermission() {
        for (String permission : BluetoothLEService.PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void startScan() {
        ScanFilter filter = new ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString(UUID_SERVICE.toString()))
            .build();

        ScanSettings settings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build();

        mScanner.startScan(Collections.singletonList(filter), settings, mScanCallback);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void stopScan() {
        mScanner.stopScan(mScanCallback);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public boolean connect(final String address) {
        if (mAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mStatus = BluetoothStatus.CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mStatus = BluetoothStatus.CONNECTING;
        return true;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void disconnect() {
        if (!this.hasPermission()) {
            return;
        }
        if (mAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (ActivityCompat.checkSelfPermission(mConfig.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mStatus = BluetoothStatus.CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            } else {
                intentAction = ACTION_GATT_DISCONNECTED;
                mStatus = BluetoothStatus.ON;
                Log.i(TAG, "Disconnected from GATT server.");
            }
            broadcastUpdate(intentAction);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_CHARACTERISTIC_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_CHARACTERISTIC_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        Log.d(TAG, "Processing characteristic updated");
        if (UUID_HANDSHAKE.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Handshake format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Handshake format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_CHARACTERISTIC, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_CHARACTERISTIC, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        private final BluetoothLEService self = BluetoothLEService.this;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "Got a bluetooth broadcast event: " + action);

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "State changed");
                // Simplify BluetoothAdapter states - some of them are
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                Log.i(TAG, String.valueOf(state));
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    case BluetoothAdapter.STATE_TURNING_ON:
                        updateState(BluetoothStatus.OFF);
                        break;
                    case BluetoothAdapter.STATE_ON:
                    case BluetoothAdapter.STATE_DISCONNECTING:
                    case BluetoothAdapter.STATE_DISCONNECTED:
                        updateState(BluetoothStatus.ON);
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        updateState(BluetoothStatus.CONNECTED);
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        updateState(BluetoothStatus.CONNECTING);
                        break;
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final int RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                updateState(BluetoothStatus.SEARCHING);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                updateState(BluetoothStatus.ON);
            }
        }
    };

    private void registerReceiver() {
        unregisterReceiver();
        IntentFilter receiverFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        receiverFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        receiverFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        receiverFilter.addAction(BluetoothDevice.ACTION_FOUND);
        receiverFilter.addAction(BluetoothDevice.ACTION_UUID);
        mConfig.context.registerReceiver(mBluetoothReceiver, receiverFilter);
    }

    private void unregisterReceiver() {
        try {
            mConfig.context.unregisterReceiver(mBluetoothReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    protected synchronized void updateState(final BluetoothStatus status) {
        Log.v(TAG, "updateStatus() " + mStatus + " -> " + status);
        mStatus = status;

        // Give the new state to the Handler so the UI Activity can update
        broadcastUpdate(ACTION_STATUS_CHANGED);
    }

    protected void runOnMainThread(final Runnable runnable, final long delayMillis) {
        if (mConfig.callListenersInMainThread) {
            if (delayMillis > 0) {
                mHandler.postDelayed(runnable, delayMillis);
            } else {
                mHandler.post(runnable);
            }
        } else {
            if (delayMillis > 0) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(delayMillis);
                            runnable.run();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            } else {
                runnable.run();
            }
        }
    }

    protected void runOnMainThread(Runnable runnable) {
        runOnMainThread(runnable, 0);
    }

    /**
     * ==================================================================
     *                PUBLIC BINDER METHODS
     * ==================================================================
     */

    public BluetoothStatus getStatus() {
        if (mAdapter == null) {
            mStatus = BluetoothStatus.NOT_SUPPORTED;
        } else if (this.hasPermission()) {
            mStatus = BluetoothStatus.REQUIRES_PERMISSION;
        } else if (!mAdapter.isEnabled()) {
            mStatus = BluetoothStatus.OFF;
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            mConfig.context.registerReceiver(mBluetoothReceiver, filter);
        }
        return mStatus;
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return mAdapter.getBondedDevices();
    }

    public BluetoothDevice getDevice(String address) {
        return mAdapter.getRemoteDevice(address);
    }
}
