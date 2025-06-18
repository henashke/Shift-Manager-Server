package com.shiftmanagerserver.model;

import java.util.UUID;

public class Konan {
    private String id;
    private String name;
    private int score;

    public Konan() {
        this.id = UUID.randomUUID().toString();
    }

    public Konan(String name, int score) {
        this();
        this.name = name;
        this.score = score;
    }

    // Getters and Setters
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
}