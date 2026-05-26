package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Cart {
	private List<CartItem> cartItems = new ArrayList<>(); //always sorted by product ID incrementally

	public int adjustAndGetProductCount(int productID) {
		// first, compares cart item count with stock item count
		// if stock count is smaller, adjusts cart count to stock count
		// cart count could have been made to disregard stock count, to have it be more "resilient" against
		// depletions and restocks, but this is simpler
		CartItem dummy = new CartItem(productID, 0);
		int index = Collections.binarySearch(cartItems, dummy, // searches for the index of the item with designated ID
				Comparator.comparingInt(CartItem::getProductID));

		if (index < 0) {
			return 0;
		}

		CartItem item = cartItems.get(index);
		Product product = Shop.getInstance().getProduct(productID);

		if (product == null || product.getCount() <= 0) {
			item.setCount(0);
			return 0;
		}

		if (item.getCount() > product.getCount()) {
			item.setCount(product.getCount());
		}

		return item.getCount();
	}

	public float calculateProductTotal(int productID) {
		int count = adjustAndGetProductCount(productID);

		if (count == 0) {
			return 0f;
		}

		Product product = Shop.getInstance().getProduct(productID);

		if (product == null || product.getCount() <= 0) {
			return 0f;
		}

		return product.getPrice() * count;
	}

	public float calculateTotal() {
		//total for the whole cart
		float total = 0f;

		for (CartItem item : cartItems) {
			total += calculateProductTotal(item.getProductID());
		}

		prune();
		return total;
	}

	public void prune() {
		//deletes entries for which count is 0
		for (int i = cartItems.size() - 1; i >= 0; i--) {
			if (cartItems.get(i).getCount() == 0) {
				cartItems.remove(i);
			}
		}
	}

	public int setProduct(int productID, int count) {
		//sets the desired count of a product in cart
		//if larger than available stock count, cart count is set to stock count instead
		//if set to 0, the cart element is deleted from the cart
		CartItem dummy = new CartItem(productID, 0);
		int index = Collections.binarySearch(cartItems, dummy,
				Comparator.comparingInt(CartItem::getProductID));

		Product product = Shop.getInstance().getProduct(productID);
		int actualCount = 0;

		if (product != null && product.getCount() > 0 && count > 0) {
			actualCount = Math.min(count, product.getCount());
		}

		if (index >= 0) {
			cartItems.get(index).setCount(actualCount);
		} else if (actualCount > 0) {
			cartItems.add(-(index + 1), new CartItem(productID, actualCount));
		}

		prune();
		return actualCount;
	}

	public void emptyCart() {
		cartItems.clear();
	}
}
