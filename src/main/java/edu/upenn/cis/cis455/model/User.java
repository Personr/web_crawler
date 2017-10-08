package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class User implements Serializable {
    Integer userId;
    String userName;
    String password;
    
    
    public User(Integer userId, String userName, String password) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
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
}
