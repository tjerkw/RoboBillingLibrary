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

package com.ensolabs.robobilling.helper;

import android.content.SharedPreferences;
import android.os.Bundle;
import com.ensolabs.robobilling.RoboBillingController;
import com.ensolabs.robobilling.event.BillingCheckedEvent;
import com.ensolabs.robobilling.event.ItemInfoEvent;
import com.ensolabs.robobilling.event.PurchaseIntentEvent;
import com.ensolabs.robobilling.event.PurchaseStateChangeEvent;
import com.ensolabs.robobilling.event.RequestPurchaseResponseEvent;
import com.ensolabs.robobilling.event.SubscriptionCheckedEvent;
import com.ensolabs.robobilling.event.TransactionsRestoredEvent;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import roboguice.fragment.RoboFragment;

public abstract class RoboBillingFragment extends RoboFragment {
    private static final String KEY_TRANSACTIONS_RESTORED = "com.cperryinc.robobilling.transactions_restored";
    @Inject private SharedPreferences preferences;
    @Inject protected RoboBillingController billingController;
    @Inject private Bus eventBus;
    private boolean hasCheckedBilling;
    private RoboBillingFragment.EventSubscriber eventSubscriber;

    public abstract void onPurchaseStateChanged(PurchaseStateChangeEvent event);

    public abstract void onBillingChecked(boolean supported);

    public abstract void onSubscriptionChecked(boolean supported);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventSubscriber = new EventSubscriber();
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(eventSubscriber);
        billingController.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        billingController.onResume();

        if (!hasCheckedBilling) {
            billingController.checkBillingSupported();
            billingController.checkSubscriptionSupported();
            hasCheckedBilling = true;
        }

        if (!isTransactionsRestored()) {
            billingController.restoreTransactions();
        }
    }

    @Override
    public void onStop() {
        super.onPause();
        eventBus.unregister(eventSubscriber);
    }

    private class EventSubscriber {

        @Subscribe
        public final void onTransactionsRestoredEvent(TransactionsRestoredEvent event) {
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_TRANSACTIONS_RESTORED, true);
            editor.commit();
        }

        @Subscribe
        public final void onPurchaseStateChangedEvent(PurchaseStateChangeEvent event) {
            onPurchaseStateChanged(event);
        }

        @Subscribe
        public final void onBillingCheckedEvent(BillingCheckedEvent event) {
            onBillingChecked(event.isBillingSupported());
        }

        @Subscribe
        public final void onItemInfoEvent(ItemInfoEvent event) {

        }

        @Subscribe
        public final void onPurchaseIntentEvent(PurchaseIntentEvent event) {
            billingController.startPurchaseIntent(getActivity(), event.getPurchaseIntent(), null);

        }

        @Subscribe
        public final void onRequestPurchaseResponseEvent(RequestPurchaseResponseEvent event) {

        }

        @Subscribe
        public final void onSubscriptionCheckedEvent(SubscriptionCheckedEvent event) {
            onSubscriptionChecked(event.isSubscriptionSupported());
        }
    }

    public final void requestPurchase(String itemId) {
        billingController.requestPurchase(itemId);
    }

    public final void requestSubscription(String itemId) {
        billingController.requestSubscription(itemId);
    }

    private boolean isTransactionsRestored() {
        return preferences.getBoolean(KEY_TRANSACTIONS_RESTORED, false);
    }
}
