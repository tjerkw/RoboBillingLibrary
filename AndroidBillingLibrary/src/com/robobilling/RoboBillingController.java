package com.robobilling;

import net.robotmedia.billing.model.Transaction;
import net.robotmedia.billing.utils.IConfiguration;

import java.util.List;

public interface RoboBillingController {

    void setConfiguration(IConfiguration config);

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
}
