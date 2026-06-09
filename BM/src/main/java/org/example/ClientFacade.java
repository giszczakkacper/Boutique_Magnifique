package org.example;

public class ClientFacade {
	Client client;
	private Browser browser = new Browser();

	public ClientFacade(Client client) {
		this.client = client;
	}

	public void returnProduct(int transactionID) {
		client.returnProduct(transactionID);
	}

	public void returnProduct(Transaction transaction) {
		if (transaction != null) {
			client.returnProduct(transaction);
		}
	}

	public void browse(ClientFilter filter) {
		browser.browse(filter);
	}

	public void addToCart(int productID, int count) {
		client.addToCart(productID, count);
	}

	public void addToCart(Product product) {
		if (product != null) {
			client.addToCart(product);
		}
	}

	public void buyCart() {
		client.getCart().display();
		System.out.println("Do you confirm? (yes/no)");
		java.util.Scanner in = new java.util.Scanner(System.in);
		String answer = in.nextLine().trim().toLowerCase();
		if (answer.equals("yes")) {
			client.buyCart();
		} else {
			System.out.println("Transaction cancelled.");
		}
	}

	public void changeCredentials() {
		client.changeCredentials();
	}

	public void browseNotifications() {
		client.getNotificationManager().browseNotifications();
	}
}
