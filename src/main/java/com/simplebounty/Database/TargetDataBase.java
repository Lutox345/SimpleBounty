package com.simplebounty.Database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TargetDataBase {

    private final Path filePath;
    private final Gson gson;
    private final ReadWriteLock lock;
    private Map<String, BountyEntry> targetToPrize;

    // ── Konstruktor ──────────────────────────────────────────────────────────────

    public TargetDataBase(Path filePath) {
        this.filePath = filePath;
        this.gson = new Gson();
        this.lock = new ReentrantReadWriteLock();
        this.targetToPrize = new HashMap<>();

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Erstellen des Filepaths", e);
        }

        load();
    }

    // ── Laden & Speichern ────────────────────────────────────────────────────────

    public void load() {
        lock.writeLock().lock();
        try {
            if (Files.exists(filePath)) {
                try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                    Type type = new TypeToken<Map<String, BountyEntry>>(){}.getType();
                    targetToPrize = gson.fromJson(reader, type);
                    if (targetToPrize == null) targetToPrize = new HashMap<>();
                } catch (IOException e) {
                    throw new RuntimeException("Fehler beim Laden der Datenbank", e);
                }
            } else {
                targetToPrize = new HashMap<>();
                saveInternal();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void saveInternal() {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(targetToPrize, writer);
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Speichern der Datenbank", e);
        }
    }

    public void save() {
        lock.writeLock().lock();
        try {
            saveInternal();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ── CRUD-Methoden ────────────────────────────────────────────────────────────

    public void setBounty(String targetName, String itemStackJson, int amount) {
        lock.writeLock().lock();
        try {
            targetToPrize.put(targetName, new BountyEntry(itemStackJson, amount));
            saveInternal();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public BountyEntry getBounty(String targetName) {
        lock.readLock().lock();
        try {
            return targetToPrize.get(targetName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean removeBounty(String targetName) {
        lock.writeLock().lock();
        try {
            boolean existed = targetToPrize.remove(targetName) != null;
            if (existed) saveInternal();
            return existed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean hasBounty(String targetName) {
        lock.readLock().lock();
        try {
            return targetToPrize.containsKey(targetName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, BountyEntry> getAllBounties() {
        lock.readLock().lock();
        try {
            return new HashMap<>(targetToPrize);
        } finally {
            lock.readLock().unlock();
        }
    }
}