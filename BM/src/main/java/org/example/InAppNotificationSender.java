package org.example;

public class InAppNotificationSender implements INotificationSender {
	private final Client recipient;

	public InAppNotificationSender(Client recipient) {
		this.recipient = recipient;
	}

	@Override
	public void send(Notification notification) {
		if (recipient == null || notification == null) {
			return;
		}
		NotificationManager manager = recipient.getNotificationManager();
		if (manager != null) {
			manager.addNotification(notification);
		}
	}

	public void send(Client recipient, Notification notification) {
		new InAppNotificationSender(recipient).send(notification);
	}
}
