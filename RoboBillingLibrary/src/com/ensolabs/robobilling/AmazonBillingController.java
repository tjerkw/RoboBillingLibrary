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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.amazon.inapp.purchasing.GetUserIdResponse;
import com.amazon.inapp.purchasing.ItemDataResponse;
import com.amazon.inapp.purchasing.Offset;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.PurchaseUpdatesResponse;
import com.amazon.inapp.purchasing.PurchasingManager;
import com.amazon.inapp.purchasing.PurchasingObserver;
import com.amazon.inapp.purchasing.Receipt;
import com.amazon.inapp.purchasing.SubscriptionPeriod;
import com.ensolabs.robobilling.event.BillingCheckedEvent;
import com.ensolabs.robobilling.event.ItemInfoEvent;
import com.ensolabs.robobilling.event.PurchaseStateChangeEvent;
import com.ensolabs.robobilling.event.SubscriptionCheckedEvent;
import com.ensolabs.robobilling.logging.Logger;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import net.robotmedia.billing.model.Transaction;
import net.robotmedia.billing.model.TransactionManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Amazon flavor of AndroidBillingController
 */
public class AmazonBillingController extends AbstractBillingController {
    private static final String TAG = "AndroidBilling";
    private static final String OFFSET = "offset";
    private Context context;
    private Bus eventBus;
    private User user;

    @Inject
    public AmazonBillingController(Context context, Bus eventBus, User user) {
        super(context);
        this.context = context;
        this.eventBus = eventBus;
        this.user = user;
    }

    @Override
    public BillingStatus checkBillingSupported() {
        // TODO: Is there any way to actually check this for Amazon?
        eventBus.post(new BillingCheckedEvent(true));
        return BillingStatus.UNKNOWN;
    }

    @Override
    public BillingStatus checkSubscriptionSupported() {
        // TODO: Is there any way to actually check this for Amazon?
        eventBus.post(new SubscriptionCheckedEvent(true));
        return BillingStatus.UNKNOWN;
    }

    public void onStart() {
        PurchasingManager.registerObserver(new AmazonPurchaseObserver(context));
    }

    /**
     * Call this in your onResume method in your Activity/Fragment.
     * When the application resumes the application checks which customer is signed in.
     */
    public void onResume() {
        PurchasingManager.initiateGetUserIdRequest();
    }

    @Override
    public void requestPurchase(String itemId) {
        PurchasingManager.initiatePurchaseRequest(itemId);
    }

    @Override
    public void requestPurchase(String itemId, boolean confirm, String developerPayload) {
        PurchasingManager.initiatePurchaseRequest(itemId);
    }

    @Override
    public void requestSubscription(String itemId) {
        PurchasingManager.initiatePurchaseRequest(itemId);
    }

    @Override
    public void requestSubscription(String itemId, boolean confirm, String developerPayload) {
        PurchasingManager.initiatePurchaseRequest(itemId);
    }

    /**
     * Transactions are automatically restored in this implementation
     * when the user id is retrieved
     *
     * @see {@link GetUserIdAsyncTask}
     */
    @Override
    public void restoreTransactions() {
        // do nothing
    }

    /**
     * This is needed in the Google implementation, this version does nothing.
     * @param activity
     * @param purchaseIntent purchase intent.
     * @param intent
     */
    @Override
    public void startPurchaseIntent(Activity activity, PendingIntent purchaseIntent, Intent intent) {
    }

    private class AmazonPurchaseObserver extends PurchasingObserver {
        public AmazonPurchaseObserver(Context context) {
            super(context);
        }

        /**
         * Invoked once the observer is registered with the Puchasing Manager If the boolean is false, the application is
         * receiving responses from the SDK Tester. If the boolean is true, the application is live in production.
         *
         * @param isSandboxMode Boolean value that shows if the app is live or not.
         */
        @Override
        public void onSdkAvailable(final boolean isSandboxMode) {
            Logger.v(TAG, "onSdkAvailable received: Response -" + isSandboxMode);
            PurchasingManager.initiateGetUserIdRequest();
        }

