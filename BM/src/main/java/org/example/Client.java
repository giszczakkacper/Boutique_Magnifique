package org.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client {
	private transient Cart cart;
	private Integer ID;
	private Credentials credentials;
	private NotificationManager notificationManager;
	private List<Transaction> transactionList = new ArrayList<>();
	private transient Browser browser;
	private String salt;
	private String passwordHash;

	public String getSalt() { return salt; }
	public String getPasswordHash() { return passwordHash; }

	public Client(int ID, String password, Credentials credentials) throws NoSuchAlgorithmException {
		this.ID = ID;
		this.cart = new Cart();
		this.notificationManager = new NotificationManager();
		this.salt = generateSalt();
		password = password + salt;
		passwordHash = hash(password);
		this.credentials = credentials;
	}

	public Credentials getCredetials() {
		return credentials;
	}

	public void returnProduct(int transactionID) {
		Transaction transaction = findTransactionById(transactionID);
		if (transaction == null) {
			System.out.println("Invalid transaction ID.");
			return;
		}
		boolean returnRequested = transaction.markForReturn();
		if (returnRequested) {
			Shop.getInstance().notifyManager(
					LocalDateTime.now(),
					"Client " + getID() + " submitted a return request for transaction #" + transaction.getTransactionID() + " (product ID " + transaction.getProductID() + ")"
			);
			new InAppNotificationSender(this).send(
					new Notification(LocalDateTime.now(), "Your return request for transaction #" + transaction.getTransactionID() + " has been submitted.")
			);
			System.out.println("Return request submitted.");
		} else {
			System.out.println("A return for this transaction is not possible (already submitted or processed).");
		}
	}

	public void returnProduct(Transaction transaction) {
		if (transaction == null) {
			System.out.println("Invalid transaction.");
			return;
		}
		returnProduct(transaction.getTransactionID());
	}

	public void addToCart(int productID, int count) {
		if (cart == null) cart = new Cart();
		int addedCount = cart.setProduct(productID, count);
		if (addedCount == 0) {
			System.out.println("The product could not be added to the cart.");
		} else {
			System.out.println("Product " + productID + " added to the cart (quantity: " + addedCount + ").");
		}
	}

	public void addToCart(Product product) {
		if (product == null) {
			System.out.println("The product could not be added to the cart.");
			return;
		}
		addToCart(product.getID(), 1);
	}

	public void buyCart() {
		if (cart == null || cart.calculateTotal() == 0f) {
			System.out.println("The cart is empty.");
			return;
		}

		float total = cart.calculateTotal();

		List<CartItem> snapshot = cart.getItems();
		if (snapshot.isEmpty()) {
			System.out.println("The cart is empty after verifying stock levels.");
			return;
		}

		for (CartItem item : snapshot) {
			Product product = Shop.getInstance().getProduct(item.getProductID());
			if (product == null) continue;

			for (int i = 0; i < item.getCount(); i++) {
				Transaction t = new Transaction(
						Shop.getInstance().nextTransactionID(),
						LocalDateTime.now().toLocalDate(),
						item.getProductID(),
						this.ID
				);
				Shop.getInstance().addTransaction(t);
				transactionList.add(t);
			}

			product.decreaseCount(item.getCount());
		}

		cart.emptyCart();
		System.out.println("Purchase completed successfully! Total amount: " + String.format("%.2f", total) + " PLN");
		System.out.println("Number of transactions: " + snapshot.stream().mapToInt(CartItem::getCount).sum());

		new InAppNotificationSender(this).send(
				new Notification(LocalDateTime.now(), "Your purchase worth " + String.format("%.2f", total) + " PLN has been completed.")
		);
		Shop.getInstance().notifyManager(
				LocalDateTime.now(),
				"Client " + getID() + " purchased " + snapshot.stream().mapToInt(CartItem::getCount).sum() +
						" item(s) for " + String.format("%.2f", total) + " PLN"
		);
	}

	public void changeCredentials() {
		Scanner in = new Scanner(System.in);
		System.out.println("=== CHANGE DETAILS ===");
		System.out.println("What would you like to change?");
		System.out.println("1 - Password\n2 - Email\n3 - Address\n0 - Cancel");
		String choice = in.nextLine().trim();

		switch (choice) {
			case "1" -> {
				System.out.println("Enter a new password:");
				String newPass = in.nextLine();
				if (newPass.isBlank()) { System.out.println("Cancelled."); return; }
				try {
					this.salt = generateSalt();
					this.passwordHash = hash(newPass + salt);
					System.out.println("Password changed.");
				} catch (NoSuchAlgorithmException e) {
					System.out.println("An error occurred while changing the password.");
				}
			}
			case "2" -> {
				System.out.println("Enter a new email address:");
				String newEmail = in.nextLine().trim();
				if (newEmail.isBlank()) { System.out.println("Cancelled."); return; }
				if (Shop.getInstance().checkEmailCollision(newEmail)) {
					System.out.println("This email address is already in use.");
					return;
				}
				credentials.setEmail(newEmail);
				System.out.println("Email changed.");
			}
			case "3" -> {
				System.out.println("House number:");
				String hn = in.nextLine().trim();
				System.out.println("Street:");
				String street = in.nextLine().trim();
				System.out.println("City:");
				String town = in.nextLine().trim();
				System.out.println("Postal code:");
				String postal = in.nextLine().trim();
				try {
					int homeNumber = Integer.parseInt(hn);
					credentials.getAddress().setHomeNumber(homeNumber);
					credentials.getAddress().setStreet(street);
					credentials.getAddress().setTown(town);
					credentials.getAddress().setPostal(postal);
					System.out.println("Address changed.");
				} catch (NumberFormatException e) {
					System.out.println("Invalid house number.");
				}
			}
			default -> System.out.println("Cancelled.");
		}
	}

	public Cart getCart() { return cart; }
	public void setCart(Cart cart) { this.cart = cart; }
	public Integer getID() { return ID; }
	public void setID(Integer ID) { this.ID = ID; }
	public Credentials getCredentials() { return credentials; }
	public void setCredentials(Credentials credentials) { this.credentials = credentials; }
	public NotificationManager getNotificationManager() { return notificationManager; }
	public void setNotificationManager(NotificationManager notificationManager) { this.notificationManager = notificationManager; }
	public Browser getBrowser() { return browser; }
	public void setBrowser(Browser browser) { this.browser = browser; }
	public List<Transaction> getTransactionList() { return transactionList; }
	public void setTransactionList(List<Transaction> transactionList) { this.transactionList = transactionList; }

	public List<Integer> getTransactionIDs() {
		List<Integer> transactionIDs = new ArrayList<>();
		if (transactionList == null) {
			return transactionIDs;
		}
		for (Transaction transaction : transactionList) {
			transactionIDs.add(transaction.getTransactionID());
		}
		return transactionIDs;
	}

	public void setTransactionIDs(List<Integer> transactionIDs) {
		List<Transaction> resolvedTransactions = new ArrayList<>();
		if (transactionIDs != null) {
			for (Integer transactionID : transactionIDs) {
				Transaction transaction = Shop.getInstance().getTransaction(transactionID);
				if (transaction != null) {
					resolvedTransactions.add(transaction);
				}
			}
		}
		this.transactionList = resolvedTransactions;
	}

	private Transaction findTransactionById(int transactionID) {
		if (transactionList == null) {
			return null;
		}
		for (Transaction transaction : transactionList) {
			if (transaction.getTransactionID() == transactionID) {
				return transaction;
			}
		}
		return null;
	}

	private static String generateSalt() {
		Random random = new Random();
		StringBuilder salt = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			salt.append((char) (random.nextInt(94) + 33));
		}
		return salt.toString();
	}

	static String hash(String input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashed = md.digest(input.getBytes());
		StringBuilder sb = new StringBuilder();
		for (byte b : hashed) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
