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

package com.ensolabs.billing.example.auxiliary;

import com.ensolabs.billing.dungeons.redux.R;

public class CatalogEntry {

    /**
     * Each product in the catalog can be MANAGED, UNMANAGED, or SUBSCRIPTION.
     * <p/>
     * MANAGED means that the product can be purchased only once per user (such
     * as a new level in a game). The purchase is remembered by Android Market
     * and can be restored if this application is uninstalled and then
     * re-installed.
     * <p/>
     * UNMANAGED is used for products that can be used up and
     * purchased multiple times (such as poker chips). It is up to the
     * application to keep track of UNMANAGED products for the user.
     * <p/>
     * SUBSCRIPTION is just like MANAGED except that the user gets charged
     * monthly or yearly.
     * <p/>
     * The analogous product types for Amazon are ENTITLED, CONSUMABLE, or SUBSCRIPTION.
     *
     * @see {@link /DungeonsRedux/amazon.sdktester.json} for Amazon test values, which reflect a subset of the values here.
     */
    public enum Managed {
        MANAGED, UNMANAGED, SUBSCRIPTION
    }

    public String sku;
    public int nameId;
    public Managed managed;

    public CatalogEntry(String sku, int nameId, Managed managed) {
        this.sku = sku;
        this.nameId = nameId;
        this.managed = managed;
    }

    /**
     * An array of product list entries for the products that can be purchased.
     */
    public static final CatalogEntry[] CATALOG = new CatalogEntry[]{
            /**
             * These values are used to test both Google and Amazon flavors
             */
            new CatalogEntry("sword_001", R.string.two_handed_sword, Managed.MANAGED),
            new CatalogEntry("potion_001", R.string.potions, Managed.UNMANAGED),
            new CatalogEntry("subscription_type_1.monthly", R.string.subscription1_monthly, Managed.SUBSCRIPTION),
            new CatalogEntry("subscription_type_1.yearly", R.string.subscription1_yearly, Managed.SUBSCRIPTION),
            new CatalogEntry("subscription_type_2.monthly", R.string.subscription2_monthly, Managed.SUBSCRIPTION),

            /**
             * These values are specific to testing Google
             */
            new CatalogEntry("android.test.purchased", R.string.android_test_purchased, Managed.UNMANAGED),
            new CatalogEntry("android.test.canceled", R.string.android_test_canceled, Managed.UNMANAGED),
            new CatalogEntry("android.test.refunded", R.string.android_test_refunded, Managed.UNMANAGED),
            new CatalogEntry("android.test.item_unavailable", R.string.android_test_item_unavailable, Managed.UNMANAGED),};

}
