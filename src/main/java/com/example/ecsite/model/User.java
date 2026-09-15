package com.example.ecsite.model;

import java.sql.Timestamp;

/** Represents one account from the live Oracle APP_USERS table. */
/**
 * ECサイトで使用する User のデータを保持するモデルクラスです。
 *
 * <p>DAOと画面の間で値を受け渡すために使用し、データベース処理や画面処理は持ちません。</p>
 */
public class User {
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private String phone;
    private String address;
    private String role;
    private Timestamp createdAt;

    public User() {
    }

    public User(String firstName, String lastName, String email,
                String phone, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public int getUserId()
     { return userId; }
    public void setUserId(int userId)
     { this.userId = userId; }
    public String getFirstName()
     { return firstName; }
    public void setFirstName(String firstName)
     { this.firstName = firstName; }
    public String getLastName()
     { return lastName; }
    public void setLastName(String lastName) 
    { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email)
     { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
