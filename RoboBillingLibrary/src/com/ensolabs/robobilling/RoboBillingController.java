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

package com.ensolabs.robobilling;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import net.robotmedia.billing.model.Transaction;
import net.robotmedia.billing.utils.IConfiguration;

import java.util.List;

public interface RoboBillingController {
    public static enum BillingStatus {
        UNKNOWN, SUPPORTED, UNSUPPORTED
    }

    void setConfiguration(IConfiguration config);

    /**
     * Returns the in-app product billing support status, and checks it
     * asynchronously if it is currently unknown. Observers will receive a
     * {@link net.robotmedia.billing.IBillingObserver#onBillingChecked(boolean)} notification in either
     * case.
     * <p>
     * In-app product support does not imply subscription support. To check if
     * subscriptions are supported, use
     * {@link #checkSubscriptionSupported()}.
     * </p>
     *
     * @return the current in-app product billing support status (unknown,
     *         supported or unsupported). If it is unsupported, subscriptions
     *         are also unsupported.
     * @see net.robotmedia.billing.IBillingObserver#onBillingChecked(boolean)
     * @see #checkSubscriptionSupported()
     */
    BillingStatus checkBillingSupported();

    /**
     * <p>
     * Returns the subscription billing support status, and checks it
     * asynchronously if it is currently unknown. Observers will receive a
     * {@link net.robotmedia.billing.IBillingObserver#onSubscriptionChecked(boolean)} notification in
     * either case.
     * </p>
     * <p>
     * No support for subscriptions does not imply that in-app products are also
     * unsupported. To check if in-app products are supported, use
     * {@link #checkBillingSupported()}.
     * </p>
     *
     * @return the current subscription billing status (unknown, supported or
     *         unsupported). If it is supported, in-app products are also
     *         supported.
     * @see net.robotmedia.billing.IBillingObserver#onSubscriptionChecked(boolean)
     * @see #checkBillingSupported()
     */
    BillingStatus checkSubscriptionSupported();

    /**
     * Lists all transactions stored locally, including cancellations and refunds.
     *
     * @return list of transactions.
     */
    List<Transaction> getTransactions();

    /**
     * Lists all transactions of the specified item, stored locally.
     *
     * @param itemId id of the item whose transactions will be returned.
     * @return list of transactions.
     */
    List<Transaction> getTransactions(String itemId);

    /**
     * Returns true if the specified item has been registered as purchased in
     * local memory, false otherwise. Also note that the item might have been
     * purchased in another installation, but not yet registered in this one.
     *
     * @param itemId item id.
     * @return true if the specified item is purchased, false otherwise.
     */

    boolean isPurchased(String itemId);

    /**
     * Call this in your onStart method in your Activity/Fragment
     */
    void onStart();

    /**
     * Call this in your onResume method in your Activity/Fragment.
     * When the application resumes the application checks which customer is signed in (Amazon).
     */
    void onResume();

    /**
     * Requests the purchase of the specified item. The transaction will not be
     * confirmed automatically.
     * <p>
     * For subscriptions, use {@link #requestSubscription(String)}
     * instead.
     * </p>
     *
     * @param itemId id of the item to be purchased.
     * @see #requestPurchase(String, boolean, String)
     */
    void requestPurchase(String itemId);

    /**
     * <p>
     * Requests the purchase of the specified item with optional automatic
     * confirmation.
     * </p>
     * <p>
     * For subscriptions, use
     * {@link #requestSubscription(String, boolean, String)} instead.
     * </p>
     *
     * @param itemId           id of the item to be purchased.
     * @param confirm          if true, the transaction will be confirmed automatically.
     * @param developerPayload a developer-specified string that contains supplemental
     *                         information about the order.
     * @see net.robotmedia.billing.IBillingObserver#onPurchaseIntent(String, android.app.PendingIntent)
     */
    void requestPurchase(String itemId, boolean confirm, String developerPayload);

    /**
     * Requests the purchase of the specified subscription item. The transaction
     * will not be confirmed automatically.
     *
     * @param itemId id of the item to be purchased.
     * @see #requestSubscription(String, boolean, String)
     */
    void requestSubscription(String itemId);

    /**
     * Requests the purchase of the specified subscription item with optional
     * automatic confirmation.
     *
     * @param itemId           id of the item to be purchased.
     * @param confirm          if true, the transaction will be confirmed automatically.
     * @param developerPayload a developer-specified string that contains supplemental
     *                         information about the order.
     * @see net.robotmedia.billing.IBillingObserver#onPurchaseIntent(String, android.app.PendingIntent)
     */
    void requestSubscription(String itemId, boolean confirm, String developerPayload);

    /**
     * Requests to restore all transactions.
     */
    void restoreTransactions();

    /**
     * Starts the specified purchase intent with the specified activity.
     *
     * @param activity
     * @param purchaseIntent purchase intent.
     * @param intent
     */
    void startPurchaseIntent(Activity activity, PendingIntent purchaseIntent, Intent intent);
}
