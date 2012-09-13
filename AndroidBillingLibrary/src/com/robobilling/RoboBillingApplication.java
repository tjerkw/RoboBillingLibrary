package com.robobilling;

import android.app.Application;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import com.squareup.otto.Bus;
import net.robotmedia.billing.GoogleBillingController;
import net.robotmedia.billing.utils.IConfiguration;
import roboguice.RoboGuice;

public class RoboBillingApplication extends Application implements IConfiguration {
    private static String userId;
    @Inject private RoboBillingController billingController;

    /**
     * Tells Roboguice to set the event Bus as a singleton,
     * when injecting it
     */
    public class BaseInjectionModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Bus.class).in(Singleton.class);
        }
    }

    /**
     * Tells Roboguice to bind a singleton Google flavored billing controller
     * to injections of AndroidBillingController
     */
    public class GoogleInjectionModule extends BaseInjectionModule {
        @Override
        protected void configure() {
            super.configure();
            bind(RoboBillingController.class).
                    to(GoogleBillingController.class).in(Singleton.class);
        }
    }

    /**
     * Tells Roboguice to bind a singleton Amazon flavored billing controller
     * to injections of AndroidBillingController
     */
    public class AmazonInjectionModule extends BaseInjectionModule {
        @Override
        protected void configure() {
            super.configure();
            bind(RoboBillingController.class).
                    to(AmazonBillingController.class).in(Singleton.class);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * Binds the InjectionModule to the base application injector
         */
        RoboGuice.setBaseApplicationInjector(this,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(this)).with(new AmazonInjectionModule()));

        // Inject the billing controller, and set the configuration
        RoboGuice.getInjector(this).injectMembers(this);
        billingController.setConfiguration(this);
    }

    public static String getCurrentUser() {
        return RoboBillingApplication.userId;
    }

    public static void setCurrentUser(String userId) {
        RoboBillingApplication.userId = userId;
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
