package com.example.automatedtourguidesystem.ui;

public class User {


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String type, String email) {
        this.type = type;
        this.email = email;
    }

    private String type;
    private String email;


    public User(){
        this.type = "";
        this.email = "";


    }
}
