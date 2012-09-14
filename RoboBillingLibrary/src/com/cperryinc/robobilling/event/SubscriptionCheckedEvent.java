package com.cperryinc.robobilling.event;

public class SubscriptionCheckedEvent {
    private boolean isSubscriptionSupported;

    public SubscriptionCheckedEvent(boolean subscriptionSupported) {
        isSubscriptionSupported = subscriptionSupported;
    }

    public boolean isSubscriptionSupported() {
        return isSubscriptionSupported;
    }
}
