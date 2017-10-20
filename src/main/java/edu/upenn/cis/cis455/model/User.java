package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class User implements Serializable {
    Integer userId;
    String userName;
    String password;
    String firstName;
    String lastName;
    
    
    public User(Integer userId, String userName, String password, String firstName, String lastName) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
}
