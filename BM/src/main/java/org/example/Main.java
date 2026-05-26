package org.example;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static Scanner in = new Scanner(System.in);
    static String input;

    public static void main(String[] args) {

        NotificationManager nm = new NotificationManager();
        Browser browser = new Browser();
        StoreManager manager = new StoreManager(nm, browser);

        StoreManagerFacade facade = new StoreManagerFacade(manager);

        facade.setPrice(1, 499.99f);
        facade.manageSales();
        facade.getLowStockProducts();


        System.out.println("Welcome to Boutique Magnifique!");
        while(true) {
            System.out.println("Type:\n/LOGIN to log in to an already existing account\n" +
                    "/REGISTER to register a new one\n" +
                    "/MANAGER to log in as manager" +
                    "/QUIT to exit the system");
            input = in.nextLine();
            switch (input) {
                case "/LOGIN" -> loginWindow();
                case "/REGISTER" -> registerWindow();
                case "/MANAGER" -> managerWindow();
                case "/QUIT" -> System.exit(0);
            }
        }





        System.exit(0);
    }

    public static String prompt(Scanner in, String message) {
        System.out.println(message);
        String input = in.nextLine();
        if (input.toUpperCase().equals("/QUIT")) System.exit(0);
        if (input.toUpperCase().equals("/RETURN")) return null;
        if (input.charAt(0) == '/') System.out.println("Input started with / and was not a valid keyword." +
                "/QUIT and /RETURN are always available as keywords." +
                "/ at the start of an input is usable for keywords only." +
                "Returning to previous menu.");
                return null;
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
            if (Shop.getInstance().checkLoginCollision(email)) {
                System.out.println("The email is already taken.");
            } else break;
        }
        System.out.println(
                "LOGIN: " + login +
                        "\nEMAIL:" + email +
                        "\n are available"
        );

        String password, street, town, postal;
        int homeNumber;
        password = prompt(in, "Type password: ");
        if (password == null) return;
        while(true) {
            input = prompt(in, "Type homenumber:");
            try {
                homeNumber = Integer.parseInt(input);
                if (homeNumber > 0) break;
                else throw new IllegalArgumentException("bad input");;
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
        if (Shop.getInstance().registerClient(new Credentials(login, password, email,
                new Address(homeNumber, street, town, postal))))
            System.out.println("User registered successfully!");
        else
            System.out.println("Something went wrong, registration unsuccessful.");

    }

}