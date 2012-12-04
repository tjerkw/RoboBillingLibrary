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

package com.ensolabs.robobilling.event;

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
