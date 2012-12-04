/*   Copyright 2012 Christopher Perry Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package com.ensolabs.robobilling.logging;

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
