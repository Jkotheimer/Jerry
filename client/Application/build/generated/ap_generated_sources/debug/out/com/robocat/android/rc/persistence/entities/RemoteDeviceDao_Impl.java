package com.robocat.android.rc.persistence.entities;

import android.database.Cursor;
import androidx.room.EmptyResultSetException;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.RxRoom;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.UUIDUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.lang.Void;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class RemoteDeviceDao_Impl implements RemoteDeviceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RemoteDevice> __insertionAdapterOfRemoteDevice;

  private final EntityDeletionOrUpdateAdapter<RemoteDevice> __deletionAdapterOfRemoteDevice;

  private final EntityDeletionOrUpdateAdapter<RemoteDevice> __updateAdapterOfRemoteDevice;

  public RemoteDeviceDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRemoteDevice = new EntityInsertionAdapter<RemoteDevice>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `RemoteDevice` (`address`,`uuid`,`name`,`is_default`) VALUES (?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, RemoteDevice value) {
        if (value.address == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.address);
        }
        if (value.uuid == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindBlob(2, UUIDUtil.convertUUIDToByte(value.uuid));
        }
        if (value.name == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.name);
        }
        final Integer _tmp = value.isDefault == null ? null : (value.isDefault ? 1 : 0);
        if (_tmp == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindLong(4, _tmp);
        }
      }
    };
    this.__deletionAdapterOfRemoteDevice = new EntityDeletionOrUpdateAdapter<RemoteDevice>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `RemoteDevice` WHERE `address` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, RemoteDevice value) {
        if (value.address == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.address);
        }
      }
    };
    this.__updateAdapterOfRemoteDevice = new EntityDeletionOrUpdateAdapter<RemoteDevice>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR REPLACE `RemoteDevice` SET `address` = ?,`uuid` = ?,`name` = ?,`is_default` = ? WHERE `address` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, RemoteDevice value) {
        if (value.address == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.address);
        }
        if (value.uuid == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindBlob(2, UUIDUtil.convertUUIDToByte(value.uuid));
        }
        if (value.name == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.name);
        }
        final Integer _tmp = value.isDefault == null ? null : (value.isDefault ? 1 : 0);
        if (_tmp == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindLong(4, _tmp);
        }
        if (value.address == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.address);
        }
      }
    };
  }

  @Override
  public Completable insert(final RemoteDevice device) {
    return Completable.fromCallable(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRemoteDevice.insert(device);
          __db.setTransactionSuccessful();
          return null;
        } finally {
          __db.endTransaction();
        }
      }
    });
  }

  @Override
  public Completable delete(final RemoteDevice device) {
    return Completable.fromCallable(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRemoteDevice.handle(device);
          __db.setTransactionSuccessful();
          return null;
        } finally {
          __db.endTransaction();
        }
      }
    });
  }

  @Override
  public Completable update(final RemoteDevice device) {
    return Completable.fromCallable(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRemoteDevice.handle(device);
          __db.setTransactionSuccessful();
          return null;
        } finally {
          __db.endTransaction();
        }
      }
    });
  }

  @Override
  public Single<RemoteDevice> getRemoteDevice(final UUID uuid) {
    final String _sql = "SELECT * FROM REMOTEDEVICE WHERE uuid = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (uuid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindBlob(_argIndex, UUIDUtil.convertUUIDToByte(uuid));
    }
    return RxRoom.createSingle(new Callable<RemoteDevice>() {
      @Override
      public RemoteDevice call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "is_default");
          final RemoteDevice _result;
          if(_cursor.moveToFirst()) {
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            }
            final UUID _tmpUuid;
            if (_cursor.isNull(_cursorIndexOfUuid)) {
              _tmpUuid = null;
            } else {
              _tmpUuid = UUIDUtil.convertByteToUUID(_cursor.getBlob(_cursorIndexOfUuid));
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final Boolean _tmpIsDefault;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfIsDefault)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfIsDefault);
            }
            _tmpIsDefault = _tmp == null ? null : _tmp != 0;
            _result = new RemoteDevice(_tmpUuid,_tmpName,_tmpAddress,_tmpIsDefault);
          } else {
            _result = null;
          }
          if(_result == null) {
            throw new EmptyResultSetException("Query returned empty result set: " + _statement.getSql());
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Single<List<RemoteDevice>> getAllRemoteDevices() {
    final String _sql = "SELECT * FROM REMOTEDEVICE";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return RxRoom.createSingle(new Callable<List<RemoteDevice>>() {
      @Override
      public List<RemoteDevice> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "address");
          final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "is_default");
          final List<RemoteDevice> _result = new ArrayList<RemoteDevice>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final RemoteDevice _item;
            final String _tmpAddress;
            if (_cursor.isNull(_cursorIndexOfAddress)) {
              _tmpAddress = null;
            } else {
              _tmpAddress = _cursor.getString(_cursorIndexOfAddress);
            }
            final UUID _tmpUuid;
            if (_cursor.isNull(_cursorIndexOfUuid)) {
              _tmpUuid = null;
            } else {
              _tmpUuid = UUIDUtil.convertByteToUUID(_cursor.getBlob(_cursorIndexOfUuid));
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final Boolean _tmpIsDefault;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfIsDefault)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfIsDefault);
            }
            _tmpIsDefault = _tmp == null ? null : _tmp != 0;
            _item = new RemoteDevice(_tmpUuid,_tmpName,_tmpAddress,_tmpIsDefault);
            _result.add(_item);
          }
          if(_result == null) {
            throw new EmptyResultSetException("Query returned empty result set: " + _statement.getSql());
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
