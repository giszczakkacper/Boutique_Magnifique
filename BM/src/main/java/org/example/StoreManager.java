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
			System.out.println("Baza danych JSON wczytana pomyślnie.");
		} catch (Exception e) {
			System.out.println("Nie znaleziono pliku JSON. Tworzę nową, pustą bazę.");
			database = new DatabaseSchema();
		}
	}

	private void saveDatabase() {
		try (FileWriter writer = new FileWriter(DB_FILE_PATH)) {
			gson.toJson(database, writer);
			System.out.println("Zmiany zapisane do database.json");
		} catch (IOException e) {
			System.out.println("Błąd zapisu do pliku JSON!");
		}
	}

	public void executeSetPrice(int productID, float price) {
		for (Product product : database.inventory) {
			if (product.getID() == productID) {
				product.setPrice(price);
				System.out.println("Zmieniono cenę produktu " + productID + " na " + price);

				saveDatabase();
				return;
			}
		}
		System.out.println("Błąd: Nie znaleziono butów o ID " + productID);
	}

	public void executeManageSales() {
		System.out.println("Zarządzanie wyprzedażami...");
	}

	public void executeDeleteAccount(int ID) {
		boolean removed = database.clients.removeIf(client -> client.getID() == ID);
		if (removed) {
			System.out.println("Usunięto konto o ID: " + ID);
			saveDatabase();
		} else {
			System.out.println("Nie znaleziono klienta o ID " + ID);
		}
	}

	public List<Product> fetchLowStockProducts() {
		return database.inventory.stream()
				.filter(shoe -> shoe.getCount() <= shoe.getLowstockthreshold())
				.collect(Collectors.toList());
	}

	public void executeDisplayTransactionData(int ID, ManagerFilter filter) {
		browser.display("Dane transakcji dla ID: " + ID);
	}

	public void executeDisplayAccountData(int ID) {
		browser.display("Dane konta dla ID: " + ID);
	}

	public void executeBrowseNotifications() {
		notificationManager.browseNotifications();
	}
}