package com.cperryinc.billing.example;

import com.cperryinc.robobilling.RoboBillingApplication;

public class Application extends RoboBillingApplication {

    private BillingMode billingMode = BillingMode.AMAZON;

    // A randomly generated salt. You should generate your own for your application.
    private byte[] salt =
            new byte[]{41, -90, -116, -41, 66, -53, 122, -110, -127, -96, -88, 77, 127, 115, 1, 73, 57, 110, 48, -116};

    @Override
    public BillingMode getBillingMode() {
        return billingMode;
    }

    /**
     * For Amazon we use the currently logged in user as the salt, to
     * differentiate between different users when we save purchased items.
     *
     * @return The salt to use when obfuscating purchases in the database and
     * making calls to the Google server
     */
    @Override
    public byte[] getObfuscationSalt() {
        return billingMode == BillingMode.GOOGLE ? salt : getCurrentUser().getBytes();
    }

    @Override
    public String getPublicKey() {
        return "your public key here";
    }

}
