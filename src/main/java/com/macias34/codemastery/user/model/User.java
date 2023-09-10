package com.macias34.codemastery.user.model;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "user_")
@Getter
@Setter
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String username;
	private String email;
	private Timestamp createdAt;
	private String role;
	private String password;

	public User(String username, String email, String passwordHash, Timestamp createdAt, String role) {
		this.username = username;
		this.email = email;
		this.password = passwordHash;
		this.createdAt = createdAt;
		this.role = role;
	}

}
