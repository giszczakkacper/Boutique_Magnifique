package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Cart {
	private List<CartItem> cartItems = new ArrayList<>();

	public List<CartItem> getItems() {
		return new ArrayList<>(cartItems);
	}

	public int adjustAndGetProductCount(int productID) {
		CartItem dummy = new CartItem(productID, 0);
		int index = Collections.binarySearch(cartItems, dummy,
				Comparator.comparingInt(CartItem::getProductID));
		if (index < 0) return 0;

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
		if (count == 0) return 0f;
		float unitPrice = Shop.getInstance().getEffectivePrice(productID);
		return unitPrice * count;
	}

	public float calculateTotal() {
		float total = 0f;
		for (CartItem item : cartItems) {
			total += calculateProductTotal(item.getProductID());
		}
		prune();
		return total;
	}

	public void prune() {
		for (int i = cartItems.size() - 1; i >= 0; i--) {
			if (cartItems.get(i).getCount() == 0) cartItems.remove(i);
		}
	}

	public int setProduct(int productID, int count) {
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

	public void display() {
		if (cartItems.isEmpty()) {
			System.out.println("Cart is empty.");
			return;
		}
		System.out.println("=== CART ===");
		for (CartItem item : cartItems) {
			Product p = Shop.getInstance().getProduct(item.getProductID());
			if (p != null) {
				float unitPrice = Shop.getInstance().getEffectivePrice(item.getProductID());
				Sale sale = Shop.getInstance().getActiveSaleForProduct(item.getProductID());
				System.out.println("Product ID: " + item.getProductID() +
						" | Quantity: " + item.getCount() +
						" | Price: " + String.format("%.2f", unitPrice) + " PLN" +
						(sale != null ? " | Regular price: " + String.format("%.2f", p.getPrice()) + " PLN" : "") +
						" | Total: " + String.format("%.2f", unitPrice * item.getCount()) + " PLN");
			}
		}
		System.out.println("TOTAL: " + String.format("%.2f", calculateTotal()) + " PLN");
		System.out.println("==============");
	}
}
