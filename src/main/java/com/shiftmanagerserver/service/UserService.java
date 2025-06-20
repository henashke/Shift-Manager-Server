package com.shiftmanagerserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftmanagerserver.model.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {
    private  final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String DATA_FILE = "users.json";
    private final ObjectMapper objectMapper;

    public List<User> users() {
        return users;
    }

    private List<User> users;

    public UserService() {
        this.objectMapper = new ObjectMapper();
        this.users = new ArrayList<>();
        loadUsers();
    }

    public Future<List<User>> getAllUsers() {
        logger.debug("Getting all users");
        return Future.succeededFuture(new ArrayList<>(users));
    }

    public Future<User> getUserById(String id) {
        logger.debug("Getting user with id: {}", id);
        Optional<User> user = users.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
        return user.map(Future::succeededFuture)
                .orElseGet(() -> Future.failedFuture("User not found"));
    }

    public Future<User> createUser(User user) {
        logger.debug("Creating new user: {}", user);
        users.add(user);
        saveUsers();
        return Future.succeededFuture(user);
    }

    public Future<User> updateUser(String id, JsonObject updates) {
        logger.debug("Updating user with id: {} and updates: {}", id, updates);
        return getUserById(id)
                .map(user -> {
                    if (updates.containsKey("name")) {
                        user.setName(updates.getString("name"));
                    }
                    if (updates.containsKey("score")) {
                        user.setScore(updates.getInteger("score"));
                    }
                    saveUsers();
                    return user;
                });
    }

    public Future<Void> deleteUser(String id) {
        logger.debug("Deleting user with id: {}", id);
        boolean removed = users.removeIf(user -> user.getId().equals(id));
        if (removed) {
            saveUsers();
            return Future.succeededFuture();
        }
        return Future.failedFuture("User not found");
    }

    private void loadUsers() {
        try {
            File file = new File(DATA_FILE);
            if (file.exists()) {
                users = objectMapper.readValue(file, new TypeReference<List<User>>() {
                });
                logger.info("Loaded {} users from file", users.size());
            } else {
                users = new ArrayList<>();
                logger.info("No existing users file found, starting with empty list");
            }
        } catch (IOException e) {
            logger.error("Error loading users from file", e);
            users = new ArrayList<>();
        }
    }

    private void saveUsers() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), users);
            logger.debug("Saved {} users to file", users.size());
        } catch (IOException e) {
            logger.error("Error saving users to file", e);
        }
    }
}