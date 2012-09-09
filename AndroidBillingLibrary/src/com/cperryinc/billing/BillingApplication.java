package com.cperryinc.billing;

import android.app.Application;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import com.squareup.otto.Bus;
import roboguice.RoboGuice;

public class BillingApplication extends Application {
    private static String userId;

    /**
     * Sets the event Bus as a singleton
     */
    public class BillingModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Bus.class).in(Singleton.class);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * Binds the BillingModule to the base application injector
         */
        RoboGuice.setBaseApplicationInjector(this,
                        RoboGuice.DEFAULT_STAGE,
                        Modules.override(RoboGuice.newDefaultRoboModule(this)).with(new BillingModule()));
    }

    public static String getCurrentUser() {
        return userId;
    }

    public static void setCurrentUser(String userId) {
        BillingApplication.userId = userId;
    }
}
