package com.cperryinc.billing.event;

import static net.robotmedia.billing.request.AbstractGoogleBillingRequest.ResponseCode;

public class RequestPurchaseResponseEvent {
    private String itemId;
    private ResponseCode response;

    public RequestPurchaseResponseEvent(String itemId, ResponseCode response) {
        this.itemId = itemId;
        this.response = response;
    }

    public String getItemId() {
        return itemId;
    }

    public ResponseCode getResponse() {
        return response;
    }
}
