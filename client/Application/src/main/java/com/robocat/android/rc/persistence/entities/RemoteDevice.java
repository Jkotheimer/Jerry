package com.robocat.android.rc.persistence.entities;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.UUID;

@Entity
public class RemoteDevice implements Parcelable {

    /**
     * ---------------------------
     * REMOTEDEVICE TABLE COLUMNS
     * ---------------------------
     */

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "address")
    public String address;

    @NonNull
    @ColumnInfo(name = "uuid")
    public UUID uuid;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @NonNull
    @ColumnInfo(name = "is_default")
    public Boolean isDefault;

    /**
     * ------------------------
     *      CONSTRUCTORS
     * ------------------------
     */

    @Ignore
    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT
    })
    public RemoteDevice(BluetoothDevice device) {
        ParcelUuid[] parcelUuids = device.getUuids();
        if (parcelUuids != null && parcelUuids.length > 0) {
            for (ParcelUuid parcelUuid : parcelUuids) {
                this.uuid = parcelUuid.getUuid();
                break;
            }
        }
        this.name = device.getName() == null ? (device.getAlias() == null ? "Anonymous Device" : device.getAlias()) : device.getName();
        this.address = device.getAddress();
    }

    protected RemoteDevice(UUID uuid, String name, String address, Boolean isDefault) {
        this.uuid = uuid;
        this.name = name;
        this.address = address;
        this.isDefault = isDefault;
    }

    @Ignore
    protected RemoteDevice(Parcel in) {
        uuid = UUID.fromString(in.readString());
        name = in.readString();
        isDefault = in.readByte() != 0;
    }

    @Ignore
    public RemoteDevice(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.isDefault = false;
    }

    /**
     * ----------------------------------
     * ATTRIBUTE GETTERS FOR LIST ADAPTER
     * ----------------------------------
     */

    @NonNull
    public String getName() {
        return this.name;
    }

    @NonNull
    public String getAddress() {
        return this.address;
    }

    @NonNull
    public UUID getUuid() {
        return this.uuid;
    }

    @NonNull
    public Boolean getIsDefault() {
        return this.isDefault;
    }

    /**
     * ----------------------------------
     *      PARCELABLE REQUIREMENTS
     * ----------------------------------
     */

    @Ignore
    public static final Creator<RemoteDevice> CREATOR = new Creator<RemoteDevice>() {
        @Override
        public RemoteDevice createFromParcel(Parcel in) {
            return new RemoteDevice(in);
        }

        @Override
        public RemoteDevice[] newArray(int size) {
            return new RemoteDevice[size];
        }
    };

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid.toString());
        dest.writeString(name);
        dest.writeByte((byte) (isDefault ? 1 : 0));
    }
}
