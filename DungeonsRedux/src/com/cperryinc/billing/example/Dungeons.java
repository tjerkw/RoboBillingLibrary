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

package com.cperryinc.billing.example;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import com.cperryinc.billing.dungeons.redux.R;
import com.cperryinc.billing.example.auxiliary.CatalogAdapter;
import com.cperryinc.billing.example.auxiliary.CatalogEntry;
import com.cperryinc.billing.example.auxiliary.CatalogEntry.Managed;
import com.cperryinc.robobilling.event.PurchaseStateChangeEvent;
import com.cperryinc.robobilling.helper.RoboBillingFragmentActivity;
import net.robotmedia.billing.model.Transaction;
import net.robotmedia.billing.model.Transaction.PurchaseState;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.List;

/**
 * A sample application based on the original Dungeons to demonstrate how to use
 * BillingController and implement IBillingObserver.
 */
public class Dungeons extends RoboBillingFragmentActivity {
    private static final int DIALOG_BILLING_NOT_SUPPORTED_ID = 2;
    private static final String TAG = "Dungeons";

    @InjectView(R.id.buy_button) private Button buyButton;
    @InjectView(R.id.item_choices) private Spinner selectItemSpinner;
    @InjectView(R.id.owned_items) private ListView ownedItemsTable;

    private CatalogEntry selectedItem;
    private CatalogAdapter catalogAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        buyButton.setEnabled(false);
        buyButton.setOnClickListener(new PurchaseRequester());

        catalogAdapter = new CatalogAdapter(this, CatalogEntry.CATALOG);
        selectItemSpinner.setAdapter(catalogAdapter);
        selectItemSpinner.setOnItemSelectedListener(new SelectedItemTracker());

        updateOwnedItems();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_BILLING_NOT_SUPPORTED_ID:
                return createDialog(R.string.billing_not_supported_title, R.string.billing_not_supported_message);
            default:
                return null;
        }
    }

    @Override
    public void onPurchaseStateChanged(PurchaseStateChangeEvent event) {
        Log.i(TAG, "onPurchaseStateChanged() itemId: " + event.getProductId());
        updateOwnedItems();
    }

    @Override
    public void onBillingChecked(boolean supported) {
        if (supported) {
            buyButton.setEnabled(true);
        } else {
            showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
        }
    }

    @Override
    public void onSubscriptionChecked(boolean supported) {
    }

    private void updateOwnedItems() {
        List<Transaction> transactions = billingController.getTransactions();
        final ArrayList<String> ownedItems = new ArrayList<String>();
        for (Transaction t : transactions) {
            if (t.purchaseState == PurchaseState.PURCHASED) {
                ownedItems.add(t.productId);
            }
        }

        catalogAdapter.setOwnedItems(ownedItems);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_row, R.id.item_name, ownedItems);
        ownedItemsTable.setAdapter(adapter);
    }

    private Dialog createDialog(int titleId, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleId).setIcon(android.R.drawable.stat_sys_warning).setMessage(messageId).setCancelable(
                false).setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }

    private class PurchaseRequester implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (selectedItem.managed != Managed.SUBSCRIPTION) {
                Log.v(TAG, "Requesting purchase for sku: " + selectedItem.sku);
                requestPurchase(selectedItem.sku);
            } else {
                Log.v(TAG, "Requesting subscription for sku: " + selectedItem.sku);
                requestSubscription(selectedItem.sku);
            }
        }
    }

    private class SelectedItemTracker implements OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedItem = CatalogEntry.CATALOG[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}