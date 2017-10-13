package com.example.pasca.yugiohcart;

import java.lang.ref.SoftReference;

public class Card {

	private int id, quantity;
	private String title, rarity, type, condition, currency;
	private double price;

	public Card() {
		super();
	}

	public Card(int id, int quantity, String title, String rarity, String type, String condition, double price, String currency) {

		this.id = id;
		this.quantity = quantity;
		this.title = title;
		this.rarity = rarity;
		this.type = type;
		this.condition = condition;
		this.price = price;
		this.currency = currency;
	}

	public Card(int quantity, String title, String rarity, String type, String condition, String currency, double price) {
		this.quantity = quantity;
		this.title = title;
		this.rarity = rarity;
		this.type = type;
		this.condition = condition;
		this.currency = currency;
		this.price = price;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRarity() {
		return rarity;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