        /**
         * Invoked once the call from initiateGetUserIdRequest is completed.
         * On a successful response, a response object is passed which contains the request id, request status, and the
         * userid generated for your application.
         *
         * @param getUserIdResponse Response object containing the UserID
         */
        @Override
        public void onGetUserIdResponse(final GetUserIdResponse getUserIdResponse) {
            Logger.v(TAG, "onGetUserIdResponse received: Response -" + getUserIdResponse);
            Logger.v(TAG, "RequestId:" + getUserIdResponse.getRequestId());
            Logger.v(TAG, "IdRequestStatus:" + getUserIdResponse.getUserIdRequestStatus());
            new GetUserIdAsyncTask().execute(getUserIdResponse);
        }

        /**
         * Invoked once the call from initiateItemDataRequest is completed.
         * On a successful response, a response object is passed which contains the request id, request status, and a set of
         * item data for the requested skus. Items that have been suppressed or are unavailable will be returned in a
         * set of unavailable skus.
         *
         * @param itemDataResponse Response object containing a set of purchasable/non-purchasable items
         */
        @Override
        public void onItemDataResponse(final ItemDataResponse itemDataResponse) {
            Logger.v(TAG, "onItemDataResponse received");
            Logger.v(TAG, "ItemDataRequestStatus" + itemDataResponse.getItemDataRequestStatus());
            Logger.v(TAG, "ItemDataRequestId" + itemDataResponse.getRequestId());
            switch (itemDataResponse.getItemDataRequestStatus()) {
                case SUCCESSFUL_WITH_UNAVAILABLE_SKUS:
                case SUCCESSFUL:
                    eventBus.post(
                            new ItemInfoEvent(itemDataResponse.getUnavailableSkus(), itemDataResponse.getItemData()));
                    break;
                case FAILED:
                    // On failed responses will fail gracefully.
                    break;

            }
        }

        /**
         * Is invoked once the call from initiatePurchaseRequest is completed.
         * On a successful response, a response object is passed which contains the request id, request status, and the
         * receipt of the purchase.
         *
         * @param purchaseResponse Response object containing a receipt of a purchase
         */
        @Override
        public void onPurchaseResponse(final PurchaseResponse purchaseResponse) {
            Logger.v(TAG, "onPurchaseResponse received");
            Logger.v(TAG, "PurchaseRequestStatus:" + purchaseResponse.getPurchaseRequestStatus());
            new PurchaseAsyncTask().execute(purchaseResponse);
        }

        /**
         * Is invoked once the call from initiatePurchaseUpdatesRequest is completed.
         * On a successful response, a response object is passed which contains the request id, request status, a set of
         * previously purchased receipts, a set of revoked skus, and the next offset if applicable. If a user downloads your
         * application to another device, this call is used to sync up this device with all the user's purchases.
         *
         * @param purchaseUpdatesResponse Response object containing the user's recent purchases.
         */
        @Override
        public void onPurchaseUpdatesResponse(final PurchaseUpdatesResponse purchaseUpdatesResponse) {
            Logger.v(TAG, "onPurchaseUpdatesRecived recieved: Response -" + purchaseUpdatesResponse);
            Logger.v(TAG, "PurchaseUpdatesRequestStatus:" + purchaseUpdatesResponse.getPurchaseUpdatesRequestStatus());
            Logger.v(TAG, "RequestID:" + purchaseUpdatesResponse.getRequestId());
            new PurchaseUpdatesAsyncTask().execute(purchaseUpdatesResponse);
        }
    }

    private class GetUserIdAsyncTask extends AsyncTask<GetUserIdResponse, Void, Boolean> {
        @Override
        protected Boolean doInBackground(final GetUserIdResponse... params) {
            GetUserIdResponse getUserIdResponse = params[0];

            if (getUserIdResponse.getUserIdRequestStatus() == GetUserIdResponse.GetUserIdRequestStatus.SUCCESSFUL) {
                final String userId = getUserIdResponse.getUserId();

                // Each UserID has their own shared preferences file, and we'll load that file when a new user logs in.
                user.setUserId(userId);
                return true;
            } else {
                Logger.v(TAG, "onGetUserIdResponse: Unable to get user ID.");
                return false;
            }
        }

