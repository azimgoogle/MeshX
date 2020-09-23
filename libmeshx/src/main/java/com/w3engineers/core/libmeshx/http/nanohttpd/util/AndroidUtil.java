/*
 * ***************************************************************************
 *  Copyright (C) Wave.io - All Rights Reserved.
 *
 *  This file is part of YO! project.
 *
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential.
 * ***************************************************************************
 */

package com.w3engineers.core.libmeshx.http.nanohttpd.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.Random;

/**
 * Provides Android Utility
 */
public final class AndroidUtil {
    private AndroidUtil() {
    }

    public static boolean needThread(Thread thread) {
        return (thread == null || !thread.isAlive());
    }

    public static Thread createThread(Runnable runnable) {
        return new Thread(runnable);
    }

    public static Thread createThread(Runnable runnable, String name) {
        Thread thread = new Thread(runnable);
        thread.setName(name);
        thread.setDaemon(true);
        return thread;
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static HandlerThread executor;
    private static Handler backgroundHandler;

    private static final long defaultDelay = 270L;

    public static void post(Runnable runnable) {
        uiHandler.post(runnable);
    }

    public static void post(Runnable runnable, long time) {
        uiHandler.postDelayed(runnable, time);
    }

    public static void postDelay(Runnable runnable) {
        post(runnable, defaultDelay);
    }

    public static void postDelay(Runnable runnable, long delay) {
        post(runnable, delay);
    }

    public static void remove(Runnable runnable) {
        uiHandler.removeCallbacks(runnable);
    }

    public static void postBackground(Runnable runnable) {
        postBackground(runnable, 0);

    }

    public static void postBackground(Runnable runnable, long delay) {
        resolveBackgroundHandler();
        backgroundHandler.removeCallbacks(runnable);
        backgroundHandler.postDelayed(runnable, delay);

    }
    public static void removeAllBackgroundHandlers() {
        backgroundHandler.removeCallbacksAndMessages(null);
    }



    public static void removeBackground(Runnable runnable) {
        resolveBackgroundHandler();
        backgroundHandler.removeCallbacks(runnable);
    }

    private static void resolveBackgroundHandler() {
        if (executor == null) {
            executor = new HandlerThread("background-handler");
            executor.start();
        }

        if (backgroundHandler == null) {
            backgroundHandler = new Handler(executor.getLooper());
        }
    }

    String getAlphaNumericString(int n) {

        // lower limit for LowerCase Letters
        int lowerLimit = 97;

        // lower limit for LowerCase Letters
        int upperLimit = 122;

        Random random = new Random();

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer(n);

        for (int i = 0; i < n; i++) {

            // take a random value between 97 and 122
            int nextRandomChar = lowerLimit
                    + (int) (random.nextFloat()
                    * (upperLimit - lowerLimit + 1));

            // append a character at the end of bs
            r.append((char) nextRandomChar);
        }

        // return the resultant string
        return r.toString();
    }
}
