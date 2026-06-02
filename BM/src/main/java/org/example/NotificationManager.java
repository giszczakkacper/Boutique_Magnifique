package org.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class NotificationManager implements INotificationSender {

	private List<Notification> notifications = new ArrayList<>();

	public List<Notification> getNotifications() {
		if (notifications == null) {
			notifications = new ArrayList<>();
		}
		return notifications;
	}

	public void browseNotifications() {
		System.out.println("\n=== NOTIFICATIONS ===");
		if (getNotifications().isEmpty()) {
			System.out.println("No new notifications.");
		} else {
			for (Notification note : getNotifications()) {
				note.markAsRead();
				System.out.print(note.returnMessageFull());
			}
		}
		System.out.println("====================\n");
	}

	public void addNotification(Notification n) {
		if (n != null) {
			getNotifications().add(n);
		}
	}

	public void addNotification(String message) {
		getNotifications().add(new Notification(LocalDateTime.now(), message));
	}

	@Override
	public void send(Notification notification) {
		addNotification(notification);
	}
}
