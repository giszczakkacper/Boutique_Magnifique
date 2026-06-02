package org.example;

import java.util.List;

public class StoreManagerFacade {
	private final StoreManager storeManager;

	public StoreManagerFacade(StoreManager storeManager) {
		this.storeManager = storeManager;
	}

	public void setPrice(int productID, float price) {
		storeManager.setPrice(productID, price);
	}

	public void manageSales() {
		storeManager.manageSales();
	}

	public void manageReturns() {
		storeManager.manageReturns();
	}

	public void browseProducts(ManagerFilter filter) {
		storeManager.browseProducts(filter);
	}

	public void deleteAccount(int ID) {
		storeManager.deleteAccount(ID);
	}

	public List<Product> getLowStockProducts() {
		return storeManager.fetchLowStockProducts();
	}

	public void displayTransactionData(int ID, ManagerFilter filter) {
		storeManager.displayTransactionData(ID, filter);
	}

	public void displayAccountData(int ID) {
		storeManager.displayAccountData(ID);
	}

	public void browseNotifications() {
		storeManager.executeBrowseNotifications();
	}

	public void saveDatabase() {
		storeManager.saveDatabase();
	}
}
