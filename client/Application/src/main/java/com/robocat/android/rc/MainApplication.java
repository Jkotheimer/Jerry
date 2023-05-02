package com.robocat.android.rc;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApplication extends Application {
    public ExecutorService mExecutorService = Executors.newFixedThreadPool(2);
}