        /*
        * Call initiatePurchaseUpdatesRequest for the returned user to sync purchases that are not yet fulfilled.
        */
        @Override
        protected void onPostExecute(final Boolean result) {
            super.onPostExecute(result);
            if (result) {
                PurchasingManager.initiatePurchaseUpdatesRequest(Offset.fromString(context
                        .getSharedPreferences(user.getUserId(), Context.MODE_PRIVATE)
                        .getString(OFFSET, Offset.BEGINNING.toString())));
            }
        }
    }

    private String getSubscriptionParentSku(String fullSku) {
        String[] strings = fullSku.split("\\.");
        return strings[0];
    }

    /*
    * Started when the observer receives a Purchase Response
    * Once the AsyncTask returns successfully, the UI is updated.
    */
    private class PurchaseAsyncTask extends AsyncTask<PurchaseResponse, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final PurchaseResponse... params) {
            final PurchaseResponse purchaseResponse = params[0];
            final String userId = user.getUserId();

            if (!purchaseResponse.getUserId().equals(userId)) {
                // currently logged in user is different than what we have so update the state
                user.setUserId(purchaseResponse.getUserId());
                PurchasingManager.initiatePurchaseUpdatesRequest(
                        Offset.fromString(context.getSharedPreferences(user.getUserId(), Context.MODE_PRIVATE)
                                .getString(OFFSET, Offset.BEGINNING.toString())));
            }

            switch (purchaseResponse.getPurchaseRequestStatus()) {
                case SUCCESSFUL:
                    /*
                     * You can verify the receipt and fulfill the purchase on successful responses.
                     */
                    final Receipt receipt = purchaseResponse.getReceipt();
                    Transaction transaction = new Transaction();
                    switch (receipt.getItemType()) {
                        case CONSUMABLE:
                        case ENTITLED:
                            transaction.orderId = receipt.getPurchaseToken();
                            transaction.productId = receipt.getSku();
                            transaction.purchaseState = Transaction.PurchaseState.PURCHASED;
                            storeTransaction(context, transaction);
                            break;
                        case SUBSCRIPTION:
                            transaction.orderId = receipt.getPurchaseToken();
                            transaction.productId = getSubscriptionParentSku(receipt.getSku());
                            transaction.purchaseState = Transaction.PurchaseState.PURCHASED;
                            storeTransaction(context, transaction);
                            break;
                    }

                    return true;
                case ALREADY_ENTITLED:
                    /*
                     * It should already be in the database
                     */

                    return true;
                case FAILED:
                    /*
                     * If the purchase failed for some reason, (The customer canceled the order, or some other
                     * extraneous circumstance happens) the application ignores the request and logs the failure.
                     */
                    Logger.v(TAG, "Failed purchase request");
                    return false;
                case INVALID_SKU:
                    /*
                     * If the sku that was purchased was invalid, the application ignores the request and logs the failure.
                     * This can happen when there is a sku mismatch between what is sent from the application and what
                     * currently exists on the dev portal.
                     */
                    Logger.v(TAG, "Invalid Sku for request");
                    return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            if (success) {
                eventBus.post(new PurchaseStateChangeEvent());
            }
        }
    }

    /*
    * Started when the observer receives a Purchase Updates Response Once the AsyncTask returns successfully, we'll
    * update the UI.
    */
    private class PurchaseUpdatesAsyncTask extends AsyncTask<PurchaseUpdatesResponse, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final PurchaseUpdatesResponse... params) {
            final PurchaseUpdatesResponse purchaseUpdatesResponse = params[0];
            final String userId = user.getUserId();
            if (!purchaseUpdatesResponse.getUserId().equals(userId)) {
                return false;
            }

            /*
             * If the customer for some reason had items revoked, the skus for these items will be contained in the
             * revoked skus set.
             */
            Set<String> revokedSkus = purchaseUpdatesResponse.getRevokedSkus();
            String[] obfuscatedSkus = new String[revokedSkus.size()];
            int i = 0;
            for (final String sku : revokedSkus) {
                Logger.v(TAG, "Revoked Sku:" + sku);
                obfuscatedSkus[i] = obfuscate(context, sku);
                i++;
            }
            TransactionManager.removeTransactions(context, obfuscatedSkus);

            switch (purchaseUpdatesResponse.getPurchaseUpdatesRequestStatus()) {
                case SUCCESSFUL:
                    SubscriptionPeriod latestSubscriptionPeriod = null;
                    final Map<SubscriptionPeriod, String> currentSubscriptionPeriods = new HashMap<SubscriptionPeriod, String>();
                    for (final Receipt receipt : purchaseUpdatesResponse.getReceipts()) {
                        switch (receipt.getItemType()) {
                            case ENTITLED:
                                /*
                                 * If the receipt is for an entitlement, the customer is re-entitled.
                                 */
                                Transaction transaction = new Transaction();
                                transaction.orderId = receipt.getPurchaseToken();
                                transaction.productId = receipt.getSku();
                                transaction.purchaseState = Transaction.PurchaseState.PURCHASED;
                                storeTransaction(context, transaction);
                                break;

                            case SUBSCRIPTION:

                                /**
                                 * Purchase Updates for subscriptions can be done in one of two ways:
                                 *
                                 * 1. Use the receipts to determine if the user currently has an active subscription
                                 * 2. Use the receipts to create a subscription history for your customer.
                                 *
                                 * This library checks if there is an open subscription the application uses the receipts
                                 * returned to determine an active subscription. (option 1)
                                 *
                                 * Applications that unlock content based on past active subscription periods, should create
                                 * purchasing history for the customer.
                                 *
                                 * For example, if the customer has a magazine subscription for a year,
                                 * even if they do not have a currently active subscription,
                                 * they still have access to the magazines from when they were subscribed.
                                 */

                                final SubscriptionPeriod subscriptionPeriod = receipt.getSubscriptionPeriod();
                                final String subscriptionSku = receipt.getSku();
                                final Date startDate = subscriptionPeriod.getStartDate();

                                /**
                                 * Keep track of the receipt that has the most current start date.
                                 * Store a container of duplicate subscription periods.
                                 * If there is a duplicate, the duplicate is added to the list of current subscription periods.
                                 */

                                if (latestSubscriptionPeriod == null || startDate.after(latestSubscriptionPeriod.getStartDate())) {
                                    currentSubscriptionPeriods.clear();
                                    latestSubscriptionPeriod = subscriptionPeriod;
                                    currentSubscriptionPeriods.put(latestSubscriptionPeriod, subscriptionSku);
                                } else if (startDate.equals(latestSubscriptionPeriod.getStartDate())) {
                                    currentSubscriptionPeriods.put(receipt.getSubscriptionPeriod(), subscriptionSku);
                                }

                                break;
                        }

                        /**
                         * Check the latest subscription periods once all receipts have been read, if there is a subscription
                         * with an existing end date, then the subscription is not active, so we remove the transaction
                         */

                        if (latestSubscriptionPeriod != null) {
                            for (Map.Entry<SubscriptionPeriod, String> subscriptionPeriodEntry : currentSubscriptionPeriods.entrySet()) {
                                if (subscriptionPeriodEntry.getKey().getEndDate() != null) {
                                    final String sku = subscriptionPeriodEntry.getValue();
                                    String obfuscatedSku = obfuscate(context, sku);
                                    TransactionManager.removeTransactions(context, new String[]{obfuscatedSku});
                                    break;
                                }
                            }

                        }

                    }
                    return true;
                case FAILED:
                    // Ignore failed requests
                    return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            if (success) {
                eventBus.post(new PurchaseStateChangeEvent());
            }
        }
    }
}
