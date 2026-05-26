package org.example;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

	private List<String> notifications = new ArrayList<>();

	public void browseNotifications() {
		System.out.println("\n=== TWOJE POWIADOMIENIA ===");

		if (notifications.isEmpty()) {
			System.out.println("Brak nowych powiadomień.");
		} else {
			for (String note : notifications) {
				System.out.println("- " + note);
			}
		}
		System.out.println("===========================\n");
	}

	public void addNotification(String message) {
		notifications.add(message);
	}
}