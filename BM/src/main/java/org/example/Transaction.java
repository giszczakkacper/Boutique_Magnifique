package org.example;

import java.time.LocalDate;

public class Transaction {
	private int transactionID;
	private LocalDate date;
	private int productID;
	private Integer quantity;
	private ReturnState returnedState = ReturnState.NOTATTEMPTED;
	private LocalDate returnDate;
	private int userID;

	public Transaction(int transactionID, LocalDate date, int productID, int userID) {
		this(transactionID, date, productID, userID, 1);
	}

	public Transaction(int transactionID, LocalDate date, int productID, int userID, int quantity) {
		this.transactionID = transactionID;
		this.date = date;
		this.productID = productID;
		this.userID = userID;
		this.quantity = quantity > 0 ? quantity : 1;
	}

	public boolean markForReturn() {
		if (returnedState == ReturnState.NOTATTEMPTED) {
			returnedState = ReturnState.PENDING;
			returnDate = LocalDate.now();
			return true;
		}
		return false;
	}

	public boolean approveReturn() {
		if (returnedState == ReturnState.PENDING) {
			returnedState = ReturnState.RETURNED;
			return true;
		}
		return false;
	}

	public boolean denyReturn() {
		if (returnedState == ReturnState.PENDING) {
			returnedState = ReturnState.DENIED;
			return true;
		}
		return false;
	}

	public boolean isReturnPending() {
		return returnedState == ReturnState.PENDING;
	}

	public int getUserID() { return userID; }
	public int getTransactionID() { return transactionID; }
	public int getProductID() { return productID; }
	public int getQuantity() { return quantity == null || quantity <= 0 ? 1 : quantity; }
	public LocalDate getDate() { return date; }
	public ReturnState getReturnedState() { return returnedState; }
	public LocalDate getReturnDate() { return returnDate; }

	@Override
	public String toString() {
		return "Transaction #" + transactionID +
				" | Product ID: " + productID +
				" | Quantity: " + getQuantity() +
				" | User ID: " + userID +
				" | Date: " + date +
				" | Return status: " + returnedState +
				(returnDate != null ? " | Return date: " + returnDate : "");
	}
}
