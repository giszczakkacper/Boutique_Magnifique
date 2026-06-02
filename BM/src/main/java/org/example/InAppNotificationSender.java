package org.example;

public class InAppNotificationSender implements INotificationSender {
	private Client recipient;

	public InAppNotificationSender() {
	}

	public InAppNotificationSender(Client recipient) {
		this.recipient = recipient;
	}

	public Client getRecipient() {
		return recipient;
	}

	public void setRecipient(Client recipient) {
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
		setRecipient(recipient);
		send(notification);
	}
}
