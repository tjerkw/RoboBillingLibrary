package com.cperryinc.robobilling;

import android.content.Context;
import android.util.Log;
import net.robotmedia.billing.model.Transaction;
import net.robotmedia.billing.model.TransactionManager;
import net.robotmedia.billing.utils.IConfiguration;
import net.robotmedia.billing.utils.Security;

import java.util.List;

public abstract class AbstractBillingController implements RoboBillingController {

    private static final String LOG_TAG = "AbstractBillingController";
    protected IConfiguration configuration = null;
    private Context context;

    protected AbstractBillingController(Context context) {
        this.context = context;
    }

    /**
     * Sets the configuration instance of the controller.
     *
     * @param config configuration instance.
     */
    public void setConfiguration(IConfiguration config) {
        configuration = config;
    }

    /**
     * Lists all transactions stored locally, including cancellations and
     * refunds.
     *
     * @return list of transactions.
     */
    public List<Transaction> getTransactions() {
        List<Transaction> transactions = TransactionManager.getTransactions(context);
        unobfuscate(context, transactions);
        return transactions;
    }

    /**
     * Lists all transactions of the specified item, stored locally.
     *
     * @param itemId id of the item whose transactions will be returned.
     * @return list of transactions.
     */
    public List<Transaction> getTransactions(String itemId) {
        final byte[] salt = getSalt();
        itemId = salt != null ? Security.obfuscate(context, salt, itemId) : itemId;
        List<Transaction> transactions = TransactionManager.getTransactions(context, itemId);
        unobfuscate(context, transactions);
        return transactions;
    }

    /**
     * Returns true if the specified item has been registered as purchased in
     * local memory, false otherwise. Also note that the item might have been
     * purchased in another installation, but not yet registered in this one.
     *
     * @param itemId item id.
     * @return true if the specified item is purchased, false otherwise.
     */
    public boolean isPurchased(String itemId) {
        final byte[] salt = getSalt();
        itemId = salt != null ? Security.obfuscate(context, salt, itemId) : itemId;
        return TransactionManager.isPurchased(context, itemId);
    }

    protected void storeTransaction(Context context, Transaction t) {
        final Transaction t2 = t.clone();
        obfuscate(context, t2);
        TransactionManager.addTransaction(context, t2);
    }

    protected void unobfuscate(Context context, List<Transaction> transactions) {
        for (Transaction p : transactions) {
            unobfuscate(context, p);
        }
    }

    /**
     * Obfuscates the specified purchase. Only the order id, product id and
     * developer payload are obfuscated.
     *
     * @param context
     * @param purchase purchase to be obfuscated.
     * @see #unobfuscate(Context, Transaction)
     */
    protected void obfuscate(Context context, Transaction purchase) {
        final byte[] salt = getSalt();
        if (salt == null) {
            return;
        }
        purchase.orderId = Security.obfuscate(context, salt, purchase.orderId);
        purchase.productId = Security.obfuscate(context, salt, purchase.productId);
        purchase.developerPayload = Security.obfuscate(context, salt, purchase.developerPayload);
    }

    /**
     * Unobfuscate the specified purchase.
     *
     * @param context
     * @param purchase purchase to unobfuscate.
     * @see #obfuscate(Context, Transaction)
     */
    protected void unobfuscate(Context context, Transaction purchase) {
        final byte[] salt = getSalt();
        if (salt == null) {
            return;
        }
        purchase.orderId = Security.unobfuscate(context, salt, purchase.orderId);
        purchase.productId = Security.unobfuscate(context, salt, purchase.productId);
        purchase.developerPayload = Security.unobfuscate(context, salt, purchase.developerPayload);
    }

    /**
     * Gets the salt from the configuration and logs a warning if it's null.
     *
     * @return salt.
     */
    protected byte[] getSalt() {
        byte[] salt = null;
        if (configuration == null || ((salt = configuration.getObfuscationSalt()) == null)) {
            Log.w(LOG_TAG, "Can't (un)obfuscate purchases without salt");
        }
        return salt;
    }
}
