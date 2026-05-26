package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationManager {

	private List<Notification> notifications = new ArrayList<>();

	public void browseNotifications() {
		System.out.println("\n=== TWOJE POWIADOMIENIA ===");

		if (notifications.isEmpty()) {
			System.out.println("Brak nowych powiadomień.");
		} else {
			for (Notification note : notifications) {
				note.returnMessageFull();
			}
		}
		System.out.println("===========================\n");
	}

	public void deleteNotifications(int[] ids) {
		Arrays.sort(ids);
		for (int i = ids.length - 1; i >= 0; i--) {
			notifications.remove(ids[i]);
		}
	}
	public void deleteAllNotifications (){
		notifications.clear();
	}

	public void addNotification(Notification notification) {
		notifications.add(notification);
	}
}