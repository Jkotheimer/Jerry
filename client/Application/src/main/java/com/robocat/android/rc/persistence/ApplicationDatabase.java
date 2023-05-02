package com.robocat.android.rc.persistence;
import com.robocat.android.rc.persistence.entities.*;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {RemoteDevice.class}, version = 1)
public abstract class ApplicationDatabase extends RoomDatabase {

    private static final String NAME = "RobocatApplication.db";
    private static volatile ApplicationDatabase INSTANCE;

    public abstract RemoteDeviceDao remoteDeviceDao();

    public static ApplicationDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ApplicationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ApplicationDatabase.class,
                            ApplicationDatabase.NAME
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
