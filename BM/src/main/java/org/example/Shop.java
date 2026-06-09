package org.example;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Shop {
	private static Shop instance = null;
	private static NotificationManager managersNotificationManager = new NotificationManager();

	private Shop() {}

	private List<Product> productList = new ArrayList<>();
	private ArrayList<Client> clientList = new ArrayList<>();
	private ArrayList<Transaction> transactions = new ArrayList<>();
	private ArrayList<Sale> saleList = new ArrayList<>();

	public static Shop getInstance() {
		if (instance == null) instance = new Shop();
		return instance;
	}

	public void setProductList(List<Product> products) { this.productList = new ArrayList<>(products); }
	public List<Product> getProductList() { return productList; }
	public void setClientList(List<Client> clients) { this.clientList = new ArrayList<>(clients); }
	public ArrayList<Client> getClientList() { return clientList; }
	public ArrayList<Transaction> getTransactions() { return transactions; }
	public ArrayList<Sale> getSaleList() { return saleList; }
	public void setSaleList(List<Sale> sales) { this.saleList = new ArrayList<>(sales); }

	public int nextTransactionID() {
		return transactions.size() + 1;
	}

	public int nextClientID() {
		return clientList.stream().mapToInt(Client::getID).max().orElse(-1) + 1;
	}

	public Sale getActiveSaleForProduct(int productID) {
		for (Sale sale : saleList) {
			if (sale.getDiscountedProductID() == productID && sale.isActiveOn(LocalDate.now())) {
				return sale;
			}
		}
		return null;
	}

	public boolean isProductOnSale(int productID) {
		return getActiveSaleForProduct(productID) != null;
	}

	public float getEffectivePrice(int productID) {
		Product product = getProduct(productID);
		if (product == null) {
			return 0f;
		}
		Sale sale = getActiveSaleForProduct(productID);
		return sale != null ? sale.getDiscountedPrice() : product.getPrice();
	}

	public boolean checkLoginCollision(String username) {
		for (Client client : clientList) {
			if (client.getCredentials().getUsername().equals(username)) return true;
		}
		return false;
	}

	public boolean checkEmailCollision(String email) {
		for (Client client : clientList) {
			if (client.getCredentials().getEmail().equals(email)) return true;
		}
		return false;
	}

	public boolean registerClient(String password, Credentials credentials) throws NoSuchAlgorithmException {
		int ID = nextClientID();
		Client createdClient = new Client(ID, password, credentials);
		clientList.add(createdClient);
		return true;
	}

	public Client loginClient(String username, String password) throws NoSuchAlgorithmException {
		for (Client client : clientList) {
			if (client.getCredentials().getUsername().equals(username)) {
				String hashed = Client.hash(password + client.getSalt());
				if (hashed.equals(client.getPasswordHash())) return client;
			}
		}
		return null;
	}

	public void notifyManager(LocalDateTime time, String message) {
		managersNotificationManager.addNotification(new Notification(time, message));
	}

	public NotificationManager getManagersNotificationManager() { return managersNotificationManager; }

	public Product getProduct(int productID) {
		for (Product product : productList) {
			if (product.getID() == productID) return product;
		}
		return null;
	}

	public void setPrice(int productID, float price) {
		Product p = getProduct(productID);
		if (p != null) {
			Sale sale = getActiveSaleForProduct(productID);
			if (sale != null && sale.getOriginalPrice() > 0f) {
				float discountRatio = sale.getDiscountedPrice() / sale.getOriginalPrice();
				sale.setOriginalPrice(price);
				sale.setDiscountedPrice(price * discountRatio);
			}
			p.setPrice(price);
		}
	}

	public boolean deleteAccount(int ID) {
		return clientList.removeIf(c -> c.getID() == ID);
	}

	public Transaction getTransaction(int transactionID) {
		for (Transaction t : transactions) {
			if (t.getTransactionID() == transactionID) return t;
		}
		return null;
	}

	public void addTransaction(Transaction t) { transactions.add(t); }

	public Client getClientByID(int ID) {
		for (Client c : clientList) {
			if (c.getID() == ID) return c;
		}
		return null;
	}
}
