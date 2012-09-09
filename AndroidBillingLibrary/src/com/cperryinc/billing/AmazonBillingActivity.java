package com.cperryinc.billing;

import android.support.v4.app.FragmentActivity;
import com.google.inject.Inject;

public class AmazonBillingActivity extends FragmentActivity {
    @Inject AmazonBillingController billingController;

    /**
     * Whenever the application regains focus, the observer is registered again.
     */
    @Override
    public void onStart() {
        super.onStart();
        billingController.onStart();
    }

    /**
     * When the application resumes the application checks which customer is signed in.
     */
    @Override
    protected void onResume() {
        super.onResume();
        billingController.onResume();
    }


}
