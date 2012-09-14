package com.cperryinc.robobilling;

import android.app.Application;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import com.squareup.otto.Bus;
import net.robotmedia.billing.GoogleBillingController;
import net.robotmedia.billing.utils.IConfiguration;
import roboguice.RoboGuice;

public abstract class RoboBillingApplication extends Application implements IConfiguration {

    /**
     * The type of billing module to use in the application.
     */
    public static enum BillingMode {
        AMAZON, GOOGLE
    }

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

    public abstract BillingMode getBillingMode();

    @Override
    public void onCreate() {
        super.onCreate();

        BillingMode billingMode = getBillingMode();
        AbstractModule module = new GoogleInjectionModule();
        if (billingMode == BillingMode.AMAZON) {
            module = new AmazonInjectionModule();
        }

        /**
         * Binds the InjectionModule to the base application injector
         */
        RoboGuice.setBaseApplicationInjector(this,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(this)).with(module));

        // Inject the billing controller, and set the configuration
        RoboGuice.getInjector(this).injectMembers(this);
        billingController.setConfiguration(this);
    }

    // TODO: move this
    public static String getCurrentUser() {
        return RoboBillingApplication.userId;
    }

    // TODO: move this
    public static void setCurrentUser(String userId) {
        RoboBillingApplication.userId = userId;
    }
}
