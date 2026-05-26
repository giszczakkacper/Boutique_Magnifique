package org.example;

import java.util.List;

public class StoreManagerFacade {

	private final StoreManager storeManager;

	public StoreManagerFacade(StoreManager storeManager) {
		this.storeManager = storeManager;
	}

	public void setPrice(int productID, float price) {
		storeManager.executeSetPrice(productID, price);
	}

	public void manageSales() {
		storeManager.executeManageSales();
	}

	public void deleteAccount(int ID) {
		storeManager.executeDeleteAccount(ID);
	}

	public List<Product> getLowStockProducts() {
		return storeManager.fetchLowStockProducts();
	}

	public void displayTransactionData(int ID, ManagerFilter filter) {
		storeManager.executeDisplayTransactionData(ID, filter);
	}

	public void displayAccountData(int ID) {
		storeManager.executeDisplayAccountData(ID);
	}

	public void browseNotifications() {
		storeManager.executeBrowseNotifications();
	}
}