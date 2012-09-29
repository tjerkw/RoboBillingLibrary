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

package com.cperryinc.billing.example.auxiliary;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter used for displaying a catalog of products. If a product is
 * managed by Android Market and already purchased, then it will be
 * "grayed-out" in the list and not selectable.
 */
public class CatalogAdapter extends ArrayAdapter<String> {
	
	private CatalogEntry[] mCatalog;
	private List<String> mOwnedItems = new ArrayList<String>();

	public CatalogAdapter(Context context, CatalogEntry[] catalog) {
		super(context, android.R.layout.simple_spinner_item);
		mCatalog = catalog;
		for (CatalogEntry element : catalog) {
			add(context.getString(element.nameId));
		}
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	@Override
	public boolean areAllItemsEnabled() {
		// Return false to have the adapter call isEnabled()
		return false;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// If the item at the given list position is not purchasable, then
		// "gray out" the list item.
		View view = super.getDropDownView(position, convertView, parent);
		view.setEnabled(isEnabled(position));
		return view;
	}
	
	private boolean isPurchased(String sku) {
		for (int i = 0; i < mOwnedItems.size(); i++) {
			if (sku.equals(mOwnedItems.get(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		// If the item at the given list position is not purchasable,
		// then prevent the list item from being selected.
		CatalogEntry entry = mCatalog[position];
		if (entry.managed == CatalogEntry.Managed.MANAGED && isPurchased(entry.sku)) {
			return false;
		}
		return true;
	}

	public void setOwnedItems(List<String> ownedItems) {
		mOwnedItems = ownedItems;
		notifyDataSetChanged();
	}

}
