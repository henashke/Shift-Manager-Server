package com.shiftmanagerserver.entities;

import java.util.UUID;

public class User {


    private String password;
    private String id;
    private String name;
    private int score;
    private String konanId;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String name, int score, String konanId) {
        this();
        this.name = name;
        this.score = score;
        this.konanId = konanId;
    }

    public String konanId() {
        return konanId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setKonanId(String id) {
        this.konanId = id;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String hashedPassword) {
        this.password = hashedPassword;
    }
}