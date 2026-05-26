package org.example;


import java.lang.reflect.Type;

public class Product {
	private int ID;
	private float price;
	private int size;
	private int count;
	private Type type;
	private int lowstockthreshold = 5;
	private Color color;

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