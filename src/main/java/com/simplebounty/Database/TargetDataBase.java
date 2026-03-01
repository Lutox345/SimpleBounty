package com.simplebounty.Database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.Material;
import com.google.gson.Gson;

public class TargetDataBase {

    // Wo die JSON-Datei liegt, Gson-Instanz, Thread-Lock und die eigentliche Map
    private final Path filePath;
    private final Gson gson;
    private final ReadWriteLock lock;
    private Map<String, Material> targetToPrize;

    // ── Konstruktor ──────────────────────────────────────────────────────────────

    public TargetDataBase(Path filePath) {
        this.filePath = filePath;
        this.gson = new Gson();
        this.lock = new ReentrantReadWriteLock();
        this.targetToPrize = new HashMap<>();

        // Ordner anlegen falls nicht vorhanden
        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Erstellen des Filepaths", e);
        }

        load();
    }

    // ── Laden & Speichern ────────────────────────────────────────────────────────

    // Lädt die JSON-Datei in die Map – bei fehlender Datei wird eine leere erstellt
    public void load() {
        lock.writeLock().lock();
        try {
            if (Files.exists(filePath)) {
                try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                    targetToPrize = gson.fromJson(reader, Map.class);
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

    // Intern speichern – ohne eigenen Lock, da immer schon ein WriteLock gehalten wird
    private void saveInternal() {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(targetToPrize, writer);
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Speichern der Datenbank", e);
        }
    }

    // Öffentliches Speichern – holt sich selbst den WriteLock
    public void save() {
        lock.writeLock().lock();
        try {
            saveInternal();
        } finally {
            lock.writeLock().unlock();
        }
    }

    //CRUD-Methoden

    // Kopfgeld setzen oder überschreiben
    public void setBounty(String targetName, Material prize) {
        lock.writeLock().lock();
        try {
            targetToPrize.put(targetName, prize);
            saveInternal();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Kopfgeld eines Spielers abfragen – null wenn keins vorhanden
    public Material getBounty(String targetName) {
        lock.readLock().lock();
        try {
            return targetToPrize.get(targetName);
        } finally {
            lock.readLock().unlock();
        }
    }

    // Kopfgeld entfernen – gibt true zurück wenn es existiert hat
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

    // Prüft ob ein Spieler ein Kopfgeld hat
    public boolean hasBounty(String targetName) {
        lock.readLock().lock();
        try {
            return targetToPrize.containsKey(targetName);
        } finally {
            lock.readLock().unlock();
        }
    }

    // Gibt eine Kopie aller Kopfgelder zurück – für /bounty list
    public Map<String, Material> getAllBounties() {
        lock.readLock().lock();
        try {
            return new HashMap<>(targetToPrize); // Kopie damit die interne Map nicht von außen verändert werden kann
        } finally {
            lock.readLock().unlock();
        }
    }
}