package com.shiftmanagerserver.entities;

import java.util.Set;
import java.util.UUID;

public class User {
    private String id;
    private String name;
    private int score;
    private Set<ShiftPreference> preferences;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String name, int score) {
        this();
        this.name = name;
        this.score = score;
    }

    public String name() {
        return name;
    }

    public int score() {
        return score;
    }

    public String id() {
        return id;
    }

    public Set<ShiftPreference> preferences() {
        return preferences;
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