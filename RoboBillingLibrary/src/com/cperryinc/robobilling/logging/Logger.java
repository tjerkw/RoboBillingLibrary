package com.cperryinc.robobilling.logging;

import android.util.Log;

public class Logger {
    private static final String TAG = "RoboBillingLibrary";
    private static final String LOG_FORMAT = "[%s]: %s";

    public static final void d(String tag, String message) {
        Log.d(TAG, String.format(LOG_FORMAT, tag, message));
    }

    public static final void v(String tag, String message) {
        Log.v(TAG, String.format(LOG_FORMAT, tag, message));
    }

    public static final void i(String tag, String message) {
        Log.i(TAG, String.format(LOG_FORMAT, tag, message));
    }

    public static final void w(String tag, String message) {
        Log.w(TAG, String.format(LOG_FORMAT, tag, message));
    }

    public static final void e(String tag, String message) {
        Log.e(TAG, String.format(LOG_FORMAT, tag, message));
    }

    public static void e(String tag, String message, Exception e) {
        Log.e(TAG, String.format(LOG_FORMAT, tag, message, e));
    }
}
