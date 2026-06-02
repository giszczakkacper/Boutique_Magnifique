package org.example;

public class Credentials {
	private String username;
	private String email;
	private Address address;

	public Credentials(String username, String email, Address address) {
		this.username = username;
		this.email = email;
		this.address = address;
	}

	public String getUsername() { return username; }
	public String getEmail() { return email; }
	public Address getAddress() { return address; }

	public void setUsername(String username) { this.username = username; }
	public void setEmail(String email) { this.email = email; }
	public void setAddress(Address address) { this.address = address; }
}