package com.shiftmanagerserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.shiftmanagerserver.entities.Konan;
import com.shiftmanagerserver.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String USERS_FILE = "users.json";
    private final ObjectMapper objectMapper;
    private final List<User> users;
    private final KonanService konanService;


    public UserService() {
        this.objectMapper = new ObjectMapper();
        this.users = loadUsers();
        this.konanService = new KonanService();
    }

    public List<User> users() {
        return users;
    }

    public boolean createUser(User user) {
        if (userExists(user.getName())) {
            return false;
        }
        Konan konan = new Konan(user.getId(), konanService.getAverageKonanScore());
        konanService.createKonan(konan);
        user.setKonanId(konan.getId());
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);
        users.add(user);
        saveUsers();
        return true;
    }

    public boolean authenticateUser(String username, String password) {
        return users.stream()
                .filter(u -> u.getName().equals(username))
                .findFirst()
                .map(user -> BCrypt.checkpw(password, user.getPassword()))
                .orElse(false);
    }

    private List<User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            logger.info("Users file not found, creating new user list");
            return new ArrayList<>();
        }
        try {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, User.class);
            List<User> loadedUsers = objectMapper.readValue(file, listType);
            logger.info("Successfully loaded {} users from file", loadedUsers.size());
            return loadedUsers;
        } catch (IOException e) {
            logger.error("Error loading users from file", e);
            return new ArrayList<>();
        }
    }

    private void saveUsers() {
        try {
            objectMapper.writeValue(new File(USERS_FILE), users);
            logger.info("Successfully saved {} users to file", users.size());
        } catch (IOException e) {
            logger.error("Error saving users to file", e);
            throw new RuntimeException("Failed to save users", e);
        }
    }

    private boolean userExists(String username) {
        return users.stream()
                .anyMatch(u -> u.getName().equals(username));
    }
}
