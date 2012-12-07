/*   Copyright 2011 Robot Media SL (http://www.robotmedia.net)
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

package net.robotmedia.billing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.ensolabs.robobilling.RoboBillingController;
import com.ensolabs.robobilling.logging.Logger;
import com.google.inject.Inject;
import roboguice.RoboGuice;

public class BillingReceiver extends BroadcastReceiver {

    static final String ACTION_NOTIFY = "com.android.vending.billing.IN_APP_NOTIFY";
    static final String ACTION_RESPONSE_CODE =
            "com.android.vending.billing.RESPONSE_CODE";
    static final String ACTION_PURCHASE_STATE_CHANGED =
            "com.android.vending.billing.PURCHASE_STATE_CHANGED";

    static final String EXTRA_NOTIFICATION_ID = "notification_id";
    static final String EXTRA_INAPP_SIGNED_DATA = "inapp_signed_data";
    static final String EXTRA_INAPP_SIGNATURE = "inapp_signature";
    static final String EXTRA_REQUEST_ID = "request_id";
    static final String EXTRA_RESPONSE_CODE = "response_code";

    @Inject private RoboBillingController billingController;

    @Override
    public void onReceive(Context context, Intent intent) {
        RoboGuice.getInjector(context).injectMembers(this);

        final String action = intent.getAction();

        if (ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
            purchaseStateChanged(intent);
        } else if (ACTION_NOTIFY.equals(action)) {
            notify(intent);
        } else if (ACTION_RESPONSE_CODE.equals(action)) {
            responseCode(intent);
        } else {
            Logger.w(this.getClass().getSimpleName(), "Unexpected action: " + action);
        }
    }

    private void purchaseStateChanged(Intent intent) {
        final String signedData = intent.getStringExtra(EXTRA_INAPP_SIGNED_DATA);
        final String signature = intent.getStringExtra(EXTRA_INAPP_SIGNATURE);
        ((GoogleBillingController) billingController).onPurchaseStateChanged(signedData, signature);
    }

    private void notify(Intent intent) {
        String notifyId = intent.getStringExtra(EXTRA_NOTIFICATION_ID);
        ((GoogleBillingController) billingController).onNotify(notifyId);
    }

    private void responseCode(Intent intent) {
        final long requestId = intent.getLongExtra(EXTRA_REQUEST_ID, -1);
        final int responseCode = intent.getIntExtra(EXTRA_RESPONSE_CODE, 0);
        ((GoogleBillingController) billingController).onResponseCode(requestId, responseCode);
    }

}
