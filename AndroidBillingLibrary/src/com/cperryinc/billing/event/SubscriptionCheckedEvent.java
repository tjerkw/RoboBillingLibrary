package com.cperryinc.billing.event;

public class SubscriptionCheckedEvent {
    private boolean isSubscriptionSupported;

    public SubscriptionCheckedEvent(boolean subscriptionSupported) {
        isSubscriptionSupported = subscriptionSupported;
    }

    public boolean isSubscriptionSupported() {
        return isSubscriptionSupported;
    }
}
