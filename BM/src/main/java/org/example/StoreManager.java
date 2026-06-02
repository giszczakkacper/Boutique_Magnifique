package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class StoreManager {
	private NotificationManager notificationManager;
	private Browser browser;

	private final String DB_FILE_NAME = "database.json";
	private final Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
					new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
			.registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
					parseLocalDate(json))
			.registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
					new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
			.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
					parseLocalDateTime(json))
			.create();
	private final Path dbFilePath;
	private DatabaseSchema database;

	private static class DatabaseSchema {
		public List<Product> inventory = new ArrayList<>();
		public List<Client> clients = new ArrayList<>();
		public List<Sale> sales = new ArrayList<>();
	}

	public StoreManager(NotificationManager notificationManager, Browser browser) {
		this.notificationManager = notificationManager;
		this.browser = browser;
		this.dbFilePath = resolveDatabasePath();
		loadDatabase();
	}

	private void loadDatabase() {
		try (FileReader reader = new FileReader(dbFilePath.toFile())) {
			database = gson.fromJson(reader, DatabaseSchema.class);
			if (database == null) {
				database = new DatabaseSchema();
			}
			if (database.inventory == null) {
				database.inventory = new ArrayList<>();
			}
			if (database.clients == null) {
				database.clients = new ArrayList<>();
			}
			if (database.sales == null) {
				database.sales = new ArrayList<>();
			}
			normalizeLoadedClients();
			Shop.getInstance().setProductList(database.inventory);
			Shop.getInstance().setClientList(database.clients);
			Shop.getInstance().setSaleList(database.sales);
			restoreTransactionsFromClients();
			System.out.println("JSON database loaded successfully.");
		} catch (Exception e) {
			System.out.println("Could not load the JSON database from " + dbFilePath + ". Creating a new empty database.");
			database = new DatabaseSchema();
			database.inventory = new ArrayList<>();
			database.clients = new ArrayList<>();
			database.sales = new ArrayList<>();
			Shop.getInstance().setProductList(database.inventory);
			Shop.getInstance().setClientList(database.clients);
			Shop.getInstance().setSaleList(database.sales);
			Shop.getInstance().getTransactions().clear();
		}

	}

	void saveDatabase() {
		syncDatabaseFromShop();
		try (FileWriter writer = new FileWriter(dbFilePath.toFile())) {
			gson.toJson(database, writer);
			System.out.println("Changes saved to " + dbFilePath.getFileName());
		} catch (IOException e) {
			System.out.println("Error writing to JSON file");
		}
	}

	public void executeSetPrice(int productID, float price) {
		Product product = Shop.getInstance().getProduct(productID);
		if (product != null) {
			Shop.getInstance().setPrice(productID, price);
			System.out.println("The product price with ID " + productID + " has been changed to " + price + " PLN.");
			saveDatabase();
			return;
		}
		System.out.println("Error: product with ID " + productID + " not found.");
	}

	public void setPrice(int productID, float price) {
		executeSetPrice(productID, price);
	}

	public void executeBrowseProducts(ManagerFilter filter) {
		browser.browseAndDisplay(filter);
	}

	public void browseProducts(ManagerFilter filter) {
		executeBrowseProducts(filter);
	}

	public void executeManageSales() {
		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.println("Sales management...");
			System.out.println("Type:\n/LIST to display sales\n" +
					"/CREATE to create a sale\n" +
					"/END to end a sale\n" +
					"/RETURN to return to main menu");
			String command = in.nextLine().trim().toUpperCase();
			switch (command) {
				case "/LIST" -> displaySales();
				case "/CREATE" -> createSaleWindow(in);
				case "/END" -> endSaleWindow(in);
				case "/RETURN" -> {
					return;
				}
				default -> System.out.println("Unknown command.");
			}
		}
	}

	public void manageSales() {
		executeManageSales();
	}

	public void executeManageReturns() {
		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.println("Type:\n/LIST to display pending returns\n" +
					"/APPROVE to approve a return\n" +
					"/DENY to deny a return\n" +
					"/RETURN to return to main menu");
			String command = in.nextLine().trim().toUpperCase();
			switch (command) {
				case "/LIST" -> displayPendingReturns();
				case "/APPROVE" -> processReturnWindow(in, true);
				case "/DENY" -> processReturnWindow(in, false);
				case "/RETURN" -> {
					return;
				}
				default -> System.out.println("Unknown command.");
			}
		}
	}

	public void manageReturns() {
		executeManageReturns();
	}

	public void executeDeleteAccount(int ID) {
		boolean removed = Shop.getInstance().deleteAccount(ID);
		if (removed) {
			saveDatabase();
			System.out.println("Account with ID " + ID + " deleted.");
		} else {
			System.out.println("No account found with ID " + ID + ".");
		}
	}

	public void deleteAccount(int ID) {
		executeDeleteAccount(ID);
	}

	public List<Product> fetchLowStockProducts() {
		return database.inventory.stream()
				.filter(shoe -> shoe.getCount() <= shoe.getLowstockthreshold())
				.collect(Collectors.toList());
	}

	public void executeDisplayTransactionData(int ID, ManagerFilter filter) {
		Transaction transaction = Shop.getInstance().getTransaction(ID);
		if (transaction == null) {
			browser.display("No transaction found for ID: " + ID);
			return;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Transaction data for ID: ").append(ID).append('\n');
		builder.append(transaction).append('\n');

		Product product = Shop.getInstance().getProduct(transaction.getProductID());
		if (product != null) {
			builder.append("Product: ").append(product).append('\n');
		}

		Client client = Shop.getInstance().getClientByID(transaction.getUserID());
		if (client != null && client.getCredentials() != null) {
			builder.append("Client: ").append(client.getCredentials().getUsername())
					.append(" (ID: ").append(client.getID()).append(")\n");
		}

		browser.display(builder.toString().trim());
	}

	public void displayTransactionData(int ID, ManagerFilter filter) {
		executeDisplayTransactionData(ID, filter);
	}

	public void executeDisplayAccountData(int ID) {
		Client client = Shop.getInstance().getClientByID(ID);
		if (client == null) {
			browser.display("No account found with ID: " + ID);
			return;
		}

		Credentials credentials = client.getCredentials();
		Address address = credentials != null ? credentials.getAddress() : null;

		StringBuilder builder = new StringBuilder();
		builder.append("Account details for ID: ").append(ID).append('\n');
		if (credentials != null) {
			builder.append("Username: ").append(credentials.getUsername()).append('\n');
			builder.append("Email: ").append(credentials.getEmail()).append('\n');
		}
		if (address != null) {
			builder.append("Address: ")
					.append(address.getHomeNumber()).append(' ')
					.append(address.getStreet()).append(", ")
					.append(address.getTown()).append(' ')
					.append(address.getPostal()).append('\n');
		}
		builder.append("Transactions: ").append(client.getTransactionList() != null ? client.getTransactionList().size() : 0);
		if (client.getTransactionList() != null && !client.getTransactionList().isEmpty()) {
			builder.append('\n').append("Transaction history:");
			for (Transaction transaction : client.getTransactionList()) {
				builder.append('\n').append(transaction);
			}
		}

		browser.display(builder.toString().trim());
	}

	public void displayAccountData(int ID) {
		executeDisplayAccountData(ID);
	}

	public void executeBrowseNotifications() {
		Shop.getInstance().getManagersNotificationManager().browseNotifications();
	}

	private void displaySales() {
		List<Sale> sales = Shop.getInstance().getSaleList().stream()
				.filter(sale -> sale.isActiveOn(LocalDate.now()))
				.collect(Collectors.toList());
		if (sales.isEmpty()) {
			System.out.println("No sales configured.");
			return;
		}

		System.out.println("=== SALES ===");
		for (Sale sale : sales) {
			System.out.println(sale);
		}
		System.out.println("=============");
	}

	private void createSaleWindow(Scanner in) {
		Integer productID = promptPositiveInt(in, "Type product ID:");
		if (productID == null) {
			return;
		}

		Product product = Shop.getInstance().getProduct(productID);
		if (product == null) {
			System.out.println("Error: product with ID " + productID + " not found.");
			return;
		}
		if (Shop.getInstance().isProductOnSale(productID)) {
			System.out.println("This product already has an active sale.");
			return;
		}

		Float discountPercent = promptDiscountPercent(in, "Type discount percentage (0-100):");
		if (discountPercent == null) {
			return;
		}

		Sale sale = new Sale();
		sale.setDiscountedProductID(productID);
		sale.setOriginalPrice(product.getPrice());
		sale.setDiscountedPrice(product.getPrice() * (100f - discountPercent) / 100f);
		sale.setStartDate(LocalDate.now());
		sale.setEndDate(null);
		sale.setCurrentlyActive(true);
		Shop.getInstance().getSaleList().add(sale);
		saveDatabase();
		System.out.println("Sale created for product ID " + productID + ".");
	}

	private void endSaleWindow(Scanner in) {
		Integer productID = promptPositiveInt(in, "Type product ID:");
		if (productID == null) {
			return;
		}

		Sale sale = Shop.getInstance().getActiveSaleForProduct(productID);
		if (sale == null) {
			System.out.println("No active sale found for product ID " + productID + ".");
			return;
		}

		sale.setCurrentlyActive(false);
		sale.setEndDate(LocalDate.now());
		saveDatabase();
		System.out.println("Sale ended for product ID " + productID + ".");
	}

	private void displayPendingReturns() {
		List<Transaction> pendingReturns = Shop.getInstance().getTransactions().stream()
				.filter(Transaction::isReturnPending)
				.collect(Collectors.toList());
		if (pendingReturns.isEmpty()) {
			System.out.println("No pending return requests.");
			return;
		}

		System.out.println("=== PENDING RETURNS ===");
		for (Transaction transaction : pendingReturns) {
			System.out.println(transaction);
		}
		System.out.println("=======================");
	}

	private void processReturnWindow(Scanner in, boolean approve) {
		Integer transactionID = promptPositiveInt(in, "Type transaction ID:");
		if (transactionID == null) {
			return;
		}

		Transaction transaction = Shop.getInstance().getTransaction(transactionID);
		if (transaction == null) {
			System.out.println("No transaction found with ID " + transactionID + ".");
			return;
		}
		if (!transaction.isReturnPending()) {
			System.out.println("Transaction #" + transactionID + " is not waiting for approval.");
			return;
		}

		boolean processed = approve ? transaction.approveReturn() : transaction.denyReturn();
		if (!processed) {
			System.out.println("The return request could not be processed.");
			return;
		}

		if (approve) {
			Product product = Shop.getInstance().getProduct(transaction.getProductID());
			if (product != null) {
				product.increaseCount(1);
			}
		}

		Client client = Shop.getInstance().getClientByID(transaction.getUserID());
		if (client != null && client.getNotificationManager() != null) {
			client.getNotificationManager().addNotification(
					approve
							? "Your return request for transaction #" + transactionID + " was approved."
							: "Your return request for transaction #" + transactionID + " was denied."
			);
		}

		saveDatabase();
		System.out.println(approve ? "Return request approved." : "Return request denied.");
	}

	private Integer promptPositiveInt(Scanner in, String message) {
		while (true) {
			System.out.println(message);
			String value = in.nextLine().trim();
			String command = value.toUpperCase();
			if ("/QUIT".equals(command)) {
				System.exit(0);
			}
			if ("/RETURN".equals(command)) {
				return null;
			}
			try {
				int parsed = Integer.parseInt(value);
				if (parsed > 0) {
					return parsed;
				}
			} catch (NumberFormatException ignored) {
			}
			System.out.println("Value must be a positive integer.");
		}
	}

	private Float promptDiscountPercent(Scanner in, String message) {
		while (true) {
			System.out.println(message);
			String value = in.nextLine().trim();
			String command = value.toUpperCase();
			if ("/QUIT".equals(command)) {
				System.exit(0);
			}
			if ("/RETURN".equals(command)) {
				return null;
			}
			try {
				float parsed = Float.parseFloat(value);
				if (parsed > 0f && parsed <= 100f) {
					return parsed;
				}
			} catch (NumberFormatException ignored) {
			}
			System.out.println("Value must be a number between 0 and 100.");
		}
	}

	private static LocalDate parseLocalDate(JsonElement json) throws JsonParseException {
		if (json == null || json.isJsonNull()) {
			return null;
		}
		return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private static LocalDateTime parseLocalDateTime(JsonElement json) throws JsonParseException {
		if (json == null || json.isJsonNull()) {
			return null;
		}
		return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	private void syncDatabaseFromShop() {
		if (database == null) {
			database = new DatabaseSchema();
		}
		database.inventory = new ArrayList<>(Shop.getInstance().getProductList());
		database.clients = new ArrayList<>(Shop.getInstance().getClientList());
		database.sales = new ArrayList<>(Shop.getInstance().getSaleList());
	}

	private void restoreTransactionsFromClients() {
		Shop.getInstance().getTransactions().clear();
		for (Client client : database.clients) {
			if (client.getTransactionList() != null) {
				Shop.getInstance().getTransactions().addAll(client.getTransactionList());
			}
		}
	}

	private void normalizeLoadedClients() {
		for (Client client : database.clients) {
			if (client.getCart() == null) {
				client.setCart(new Cart());
			}
			if (client.getNotificationManager() == null) {
				client.setNotificationManager(new NotificationManager());
			}
			if (client.getBrowser() == null) {
				client.setBrowser(new Browser());
			}
			if (client.getTransactionList() == null) {
				client.setTransactionList(new ArrayList<>());
			}
		}
	}

	private static Path resolveDatabasePath() {
		Path current = Paths.get("").toAbsolutePath();
		while (current != null) {
			Path direct = current.resolve("database.json");
			if (Files.exists(direct)) {
				return direct;
			}

			Path nested = current.resolve("BM").resolve("database.json");
			if (Files.exists(nested)) {
				return nested;
			}

			current = current.getParent();
		}

		return Paths.get("database.json").toAbsolutePath();
	}
}
