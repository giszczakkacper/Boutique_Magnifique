package org.example;

import java.time.LocalDate;

public class Discount extends Sale {
	public Discount() {
	}

	public Discount(LocalDate startDate, LocalDate endDate, float originalPrice, float discountedPrice, boolean currentlyActive, int discountedProductID) {
		setStartDate(startDate);
		setEndDate(endDate);
		setOriginalPrice(originalPrice);
		setDiscountedPrice(discountedPrice);
		setCurrentlyActive(currentlyActive);
		setDiscountedProductID(discountedProductID);
	}
}
