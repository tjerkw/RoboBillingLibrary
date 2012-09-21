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

package com.cperryinc.robobilling.event;

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
