package com.robobilling.helper;

import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.inject.Inject;
import com.robobilling.RoboBillingController;
import com.robobilling.event.TransactionsRestoredEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import roboguice.activity.RoboFragmentActivity;

public class RoboBillingActivity extends RoboFragmentActivity {
    private static final String KEY_TRANSACTIONS_RESTORED = "com.robobilling.transactions_restored";
    @Inject private SharedPreferences preferences;
    @Inject private RoboBillingController billingController;
    @Inject private Bus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: add a call to see if billing is supported here

        if (!isTransactionsRestored()) {
            billingController.restoreTransactions();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
        billingController.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        billingController.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @Subscribe
    public final void onTransactionsRestored(TransactionsRestoredEvent event) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_TRANSACTIONS_RESTORED, true);
        editor.commit();
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
