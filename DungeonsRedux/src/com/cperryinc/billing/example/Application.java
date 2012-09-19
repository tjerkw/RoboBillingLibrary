package com.cperryinc.billing.example;

import com.cperryinc.robobilling.RoboBillingApplication;

public class Application extends RoboBillingApplication {

    @Override
    public BillingMode getBillingMode() {
        return BillingMode.AMAZON;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public byte[] getObfuscationSalt() {
        return new byte[]{41, -90, -116, -41, 66, -53, 122, -110, -127, -96, -88, 77, 127, 115, 1, 73, 57, 110, 48, -116};
    }

    @Override
    public String getPublicKey() {
        return "your public key here";
    }

}
