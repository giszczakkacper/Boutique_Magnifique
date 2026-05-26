package org.example;


public class Product {
	private int ID;
	private float price;
	private int size;
	private int count;
	private Type type;
	private int lowstockthreshold = 5;

	public void setPrice(float price) {
		this.price = price;
	}

	public float getPrice() {
		return this.price;
	}

	public int getID() {
		return this.ID;
	}

	public int getCount() {
		return this.count;
	}

	public int getLowstockthreshold() {
		return this.lowstockthreshold;
	}
}