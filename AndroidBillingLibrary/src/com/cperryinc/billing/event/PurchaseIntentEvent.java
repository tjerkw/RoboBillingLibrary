package com.cperryinc.billing.event;

import android.app.PendingIntent;

public class PurchaseIntentEvent {
    private String itemId;
    private PendingIntent purchaseIntent;

    public PurchaseIntentEvent(String itemId, PendingIntent purchaseIntent) {
        this.itemId = itemId;
        this.purchaseIntent = purchaseIntent;
    }

    public String getItemId() {
        return itemId;
    }

    public PendingIntent getPurchaseIntent() {
        return purchaseIntent;
    }
}
