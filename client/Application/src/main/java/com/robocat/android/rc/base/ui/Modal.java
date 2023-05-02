package com.robocat.android.rc.base.ui;

import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

public class Modal extends AlertDialog {

    private Handler mHandler;

    private final OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (DialogInterface.BUTTON_NEGATIVE == which) {
                mHandler.onCancel();
            } else if (DialogInterface.BUTTON_NEUTRAL == which) {
                mHandler.onBack();
            } else if (DialogInterface.BUTTON_POSITIVE == which) {
                mHandler.onNext(true);
            }
        }
    };

    public Modal(Context context, Handler handler) {
        super(context);
        mHandler = handler;
        setCancelable(true);
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mHandler != null) {
                    mHandler.onCancel();
                }
            }
        });
    }

    public void setBackButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_NEUTRAL, text, mClickListener);
    }

    public void setCancelButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_NEGATIVE, text, mClickListener);
    }

    public void setNextButton(CharSequence text) {
        setButton(DialogInterface.BUTTON_POSITIVE, text, mClickListener);
    }

    public interface Handler {
        public abstract void onCancel();
        public abstract void onBack();
        public abstract void onNext(boolean isFinished);
    }
}
