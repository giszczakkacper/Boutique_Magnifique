package org.example;

public class Product {
	private int ID;
	private float price;
	private int size;
	private int count;
	private Type type;
	private int lowstockthreshold = 5;
	private Color color;
	private transient Discount discount;

	public void setPrice(float price) { this.price = price; }
	public float getPrice() { return this.price; }
	public int getID() { return this.ID; }
	public int getCount() { return this.count; }
	public int getLowstockthreshold() { return this.lowstockthreshold; }
	public Type getType() { return this.type; }
	public Color getColor() { return this.color; }
	public int getSize() { return this.size; }

	public Discount getDiscount() {
		Sale activeSale = Shop.getInstance().getActiveSaleForProduct(ID);
		if (activeSale == null) {
			return null;
		}
		if (activeSale instanceof Discount existingDiscount) {
			discount = existingDiscount;
			return existingDiscount;
		}
		Discount mappedDiscount = new Discount(
				activeSale.getStartDate(),
				activeSale.getEndDate(),
				activeSale.getOriginalPrice(),
				activeSale.getDiscountedPrice(),
				activeSale.isCurrentlyActive(),
				activeSale.getDiscountedProductID()
		);
		discount = mappedDiscount;
		return mappedDiscount;
	}

	public void setDiscount(Discount discount) {
		this.discount = discount;
		if (discount == null) {
			return;
		}
		if (discount.getDiscountedProductID() != ID) {
			discount.setDiscountedProductID(ID);
		}

		Sale existingSale = null;
		for (Sale sale : Shop.getInstance().getSaleList()) {
			if (sale.getDiscountedProductID() == ID) {
				existingSale = sale;
				break;
			}
		}

		if (existingSale == null) {
			Shop.getInstance().getSaleList().add(discount);
		} else {
			existingSale.setStartDate(discount.getStartDate());
			existingSale.setEndDate(discount.getEndDate());
			existingSale.setOriginalPrice(discount.getOriginalPrice());
			existingSale.setDiscountedPrice(discount.getDiscountedPrice());
			existingSale.setCurrentlyActive(discount.isCurrentlyActive());
			existingSale.setDiscountedProductID(discount.getDiscountedProductID());
		}
	}

	public float getEffectivePrice() {
		Sale sale = Shop.getInstance().getActiveSaleForProduct(ID);
		return sale != null ? sale.getDiscountedPrice() : price;
	}

	public boolean isOnSale() {
		return Shop.getInstance().isProductOnSale(ID);
	}

	public void decreaseCount(int amount) {
		this.count = Math.max(0, this.count - amount);
	}

	public void increaseCount(int amount) {
		this.count = Math.max(0, this.count + amount);
	}

	@Override
	public String toString() {
		Sale sale = Shop.getInstance().getActiveSaleForProduct(ID);
		float displayPrice = sale != null ? sale.getDiscountedPrice() : price;
		StringBuilder builder = new StringBuilder();
		builder.append("ID: ").append(ID)
				.append(" | Price: ").append(String.format("%.2f", displayPrice)).append(" PLN")
				.append(" | Size: ").append(size)
				.append(" | Stock: ").append(count)
				.append(" | Type: ").append(type != null ? type : "none")
				.append(" | Color: ").append(color != null ? color : "none");
		if (sale != null) {
			builder.append(" | Regular price: ").append(String.format("%.2f", price)).append(" PLN")
					.append(" | Sale: active");
		}
		return builder.toString();
	}
}
