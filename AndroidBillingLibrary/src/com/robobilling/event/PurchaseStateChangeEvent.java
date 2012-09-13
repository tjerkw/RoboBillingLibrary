package com.robobilling.event;

import net.robotmedia.billing.model.Transaction;

public class PurchaseStateChangeEvent {
    private String productId;
    private Transaction.PurchaseState purchaseState;

    // Empty constructor for empty events fired from Amazon,
    // There could be multiple purchases with this event
    // TODO: keep a map of productId/PurchaseState instead of a single piece of info
    public PurchaseStateChangeEvent() {
    }

    // Google flavored constructor, with some info
    public PurchaseStateChangeEvent(String productId, Transaction.PurchaseState purchaseState) {
        this.productId = productId;
        this.purchaseState = purchaseState;
    }

    public String getProductId() {
        return productId;
    }

    public Transaction.PurchaseState getPurchaseState() {
        return purchaseState;
    }
}
