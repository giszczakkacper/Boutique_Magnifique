package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class StoreManager {
	private NotificationManager notificationManager;
	private Browser browser;

	private final String DB_FILE_PATH = "database.json";
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private DatabaseSchema database;

	private static class DatabaseSchema {
		public List<Product> inventory = new ArrayList<>();
		public List<Client> clients = new ArrayList<>();
	}

	public StoreManager(NotificationManager notificationManager, Browser browser) {
		this.notificationManager = notificationManager;
		this.browser = browser;
		loadDatabase();
	}

	private void loadDatabase() {
		try (FileReader reader = new FileReader(DB_FILE_PATH)) {
			database = gson.fromJson(reader, DatabaseSchema.class);
			if (database == null) {
				database = new DatabaseSchema();
			}
			System.out.println("JSON database loaded successfully.");
		} catch (Exception e) {
			System.out.println("JSON file not found. Creating a new, empty database..");
			database = new DatabaseSchema();
		}
	}

	private void saveDatabase() {
		try (FileWriter writer = new FileWriter(DB_FILE_PATH)) {
			gson.toJson(database, writer);
			System.out.println("Changes saved to database.json");
		} catch (IOException e) {
			System.out.println("Error writing to JSON file");
		}
	}

	public void executeSetPrice(int productID, float price) {
		for (Product product : database.inventory) {
			if (product.getID() == productID) {
				product.setPrice(price);
				System.out.println("The product price with ID:" + productID + " has been changed to " + price);

				saveDatabase();
				return;
			}
		}
		System.out.println("Error: shoes with ID " + productID + "not found");
	}

	public void executeManageSales() {
		System.out.println("Sales management...");
	}

	public void executeDeleteAccount(int ID) {
		boolean removed = database.clients.removeIf(client -> client.getID() == ID);
		if (removed) {
			System.out.println("Account with ID " + ID + "got deleted");
			saveDatabase();
		} else {
			System.out.println("Account with ID " + ID + "not found");
		}
	}

	public List<Product> fetchLowStockProducts() {
		return database.inventory.stream()
				.filter(shoe -> shoe.getCount() <= shoe.getLowstockthreshold())
				.collect(Collectors.toList());
	}

	public void executeDisplayTransactionData(int ID, ManagerFilter filter) {
		browser.display("Transaction data for ID: " + ID);
	}

	public void executeDisplayAccountData(int ID) {
		browser.display("Account details for ID: " + ID);
	}

	public void executeBrowseNotifications() {
		notificationManager.browseNotifications();
	}
}