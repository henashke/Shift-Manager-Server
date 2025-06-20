package com.shiftmanagerserver.model;

import com.shiftmanagerserver.entities.ShiftPreference;

import java.util.Set;
import java.util.UUID;

public class User {
    public String id() {
        return id;
    }

    private String id;
    private String name;
    private int score;

    public Set<ShiftPreference> preferences() {
        return preferences;
    }

    private Set<ShiftPreference> preferences;


    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String name, int score) {
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