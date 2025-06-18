package main.java.com.shiftmanagerserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import main.java.com.shiftmanagerserver.model.Konan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class KonanService {
    private static final Logger logger = LoggerFactory.getLogger(KonanService.class);
    private static final String DATA_FILE = "konans.json";
    private final ObjectMapper objectMapper;
    private List<Konan> konans;

    public KonanService() {
        this.objectMapper = new ObjectMapper();
        this.konans = new ArrayList<>();
        loadKonans();
    }

    private void loadKonans() {
        try {
            File file = new File(DATA_FILE);
            if (file.exists()) {
                konans = objectMapper.readValue(file, new TypeReference<List<Konan>>() {});
                logger.info("Loaded {} konans from file", konans.size());
            } else {
                konans = new ArrayList<>();
                logger.info("No existing konans file found, starting with empty list");
            }
        } catch (IOException e) {
            logger.error("Error loading konans from file", e);
            konans = new ArrayList<>();
        }
    }

    private void saveKonans() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), konans);
            logger.debug("Saved {} konans to file", konans.size());
        } catch (IOException e) {
            logger.error("Error saving konans to file", e);
        }
    }

    public Future<List<Konan>> getAllKonans() {
        logger.debug("Getting all konans");
        return Future.succeededFuture(new ArrayList<>(konans));
    }

    public Future<Konan> getKonanById(String id) {
        logger.debug("Getting konan with id: {}", id);
        Optional<Konan> konan = konans.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
        return konan.map(Future::succeededFuture)
                .orElseGet(() -> Future.failedFuture("Konan not found"));
    }

    public Future<Konan> createKonan(Konan konan) {
        logger.debug("Creating new konan: {}", konan);
        konans.add(konan);
        saveKonans();
        return Future.succeededFuture(konan);
    }

    public Future<Konan> updateKonan(String id, JsonObject updates) {
        logger.debug("Updating konan with id: {} and updates: {}", id, updates);
        return getKonanById(id)
                .map(konan -> {
                    if (updates.containsKey("name")) {
                        konan.setName(updates.getString("name"));
                    }
                    if (updates.containsKey("score")) {
                        konan.setScore(updates.getInteger("score"));
                    }
                    saveKonans();
                    return konan;
                });
    }

    public Future<Void> deleteKonan(String id) {
        logger.debug("Deleting konan with id: {}", id);
        boolean removed = konans.removeIf(konan -> konan.getId().equals(id));
        if (removed) {
            saveKonans();
            return Future.succeededFuture();
        }
        return Future.failedFuture("Konan not found");
    }
}