/*   Copyright 2012 Christopher Perry Inc.
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

public abstract class RoboBillingApplication extends Application {

    /**
     * The type of billing module to use in the application.
     */
    public static enum BillingMode {
        AMAZON, GOOGLE
    }

    private static String userId = "unknown";
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

    public abstract IConfiguration getConfiguration();

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

        IConfiguration configuration = getConfiguration();
        if (configuration == null) {
            throw new ConfigurationNotSetException();
        }
        billingController.setConfiguration(getConfiguration());
    }

    public static String getCurrentUser() {
        return RoboBillingApplication.userId;
    }

    public static void setCurrentUser(String userId) {
        RoboBillingApplication.userId = userId;
    }

    private class ConfigurationNotSetException extends RuntimeException {
        public ConfigurationNotSetException() {
            super("IConfiguration was not set. RoboBillingLibrary needs this object to work correctly.");
        }
    }
}
