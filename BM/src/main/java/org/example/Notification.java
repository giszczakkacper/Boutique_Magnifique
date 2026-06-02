package org.example;


import java.time.LocalDateTime;

public class Notification {
	private LocalDateTime sentDate;
	private LocalDateTime readDate;
	private String contents;

	public Notification(LocalDateTime sendDate, String s) {
		this.sentDate = sendDate;
		this.contents = s;
	}

	public LocalDateTime getSentDate() {
		return sentDate;
	}

	public LocalDateTime getReadDate() {
		return readDate;
	}

	public String getContents() {
		return contents;
	}

	public void markAsRead() {
		if (readDate == null) {
			readDate = LocalDateTime.now();
		}
	}

	public boolean isRead() {
		return readDate != null;
	}

	public String returnMessageFull() {
		return "========================\n" +
				sentDate + "\n" +
				contents + "\n" +
				(isRead() ? "Read: " + readDate + "\n" : "") +
				"========================" +
				"\n";
	}
}
