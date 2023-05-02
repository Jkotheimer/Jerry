/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.robocat.android.rc.activities;
import com.robocat.android.rc.R;
import com.robocat.android.rc.services.BluetoothLEService;
import com.robocat.android.rc.services.BluetoothConfiguration;
import com.robocat.android.rc.services.BluetoothService;
import com.robocat.android.rc.services.BluetoothStatus;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {

    private Handler handler;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private BluetoothLEService bluetoothLEService;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private final BluetoothService.OnBluetoothScanCallback bluetoothScanCallback = new BluetoothService.OnBluetoothScanCallback() {
        @Override
        public void onDeviceDiscovered(BluetoothDevice device, int rssi) {

        }
        @Override
        public void onStartScan() {

        }
        @Override
        public void onStopScan() {

        }
    };

    private final BluetoothService.OnBluetoothEventCallback bluetoothEventCallback = new BluetoothService.OnBluetoothEventCallback() {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.handler = new Handler();
        this.bluetoothDevices = new ArrayList<>();

        BluetoothConfiguration config = new BluetoothConfiguration();
        config.uuid = UUID.fromString("ebc8e850-3d0c-4231-42c5-3031a583d47f");
        config.deviceName = this.getResources().getString(R.string.app_name);
        config.bluetoothServiceClass = BluetoothLEService.class;
        config.context = this.getApplicationContext();
        config.callListenersInMainThread = true;
        config.characterDelimiter = '\n';
        config.bufferSize = 1024;
        BluetoothService.init(config);
        this.bluetoothLEService = (BluetoothLEService) BluetoothService.getDefaultInstance();
        this.bluetoothLEService.setOnScanCallback(this.bluetoothScanCallback);
        this.bluetoothLEService.setOnEventCallback(this.bluetoothEventCallback);
        this.bluetoothLEService.startScan();

        try {
            getActionBar().setTitle(R.string.title_paired_devices);
        } catch (NullPointerException npe) {
            Log.i("robocat", "Caught an exception: " + npe.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        //if (!this.isScanning) {
        //    menu.findItem(R.id.menu_stop).setVisible(false);
        //    menu.findItem(R.id.menu_scan).setVisible(true);
        //    menu.findItem(R.id.menu_refresh).setActionView(null);
        //} else {
        //    menu.findItem(R.id.menu_stop).setVisible(true);
        //    menu.findItem(R.id.menu_scan).setVisible(false);
        //    menu.findItem(R.id.menu_refresh).setActionView(
        //            R.layout.actionbar_indeterminate_progress);
        //}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //switch (item.getItemId()) {
        //    case R.id.menu_scan:
        //        mLeDeviceListAdapter.clear();
        //        scanLeDevice(true);
        //        break;
        //    case R.id.menu_stop:
        //        scanLeDevice(false);
        //        break;
        //}
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        //if (!mBluetoothAdapter.isEnabled()) {
        //    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        //}

        //// Initializes list view adapter.
        //mLeDeviceListAdapter = new LeDeviceListAdapter();
        //setListAdapter(mLeDeviceListAdapter);
        //scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        //if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
        //    finish();
        //    return;
        //}
        //super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //scanLeDevice(false);
        //mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        //if (device == null) return;
        //final Intent intent = new Intent(this, DeviceControlActivity.class);
        //if (mScanning) {
        //    mBluetoothAdapter.stopLeScan(mLeScanCallback);
        //    mScanning = false;
        //}
        //startActivity(intent);
    }

    private void scanLeDevice(final boolean enable) {
        //if (enable) {
        //    // Stops scanning after a pre-defined scan period.
        //    mHandler.postDelayed(new Runnable() {
        //        @Override
        //        public void run() {
        //            mScanning = false;
        //            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        //            invalidateOptionsMenu();
        //        }
        //    }, SCAN_PERIOD);

        //    mScanning = true;
        //    mBluetoothAdapter.startLeScan(mLeScanCallback);
        //} else {
        //    mScanning = false;
        //    mBluetoothAdapter.stopLeScan(mLeScanCallback);
        //}
        //invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                //view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //runOnUiThread(new Runnable() {
            //    @Override
            //    public void run() {
            //        mLeDeviceListAdapter.addDevice(device);
            //        mLeDeviceListAdapter.notifyDataSetChanged();
            //    }
            //});
        }
    };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}
