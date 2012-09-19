package com.cperryinc.robobilling.helper;

import android.content.SharedPreferences;
import android.os.Bundle;
import com.cperryinc.robobilling.RoboBillingController;
import com.cperryinc.robobilling.event.BillingCheckedEvent;
import com.cperryinc.robobilling.event.ItemInfoEvent;
import com.cperryinc.robobilling.event.PurchaseIntentEvent;
import com.cperryinc.robobilling.event.PurchaseStateChangeEvent;
import com.cperryinc.robobilling.event.RequestPurchaseResponseEvent;
import com.cperryinc.robobilling.event.SubscriptionCheckedEvent;
import com.cperryinc.robobilling.event.TransactionsRestoredEvent;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import roboguice.activity.RoboFragmentActivity;

public abstract class RoboBillingFragmentActivity extends RoboFragmentActivity {
    private static final String KEY_TRANSACTIONS_RESTORED = "com.cperryinc.robobilling.transactions_restored";
    @Inject private SharedPreferences preferences;
    @Inject protected RoboBillingController billingController;
    @Inject private Bus eventBus;
    private boolean hasCheckedBilling;
    private RoboBillingFragmentActivity.EventSubscriber eventSubscriber;

    public abstract void onPurchaseStateChanged(PurchaseStateChangeEvent event);

    public abstract void onBillingChecked(boolean supported);

    public abstract void onSubscriptionChecked(boolean supported);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    protected void onResume() {
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
    protected void onStop() {
        super.onPause();
        eventBus.unregister(eventSubscriber);
    }

    public class EventSubscriber {

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
