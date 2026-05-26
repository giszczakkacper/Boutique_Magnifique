package org.example;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String input;
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



    }
}