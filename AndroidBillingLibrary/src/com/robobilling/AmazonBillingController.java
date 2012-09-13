package com.robobilling;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.amazon.inapp.purchasing.GetUserIdResponse;
import com.amazon.inapp.purchasing.ItemDataResponse;
import com.amazon.inapp.purchasing.Offset;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.PurchaseUpdatesResponse;
import com.amazon.inapp.purchasing.PurchasingManager;
import com.amazon.inapp.purchasing.PurchasingObserver;
import com.amazon.inapp.purchasing.Receipt;
import com.robobilling.event.ItemInfoEvent;
import com.robobilling.event.PurchaseStateChangeEvent;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import net.robotmedia.billing.model.Transaction;

/**
 * Amazon flavor of AndroidBillingController
 */
public class AmazonBillingController extends AbstractBillingController {
    private static final String TAG = "AndroidBilling";
    private static final String OFFSET = "offset";
    private Context context;
    private Bus eventBus;

    @Inject
    public AmazonBillingController(Context context, Bus eventBus) {
        super(context);
        this.context = context;
        this.eventBus = eventBus;
    }


    public void onStart() {
        AmazonPurchaseObserver theObserver = new AmazonPurchaseObserver(context);
        PurchasingManager.registerObserver(theObserver);
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

    @Override
    public void restoreTransactions() {
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
            Log.v(TAG, "onSdkAvailable received: Response -" + isSandboxMode);
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
            Log.v(TAG, "onGetUserIdResponse received: Response -" + getUserIdResponse);
            Log.v(TAG, "RequestId:" + getUserIdResponse.getRequestId());
            Log.v(TAG, "IdRequestStatus:" + getUserIdResponse.getUserIdRequestStatus());
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
            Log.v(TAG, "onItemDataResponse received");
            Log.v(TAG, "ItemDataRequestStatus" + itemDataResponse.getItemDataRequestStatus());
            Log.v(TAG, "ItemDataRequestId" + itemDataResponse.getRequestId());
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
            Log.v(TAG, "onPurchaseResponse received");
            Log.v(TAG, "PurchaseRequestStatus:" + purchaseResponse.getPurchaseRequestStatus());
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
            Log.v(TAG, "onPurchaseUpdatesRecived recieved: Response -" + purchaseUpdatesResponse);
            Log.v(TAG, "PurchaseUpdatesRequestStatus:" + purchaseUpdatesResponse.getPurchaseUpdatesRequestStatus());
            Log.v(TAG, "RequestID:" + purchaseUpdatesResponse.getRequestId());
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
                RoboBillingApplication.setCurrentUser(userId);
                return true;
            } else {
                Log.v(TAG, "onGetUserIdResponse: Unable to get user ID.");
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
                        .getSharedPreferences(RoboBillingApplication.getCurrentUser(), Context.MODE_PRIVATE)
                        .getString(OFFSET, Offset.BEGINNING.toString())));
            }
        }
    }

    /*
    * Started when the observer receives a Purchase Response
    * Once the AsyncTask returns successfully, the UI is updated.
    */
    private class PurchaseAsyncTask extends AsyncTask<PurchaseResponse, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final PurchaseResponse... params) {
            final PurchaseResponse purchaseResponse = params[0];
            final String userId = RoboBillingApplication.getCurrentUser();

            if (!purchaseResponse.getUserId().equals(userId)) {
                // currently logged in user is different than what we have so update the state
                RoboBillingApplication.setCurrentUser(purchaseResponse.getUserId());
                PurchasingManager.initiatePurchaseUpdatesRequest(
                        Offset.fromString(context.getSharedPreferences(RoboBillingApplication.getCurrentUser(), Context.MODE_PRIVATE)
                                .getString(OFFSET, Offset.BEGINNING.toString())));
            }

            switch (purchaseResponse.getPurchaseRequestStatus()) {
                case SUCCESSFUL:
                    /*
                     * You can verify the receipt and fulfill the purchase on successful responses.
                     */
                    final Receipt receipt = purchaseResponse.getReceipt();
                    switch (receipt.getItemType()) {
                        case CONSUMABLE:
                            // TODO: doesn't do anything yet
                            break;
                        case ENTITLED:
                            Transaction transaction = new Transaction();
                            transaction.orderId = receipt.getPurchaseToken();
                            transaction.productId = receipt.getSku();
                            transaction.purchaseState = Transaction.PurchaseState.PURCHASED;
                            storeTransaction(context, transaction);
                            break;
                        case SUBSCRIPTION:
                            // TODO: doesn't do anything yet
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
                    Log.v(TAG, "Failed purchase request");
                    return false;
                case INVALID_SKU:
                    /*
                     * If the sku that was purchased was invalid, the application ignores the request and logs the failure.
                     * This can happen when there is a sku mismatch between what is sent from the application and what
                     * currently exists on the dev portal.
                     */
                    Log.v(TAG, "Invalid Sku for request");
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
            final String userId = RoboBillingApplication.getCurrentUser();
            if (!purchaseUpdatesResponse.getUserId().equals(userId)) {
                return false;
            }
                /*
                 * If the customer for some reason had items revoked, the skus for these items will be contained in the
                 * revoked skus set.
                 */
            for (final String sku : purchaseUpdatesResponse.getRevokedSkus()) {
                Log.v(TAG, "Revoked Sku:" + sku);
                // TODO: remove items
            }

            switch (purchaseUpdatesResponse.getPurchaseUpdatesRequestStatus()) {
                case SUCCESSFUL:
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
                                // TODO: not supported yet
                                break;
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
