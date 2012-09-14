package com.cperryinc.robobilling.event;

import com.amazon.inapp.purchasing.Item;

import java.util.Collection;
import java.util.Map;

/**
 * TODO: Make this more generic, not using Amazon 'Item' defintion
 */
public class ItemInfoEvent {
    private Collection<String> unavailableSkus;
    private Map<String, Item> availableItems;

    public ItemInfoEvent(Collection<String> unavailableSkus, Map<String, Item> availableItems) {
        this.unavailableSkus = unavailableSkus;
        this.availableItems = availableItems;
    }

    public Collection<String> getUnavailableSkus() {
        return unavailableSkus;
    }

    public Map<String, Item> getAvailableItems() {
        return availableItems;
    }
}
