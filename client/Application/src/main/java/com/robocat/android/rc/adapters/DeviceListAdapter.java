package com.robocat.android.rc.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.robocat.android.rc.R;
import com.robocat.android.rc.persistence.entities.RemoteDevice;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<RemoteDevice> {

    private static final String TAG = DeviceListAdapter.class.getSimpleName();

    private final ArrayList<RemoteDevice> mDevices;
    Context mContext;

    private static class ViewHolder {
        TextView name;
        TextView uuid;
    }

    /**
     * Constructor
     *
     * @param context            The current context.
     * @param resource           The resource ID for a layout file containing a layout to use when
     *                           instantiating views.
     * @param objects            The objects to represent in the ListView.
     */
    public DeviceListAdapter(@NonNull Context context, int resource, @NonNull List<RemoteDevice> objects) {
        super(context, resource, objects);
        Log.e(TAG,"Device list adapter initialized");
        Log.e(TAG,String.valueOf(objects.size()));
        mContext = context;
        mDevices = (ArrayList<RemoteDevice>) objects;
    }

    public void addDevice(RemoteDevice device) {
        mDevices.add(device);
        notifyDataSetChanged();
    }

    public RemoteDevice getDevice(int index) {
        return mDevices.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View currentItemView = convertView;
        ViewHolder viewHolder;

        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.device_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) currentItemView.findViewById(R.id.device_name);
            viewHolder.uuid = (TextView) currentItemView.findViewById(R.id.device_address);
        } else {
            viewHolder = (ViewHolder) currentItemView.getTag();
        }

        RemoteDevice device = (RemoteDevice) getItem(position);
        viewHolder.name.setText(device.name);
        viewHolder.uuid.setText(device.address);
        currentItemView.setTag(viewHolder);

        return currentItemView;
    }
}
