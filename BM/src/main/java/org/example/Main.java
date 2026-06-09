package org.example;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static Scanner in = new Scanner(System.in);
    static String input;
    static Browser browser;
    static StoreManagerFacade managerFacade;

    public static void main(String[] args) {

        browser = new Browser();
        StoreManager manager = new StoreManager(browser);

        StoreManagerFacade facade = new StoreManagerFacade(manager);
        managerFacade = facade;

        System.out.println("Welcome to Boutique Magnifique!");
        while (true) {
            System.out.println("Type:\n/LOGIN to log in to an already existing account\n" +
                    "/REGISTER to register a new one\n" +
                    "/MANAGER to log in as manager\n" +
                    "/QUIT to exit the system");
            input = in.nextLine().toUpperCase();
            switch (input) {
                case "/LOGIN" -> loginWindow();
                case "/REGISTER" -> registerWindow();
                case "/MANAGER" -> managerWindow();
                case "/QUIT" -> System.exit(0);
                default -> System.out.println("Unknown command.");
            }
        }
    }

    public static String prompt(Scanner in, String message) {
        System.out.println(message);
        String input = in.nextLine();
        String command = input.toUpperCase();

        if (command.equals("/QUIT")) System.exit(0);
        if (command.equals("/RETURN")) return null;
        if (!input.isEmpty() && input.charAt(0) == '/') {
            System.out.println("Input started with / and was not a valid keyword.\n" +
                    "/QUIT and /RETURN are always available as keywords.\n" +
                    "/ at the start of an input is usable for keywords only.\n" +
                    "Returning to previous menu.");
            return null;
        }

        return input;
    }

    public static void registerWindow() {
        String login;
        String email;

        while (true) {
            login = prompt(in, "Type login for new user:");
            if (login == null) return;
            if (Shop.getInstance().checkLoginCollision(login)) {
                System.out.println("The username is already taken.");
            } else break;
        }

        while (true) {
            email = prompt(in, "Type email for new user:");
            if (email == null) return;
            if (Shop.getInstance().checkEmailCollision(email)) {
                System.out.println("The email is already taken.");
            } else break;
        }

        System.out.println(
                "LOGIN: " + login +
                        "\nEMAIL: " + email +
                        "\nare available"
        );

        String password;
        String street;
        String town;
        String postal;
        int homeNumber;

        password = prompt(in, "Type password: ");
        if (password == null) return;

        while (true) {
            input = prompt(in, "Type homenumber:");
            if (input == null) return;
            try {
                homeNumber = Integer.parseInt(input);
                if (homeNumber > 0) break;
                throw new IllegalArgumentException("bad input");
            } catch (IllegalArgumentException e) {
                System.out.println("Home number must be a positive integer.");
            }
        }

        street = prompt(in, "Type street: ");
        if (street == null) return;
        town = prompt(in, "Type town: ");
        if (town == null) return;
        postal = prompt(in, "Type postal: ");
        if (postal == null) return;

        try {
            Credentials credentials = new Credentials(login, email, new Address(homeNumber, street, town, postal));
            if (Shop.getInstance().registerClient(password, credentials)) {
                System.out.println("User registered successfully!");
                managerFacade.saveDatabase();
            } else {
                System.out.println("Something went wrong, registration unsuccessful.");
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Something went wrong while securing the password.");
        }
    }

    public static void loginWindow() {
        String login = prompt(in, "Type login:");
        if (login == null) return;
        String password = prompt(in, "Type password:");
        if (password == null) return;

        try {
            Client client = Shop.getInstance().loginClient(login, password);
            if (client == null) {
                System.out.println("Invalid credentials.");
                return;
            }
            clientWindow(client);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Something went wrong while checking the password.");
        }
    }

    public static void managerWindow() {
        System.out.println("Welcome, manager!");
        while (true) {
            System.out.println("Type:\n/PRICE to set product price\n" +
                    "/BROWSE to browse products and apply filters\n" +
                    "/SALES to manage sales\n" +
                    "/RETURNS to manage return requests\n" +
                    "/LOWSTOCK to show low stock products\n" +
                    "/DELETE to delete client account\n" +
                    "/TRANSACTION to display transaction data\n" +
                    "/ACCOUNT to display account data\n" +
                    "/NOTIFICATIONS to browse notifications\n" +
                    "/RETURN to return to main menu\n" +
                    "/QUIT to exit the system");
            input = in.nextLine().toUpperCase();
            switch (input) {
                case "/PRICE" -> setPriceWindow();
                case "/BROWSE" -> managerBrowseWindow();
                case "/SALES" -> managerFacade.manageSales();
                case "/RETURNS" -> managerFacade.manageReturns();
                case "/LOWSTOCK" -> lowStockWindow();
                case "/DELETE" -> deleteAccountWindow();
                case "/TRANSACTION" -> transactionDataWindow();
                case "/ACCOUNT" -> accountDataWindow();
                case "/NOTIFICATIONS" -> managerFacade.browseNotifications();
                case "/RETURN" -> {
                    return;
                }
                case "/QUIT" -> System.exit(0);
                default -> System.out.println("Unknown command.");
            }
        }
    }

    public static void clientWindow(Client client) {
        ClientFacade clientFacade = new ClientFacade(client);

        while (true) {
            System.out.println("Type:\n/BROWSE to browse products and apply filters\n" +
                    "/CART to add product to cart\n" +
                    "/BUY to buy cart\n" +
                    "/RETURNPRODUCT to return product\n" +
                    "/CREDENTIALS to change credentials\n" +
                    "/NOTIFICATIONS to browse notifications\n" +
                    "/RETURN to return to main menu\n" +
                    "/QUIT to exit the system");
            input = in.nextLine().toUpperCase();
            switch (input) {
                case "/BROWSE" -> clientBrowseWindow(clientFacade);
                case "/CART" -> addToCartWindow(clientFacade);
                case "/BUY" -> {
                    clientFacade.buyCart();
                    managerFacade.saveDatabase();
                }
                case "/RETURNPRODUCT" -> {
                    returnProductWindow(clientFacade);
                    managerFacade.saveDatabase();
                }
                case "/CREDENTIALS" -> {
                    clientFacade.changeCredentials();
                    managerFacade.saveDatabase();
                }
                case "/NOTIFICATIONS" -> clientFacade.browseNotifications();
                case "/RETURN" -> {
                    return;
                }
                case "/QUIT" -> System.exit(0);
                default -> System.out.println("Unknown command.");
            }
        }
    }

    private static void addToCartWindow(ClientFacade clientFacade) {
        Integer productID = promptPositiveInt("Type product ID:");
        if (productID == null) return;
        Integer count = promptPositiveInt("Type product count:");
        if (count == null) return;

        clientFacade.addToCart(productID, count);
    }

    private static void returnProductWindow(ClientFacade clientFacade) {
        Integer transactionID = promptPositiveInt("Type transaction ID:");
        if (transactionID == null) return;

        clientFacade.returnProduct(transactionID);
    }

    private static void setPriceWindow() {
        Integer productID = promptPositiveInt("Type product ID:");
        if (productID == null) return;
        Float price = promptPositiveFloat("Type new price:");
        if (price == null) return;

        managerFacade.setPrice(productID, price);
    }

    private static void lowStockWindow() {
        List<Product> products = managerFacade.getLowStockProducts();
        if (products.isEmpty()) {
            System.out.println("No low stock products.");
            return;
        }

        System.out.println("Low stock products:");
        for (Product product : products) {
            System.out.println(product);
        }
    }

    private static void deleteAccountWindow() {
        Integer clientID = promptNonNegativeInt("Type client ID:");
        if (clientID == null) return;

        managerFacade.deleteAccount(clientID);
    }

    private static void transactionDataWindow() {
        Integer transactionID = promptPositiveInt("Type transaction ID:");
        if (transactionID == null) return;

        managerFacade.displayTransactionData(transactionID, new ManagerFilter());
    }

    private static void accountDataWindow() {
        Integer clientID = promptNonNegativeInt("Type client ID:");
        if (clientID == null) return;

        managerFacade.displayAccountData(clientID);
    }

    private static Integer promptPositiveInt(String message) {
        while (true) {
            String value = prompt(in, message);
            if (value == null) return null;
            try {
                int parsed = Integer.parseInt(value);
                if (parsed > 0) return parsed;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Value must be a positive integer.");
        }
    }

    private static Integer promptNonNegativeInt(String message) {
        while (true) {
            String value = prompt(in, message);
            if (value == null) return null;
            try {
                int parsed = Integer.parseInt(value);
                if (parsed >= 0) return parsed;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Value must be a non-negative integer.");
        }
    }

    private static Float promptPositiveFloat(String message) {
        while (true) {
            String value = prompt(in, message);
            if (value == null) return null;
            try {
                float parsed = Float.parseFloat(value);
                if (parsed > 0) return parsed;
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Value must be a positive number.");
        }
    }

    private static void managerBrowseWindow() {
        ManagerFilter filter = new ManagerFilter();
        while (true) {
            System.out.println("Browse menu:");
            System.out.println("/FILTERS - configure filters");
            System.out.println("/BROWSE - browse products with current filters");
            System.out.println("/CLEAR - reset all filters");
            System.out.println("/SHOW - show current filter settings");
            System.out.println("/RETURN - go back");
            System.out.println("/QUIT - exit the system");
            String line = in.nextLine().toUpperCase().trim();
            switch (line) {
                case "/FILTERS" -> configureFilter(filter);
                case "/BROWSE" -> browser.browse(filter);
                case "/CLEAR" -> {
                    filter = new ManagerFilter();
                    System.out.println("Filters reset.");
                }
                case "/SHOW" -> showFilter(filter);
                case "/RETURN" -> { return; }
                case "/QUIT" -> System.exit(0);
                default -> System.out.println("Unknown command.");
            }
        }
    }

    private static void clientBrowseWindow(ClientFacade clientFacade) {
        ClientFilter filter = new ClientFilter();
        while (true) {
            System.out.println("Browse menu:");
            System.out.println("/FILTERS - configure filters");
            System.out.println("/BROWSE - browse products with current filters");
            System.out.println("/CLEAR - reset all filters");
            System.out.println("/SHOW - show current filter settings");
            System.out.println("/RETURN - go back");
            System.out.println("/QUIT - exit the system");
            String line = in.nextLine().toUpperCase().trim();
            switch (line) {
                case "/FILTERS" -> configureFilter(filter);
                case "/BROWSE" -> clientFacade.browse(filter);
                case "/CLEAR" -> {
                    filter = new ClientFilter();
                    System.out.println("Filters reset.");
                }
                case "/SHOW" -> showFilter(filter);
                case "/RETURN" -> { return; }
                case "/QUIT" -> System.exit(0);
                default -> System.out.println("Unknown command.");
            }
        }
    }

    private static void configureFilter(Filter filter) {
        while (true) {
            System.out.println("Filter options:");
            System.out.println("/PRICE_LOWER <amount>");
            System.out.println("/PRICE_UPPER <amount>");
            System.out.println("/SIZE_MIN <size>");
            System.out.println("/SIZE_MAX <size>");
            System.out.println("/COLOR <color[,color...]> (ANY/CLEAR resets)");
            System.out.println("/ONSALE - toggle on-sale only");
            System.out.println("/SECONDHAND - toggle second-hand only");
            if (filter instanceof ManagerFilter) {
                System.out.println("/LOWSTOCK <count> - set low stock threshold");
            }
            System.out.println("/CLEAR - reset filters");
            System.out.println("/SHOW - current settings");
            System.out.println("/DONE - finish");
            System.out.println("/RETURN - go back");
            System.out.println("/QUIT - exit");

            String raw = in.nextLine();
            String upper = raw.toUpperCase().trim();

            if (upper.equals("/DONE") || upper.equals("/RETURN")) return;
            if (upper.equals("/QUIT")) System.exit(0);

            if (upper.equals("/CLEAR")) {
                filter.setLowerBracket(0f);
                filter.setUpperBracket(Float.MAX_VALUE);
                filter.setSmallestSize(0);
                filter.setBiggestSize(Integer.MAX_VALUE);
                filter.clearAllowedColors();
                filter.setOnSale(false);
                filter.setDisplaySecondHand(false);
                if (filter instanceof ManagerFilter mf) {
                    mf.setLowstockthreshold(0);
                }
                System.out.println("Filters reset.");
                continue;
            }

            if (upper.equals("/SHOW")) {
                showFilter(filter);
                continue;
            }

            if (upper.equals("/ONSALE")) {
                filter.setOnSale(!filter.isOnSale());
                System.out.println("On-sale filter: " + (filter.isOnSale() ? "ON" : "OFF"));
                continue;
            }

            if (upper.equals("/SECONDHAND")) {
                filter.setDisplaySecondHand(!filter.isDisplaySecondHand());
                System.out.println("Second-hand filter: " + (filter.isDisplaySecondHand() ? "ON" : "OFF"));
                continue;
            }

            String[] parts = raw.trim().split("\\s+", 2);
            if (parts.length < 2) {
                System.out.println("This command requires an argument.");
                continue;
            }
            String cmd = parts[0].toUpperCase();
            String arg = parts[1].trim();

            switch (cmd) {
                case "/PRICE_LOWER" -> {
                    try {
                        filter.setLowerBracket(Float.parseFloat(arg));
                        System.out.println("Min price set to " + arg);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                    }
                }
                case "/PRICE_UPPER" -> {
                    try {
                        filter.setUpperBracket(Float.parseFloat(arg));
                        System.out.println("Max price set to " + arg);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                    }
                }
                case "/SIZE_MIN" -> {
                    try {
                        filter.setSmallestSize(Integer.parseInt(arg));
                        System.out.println("Min size set to " + arg);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                    }
                }
                case "/SIZE_MAX" -> {
                    try {
                        filter.setBiggestSize(Integer.parseInt(arg));
                        System.out.println("Max size set to " + arg);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number.");
                    }
                }
                case "/LOWSTOCK" -> {
                    if (filter instanceof ManagerFilter mf) {
                        try {
                            mf.setLowstockthreshold(Integer.parseInt(arg));
                            System.out.println("Low stock threshold set to " + arg);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number.");
                        }
                    } else {
                        System.out.println("Unknown filter command.");
                    }
                }
                case "/COLOR" -> {
                    if (arg.equalsIgnoreCase("ANY") || arg.equalsIgnoreCase("CLEAR") || arg.equalsIgnoreCase("NONE")) {
                        filter.clearAllowedColors();
                        System.out.println("Color filter cleared.");
                        continue;
                    }

                    List<Color> colors = new ArrayList<>();
                    boolean invalid = false;
                    for (String token : arg.split(",")) {
                        String colorText = token.trim();
                        if (colorText.isEmpty()) {
                            continue;
                        }
                        Color color = parseColor(colorText.toUpperCase());
                        if (color == null) {
                            invalid = true;
                            break;
                        }
                        if (!colors.contains(color)) {
                            colors.add(color);
                        }
                    }

                    if (!invalid && !colors.isEmpty()) {
                        filter.setAllowedColors(colors);
                        System.out.println("Allowed colors set to: " + String.join(", ", colors.stream().map(Color::name).toList()));
                    } else {
                        System.out.println("Unknown color. Available: CZARNE, BIALE, CZERWONE, ZIELONE, POMARANCZOWE, NIEBIESKIE, FIOLETOWE, BRAZOWE");
                    }
                }
                default -> System.out.println("Unknown filter command.");
            }
        }
    }

    private static void showFilter(Filter filter) {
        System.out.println("=== Current Filters ===");
        System.out.println("Price: " + (filter.getLowerBracket() > 0f ? "from " + filter.getLowerBracket() : "any")
                + " - " + (filter.getUpperBracket() < Float.MAX_VALUE ? filter.getUpperBracket() : "any"));
        System.out.println("Size:  " + (filter.getSmallestSize() > 0 ? "from " + filter.getSmallestSize() : "any")
                + " - " + (filter.getBiggestSize() < Integer.MAX_VALUE ? filter.getBiggestSize() : "any"));
        List<Color> allowedColors = filter.getAllowedColors();
        System.out.println("Color: " + (allowedColors.isEmpty()
                ? "any"
                : String.join(", ", allowedColors.stream().map(Color::name).toList())));
        System.out.println("On sale: " + (filter.isOnSale() ? "YES" : "no"));
        System.out.println("Second-hand: " + (filter.isDisplaySecondHand() ? "YES" : "no"));
        if (filter instanceof ManagerFilter mf) {
            System.out.println("Low stock threshold: " + (mf.getLowstockthreshold() > 0 ? mf.getLowstockthreshold() : "none"));
        }
        System.out.println("=======================");
    }

    private static Color parseColor(String text) {
        try {
            return Color.valueOf(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
