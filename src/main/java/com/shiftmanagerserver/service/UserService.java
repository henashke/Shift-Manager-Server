package com.shiftmanagerserver.service;

import com.google.inject.Inject;
import com.shiftmanagerserver.dao.UserDao;
import com.shiftmanagerserver.model.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UserService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao dao;
    private final Set<User> users;

    @Inject
    public UserService(UserDao dao) {
        this.dao = dao;
        this.users = dao.read();
    }

    public Set<User> users() {
        return users;
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
        dao.write(users);
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
                    dao.write(users);
                    return user;
                });
    }

    public Future<Void> deleteUser(String id) {
        logger.debug("Deleting user with id: {}", id);
        boolean removed = users.removeIf(user -> user.getId().equals(id));
        if (removed) {
            dao.write(users);
            return Future.succeededFuture();
        }
        return Future.failedFuture("User not found");
    }
}