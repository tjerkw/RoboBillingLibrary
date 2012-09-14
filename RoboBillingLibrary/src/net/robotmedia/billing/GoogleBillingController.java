/*   Copyright 2011 Robot Media SL (http://www.robotmedia.net)
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

package net.robotmedia.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.cperryinc.robobilling.AbstractBillingController;
import com.cperryinc.robobilling.event.BillingCheckedEvent;
import com.cperryinc.robobilling.event.PurchaseIntentEvent;
import com.cperryinc.robobilling.event.PurchaseStateChangeEvent;
import com.cperryinc.robobilling.event.RequestPurchaseResponseEvent;
import com.cperryinc.robobilling.event.SubscriptionCheckedEvent;
import com.cperryinc.robobilling.event.TransactionsRestoredEvent;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import net.robotmedia.billing.model.Transaction;
import net.robotmedia.billing.model.TransactionManager;
import net.robotmedia.billing.security.DefaultSignatureValidator;
import net.robotmedia.billing.security.ISignatureValidator;
import net.robotmedia.billing.utils.Compatibility;
import net.robotmedia.billing.utils.IConfiguration;
import net.robotmedia.billing.utils.Security;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoogleBillingController extends AbstractBillingController {
    public static final String LOG_TAG = "Billing";
    private static final String JSON_NONCE = "nonce";
    private static final String JSON_ORDERS = "orders";
    private boolean debug = false;

    private BillingStatus billingStatus = BillingStatus.UNKNOWN;
    private BillingStatus subscriptionStatus = BillingStatus.UNKNOWN;
    private HashMap<String, Set<String>> manualConfirmations = new HashMap<String, Set<String>>();
    private HashMap<Long, BillingRequest> pendingRequests = new HashMap<Long, BillingRequest>();

    private Set<String> automaticConfirmations = new HashSet<String>();
    private IConfiguration configuration = null;
    private ISignatureValidator validator = null;
    private Context context;
    private Bus eventBus;

    @Inject
    public GoogleBillingController(Context context, Bus eventBus) {
        super(context);
        this.context = context;
        this.eventBus = eventBus;
    }

    /**
     * Adds the specified notification to the set of manual confirmations of the
     * specified item.
     *
     * @param itemId         id of the item.
     * @param notificationId id of the notification.
     */
    private final void addManualConfirmation(String itemId, String notificationId) {
        Set<String> notifications = manualConfirmations.get(itemId);
        if (notifications == null) {
            notifications = new HashSet<String>();
            manualConfirmations.put(itemId, notifications);
        }
        notifications.add(notificationId);
    }

    @Override
    public BillingStatus checkBillingSupported() {
        if (billingStatus == BillingStatus.UNKNOWN) {
            BillingService.checkBillingSupported(context);
        } else {
            boolean supported = billingStatus == BillingStatus.SUPPORTED;
            onBillingChecked(supported);
        }
        return billingStatus;
    }

    @Override
    public BillingStatus checkSubscriptionSupported() {
        if (subscriptionStatus == BillingStatus.UNKNOWN) {
            BillingService.checkSubscriptionSupported(context);
        } else {
            boolean supported = subscriptionStatus == BillingStatus.SUPPORTED;
            onSubscriptionChecked(supported);
        }
        return subscriptionStatus;
    }

    /**
     * Requests to confirm all pending notifications for the specified item.
     *
     * @param context
     * @param itemId  id of the item whose purchase must be confirmed.
     * @return true if pending notifications for this item were found, false
     *         otherwise.
     */
    public boolean confirmNotifications(Context context, String itemId) {
        final Set<String> notifications = manualConfirmations.get(itemId);
        if (notifications != null) {
            confirmNotifications(context, notifications.toArray(new String[]{}));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Requests to confirm all specified notifications.
     *
     * @param context
     * @param notifyIds array with the ids of all the notifications to confirm.
     */
    private void confirmNotifications(Context context, String[] notifyIds) {
        BillingService.confirmNotifications(context, notifyIds);
    }

    /**
     * Returns the number of purchases for the specified item. Refunded and
     * cancelled purchases are not subtracted.
     *
     * @param context
     * @param itemId  id of the item whose purchases will be counted.
     * @return number of purchases for the specified item.
     */
    public int countPurchases(Context context, String itemId) {
        final byte[] salt = getSalt();
        itemId = salt != null ? Security.obfuscate(context, salt, itemId) : itemId;
        return TransactionManager.countPurchases(context, itemId);
    }

    /**
     * Requests purchase information for the specified notification.
     *
     * @param context
     * @param notifyId id of the notification whose purchase information is
     *                 requested.
     */
    private void getPurchaseInformation(Context context, String notifyId) {
        final long nonce = Security.generateNonce();
        BillingService.getPurchaseInformation(context, new String[]{notifyId}, nonce);
    }

    /**
     * Called after the response to a
     * {@link net.robotmedia.billing.BillingRequest.CheckBillingSupported} request is
     * received.
     *
     * Posts a {@link com.cperryinc.robobilling.event.BillingCheckedEvent} to the event bus
     *
     * @param supported
     */
    protected void onBillingChecked(boolean supported) {
        billingStatus = supported ? BillingStatus.SUPPORTED : BillingStatus.UNSUPPORTED;
        if (billingStatus == BillingStatus.UNSUPPORTED) { // Save us the
            // subscription
            // check
            subscriptionStatus = BillingStatus.UNSUPPORTED;
        }

        eventBus.post(new BillingCheckedEvent(supported));
    }

    /**
     * Called when an IN_APP_NOTIFY message is received.
     *
     * @param notifyId notification id.
     */
    protected void onNotify(String notifyId) {
        Log.d(LOG_TAG, "Notification " + notifyId + " available");

        getPurchaseInformation(context, notifyId);
    }

    /**
     * Called after the response to a
     * {@link net.robotmedia.billing.BillingRequest.RequestPurchase} request is
     * received.
     *
     * Posts a {@link com.cperryinc.robobilling.event.PurchaseIntentEvent} to the event bus
     *
     * @param itemId         id of the item whose purchase was requested.
     * @param purchaseIntent intent to purchase the item.
     */
    protected void onPurchaseIntent(String itemId, PendingIntent purchaseIntent) {
        eventBus.post(new PurchaseIntentEvent(itemId, purchaseIntent));
    }

    /**
     * Called after the response to a
     * {@link net.robotmedia.billing.BillingRequest.GetPurchaseInformation} request is
     * received. Registers all transactions in local memory and confirms those
     * who can be confirmed automatically.
     *
     * Posts a {@link com.cperryinc.robobilling.event.PurchaseStateChangeEvent} to the event bus
     *
     * @param signedData signed JSON data received from the Market Billing service.
     * @param signature  data signature.
     */
    public void onPurchaseStateChanged(String signedData, String signature) {
        Log.d(LOG_TAG, "Purchase state changed");

        if (TextUtils.isEmpty(signedData)) {
            Log.w(LOG_TAG, "Signed data is empty");
            return;
        } else {
            Log.d(LOG_TAG, signedData);
        }

        if (!debug) {
            if (TextUtils.isEmpty(signature)) {
                Log.w(LOG_TAG, "Empty signature requires debug mode");
                return;
            }
            final ISignatureValidator validator = this.validator != null ? this.validator
                    : new DefaultSignatureValidator(configuration);
            if (!validator.validate(signedData, signature)) {
                Log.w(LOG_TAG, "Signature does not match data.");
                return;
            }
        }

        List<Transaction> purchases;
        try {
            JSONObject jObject = new JSONObject(signedData);
            if (!verifyNonce(jObject)) {
                Log.w(LOG_TAG, "Invalid nonce");
                return;
            }
            purchases = parsePurchases(jObject);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON exception: ", e);
            return;
        }

        ArrayList<String> confirmations = new ArrayList<String>();
        for (Transaction p : purchases) {
            if (p.notificationId != null && automaticConfirmations.contains(p.productId)) {
                confirmations.add(p.notificationId);
            } else {
                // TODO: Discriminate between purchases, cancellations and refunds.
                addManualConfirmation(p.productId, p.notificationId);
            }
            storeTransaction(context, p);
            eventBus.post(new PurchaseStateChangeEvent(p.productId, p.purchaseState));
        }
        if (!confirmations.isEmpty()) {
            final String[] notifyIds = confirmations.toArray(new String[confirmations.size()]);
            confirmNotifications(context, notifyIds);
        }
    }

    /**
     * Called after a {@link net.robotmedia.billing.BillingRequest} is sent.
     *
     * @param requestId the id the request.
     * @param request   the billing request.
     */
    public void onRequestSent(long requestId, BillingRequest request) {
        Log.d(LOG_TAG, "Request " + requestId + " of type " + request.getRequestType() + " sent");

        if (request.isSuccess()) {
            pendingRequests.put(requestId, request);
        } else if (request.hasNonce()) {
            Security.removeNonce(request.getNonce());
        }
    }

    /**
     * Called after a {@link net.robotmedia.billing.BillingRequest} is sent.
     *
     *
     * @param requestId    the id of the request.
     * @param responseCode the response code.
     * @see net.robotmedia.billing.BillingRequest.ResponseCode
     */
    protected void onResponseCode(long requestId, int responseCode) {
        final BillingRequest.ResponseCode response = BillingRequest.ResponseCode.valueOf(responseCode);
        Log.d(LOG_TAG, "Request " + requestId + " received response " + response);

        final BillingRequest request = pendingRequests.get(requestId);
        if (request != null) {
            pendingRequests.remove(requestId);
            request.onResponseCode(response);
        }
    }

    /**
     * Called after the response to a
     * {@link net.robotmedia.billing.BillingRequest.CheckSubscriptionSupported} request
     * is received.
     *
     * Posts a {@link com.cperryinc.robobilling.event.SubscriptionCheckedEvent} to the event bus
     *
     * @param supported
     */
    protected void onSubscriptionChecked(boolean supported) {
        subscriptionStatus = supported ? BillingStatus.SUPPORTED : BillingStatus.UNSUPPORTED;
        if (subscriptionStatus == BillingStatus.SUPPORTED) { // Save us the
            // billing check
            billingStatus = BillingStatus.SUPPORTED;
        }

        eventBus.post(new SubscriptionCheckedEvent(supported));
    }

    /**
     * Posts a {@link com.cperryinc.robobilling.event.TransactionsRestoredEvent} to the event bus
     */
    protected void onTransactionsRestored() {
        eventBus.post(new TransactionsRestoredEvent());
    }

    /**
     * Parse all purchases from the JSON data received from the Market Billing
     * service.
     *
     * @param data JSON data received from the Market Billing service.
     * @return list of purchases.
     * @throws JSONException if the data couldn't be properly parsed.
     */
    private static List<Transaction> parsePurchases(JSONObject data) throws JSONException {
        ArrayList<Transaction> purchases = new ArrayList<Transaction>();
        JSONArray orders = data.optJSONArray(JSON_ORDERS);
        int numTransactions = 0;
        if (orders != null) {
            numTransactions = orders.length();
        }
        for (int i = 0; i < numTransactions; i++) {
            JSONObject jElement = orders.getJSONObject(i);
            Transaction p = Transaction.parse(jElement);
            purchases.add(p);
        }
        return purchases;
    }

    @Override
    public void onStart() {
        // this implementation doesn't use this
    }

    @Override
    public void onResume() {
        // this implementation doesn't use this
    }

    @Override
    public void requestPurchase(String itemId) {
        requestPurchase(itemId, false, null);
    }

    @Override
    public void requestPurchase(String itemId, boolean confirm, String developerPayload) {
        if (confirm) {
            automaticConfirmations.add(itemId);
        }
        BillingService.requestPurchase(context, itemId, developerPayload);
    }

    @Override
    public void requestSubscription(String itemId) {
        requestSubscription(itemId, false, null);
    }

    @Override
    public void requestSubscription(String itemId, boolean confirm, String developerPayload) {
        if (confirm) {
            automaticConfirmations.add(itemId);
        }
        BillingService.requestSubscription(context, itemId, developerPayload);
    }

    @Override
    public void restoreTransactions() {
        final long nonce = Security.generateNonce();
        BillingService.restoreTransations(context, nonce);
    }

    /**
     * Sets debug mode.
     *
     * @param value
     */
    public final void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Sets a custom signature validator. If no custom signature validator is
     * provided,
     * {@link net.robotmedia.billing.security.DefaultSignatureValidator} will
     * be used.
     *
     * @param validator signature validator instance.
     */
    public void setSignatureValidator(ISignatureValidator validator) {
        this.validator = validator;
    }

    /**
     * Starts the specified purchase intent with the specified activity.
     *
     * @param activity
     * @param purchaseIntent purchase intent.
     * @param intent
     */
    public static void startPurchaseIntent(Activity activity, PendingIntent purchaseIntent, Intent intent) {
        if (Compatibility.isStartIntentSenderSupported()) {
            // This is on Android 2.0 and beyond. The in-app buy page activity
            // must be on the activity stack of the application.
            Compatibility.startIntentSender(activity, purchaseIntent.getIntentSender(), intent);
        } else {
            // This is on Android version 1.6. The in-app buy page activity must
            // be on its own separate activity stack instead of on the activity
            // stack of the application.
            try {
                purchaseIntent.send(activity, 0 /* code */, intent);
            } catch (CanceledException e) {
                Log.e(LOG_TAG, "Error starting purchase intent", e);
            }
        }
    }

    private static boolean verifyNonce(JSONObject data) {
        long nonce = data.optLong(JSON_NONCE);
        if (Security.isNonceKnown(nonce)) {
            Security.removeNonce(nonce);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Posts a {@link com.cperryinc.robobilling.event.RequestPurchaseResponseEvent} to the event bus
     *
     * @param itemId
     * @param response
     */
    protected void onRequestPurchaseResponse(String itemId, BillingRequest.ResponseCode response) {
        eventBus.post(new RequestPurchaseResponseEvent(itemId, response));
    }

}
