package com.example.motion.data;

import com.example.motion.interfaces.IMotionDataRepository;
import com.example.motion.model.AnimationData;
import com.example.motion.model.MotionState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Thread-sichere In-Memory-Implementierung des Motion Data Repositories.
 */
public class InMemoryMotionDataRepository implements IMotionDataRepository {
    
    private final Map<UUID, Queue<MotionState>> motionHistory;
    private final Map<UUID, MotionState> currentStates;
    private final Map<String, AnimationData> animations;
    private final int maxHistorySize;

    /**
     * Konstruktor mit konfigurierbarer Historiengröße.
     *
     * @param maxHistorySize Maximale Anzahl der gespeicherten Zustände pro Charakter
     */
    public InMemoryMotionDataRepository(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
        this.motionHistory = new ConcurrentHashMap<>();
        this.currentStates = new ConcurrentHashMap<>();
        this.animations = new ConcurrentHashMap<>();
    }

    /**
     * Standard-Konstruktor mit Default-Historiengröße von 1000 Einträgen.
     */
    public InMemoryMotionDataRepository() {
        this(1000);
    }

    @Override
    public boolean saveMotionState(UUID characterId, MotionState state) {
        try {
            // Aktuellen Zustand speichern
            currentStates.put(characterId, state);

            // Historie aktualisieren
            Queue<MotionState> history = motionHistory.computeIfAbsent(characterId,
                k -> new ConcurrentLinkedQueue<>());

            history.offer(state);

            // Größenbegrenzung einhalten
            while (history.size() > maxHistorySize) {
                history.poll();
            }

            return true;
        } catch (Exception e) {
            // Log error in production environment
            return false;
        }
    }

    @Override
    public Optional<MotionState> loadMotionState(UUID characterId) {
        return Optional.ofNullable(currentStates.get(characterId));
    }

    @Override
    public Optional<AnimationData> getAnimationData(String animationId) {
        return Optional.ofNullable(animations.get(animationId));
    }

    @Override
    public List<MotionState> getMotionHistory(UUID characterId, int limit) {
        Queue<MotionState> history = motionHistory.get(characterId);
        if (history == null) {
            return Collections.emptyList();
        }

        return history.stream()
            .sorted(Comparator.comparing(ms -> ms.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public int cleanupMotionData(UUID characterId, long maxAge) {
        Queue<MotionState> history = motionHistory.get(characterId);
        if (history == null) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        int removedCount = 0;

        // Entferne alte Einträge
        while (!history.isEmpty() && 
               currentTime - history.peek().getTimestamp() > maxAge) {
            history.poll();
            removedCount++;
        }

        // Wenn alle Einträge gelöscht wurden, entferne die Historie komplett
        if (history.isEmpty()) {
            motionHistory.remove(characterId);
        }

        return removedCount;
    }

    @Override
    public boolean saveAnimationData(String animationId, AnimationData data) {
        try {
            animations.put(animationId, data);
            return true;
        } catch (Exception e) {
            // Log error in production environment
            return false;
        }
    }

    /**
     * Löscht alle gespeicherten Daten.
     */
    public void clearAll() {
        motionHistory.clear();
        currentStates.clear();
        animations.clear();
    }

    /**
     * Gibt die Anzahl der gespeicherten Zustände für einen Charakter zurück.
     *
     * @param characterId Die ID des Charakters
     * @return Anzahl der gespeicherten Zustände
     */
    public int getHistorySize(UUID characterId) {
        Queue<MotionState> history = motionHistory.get(characterId);
        return history != null ? history.size() : 0;
    }

    /**
     * Prüft ob Animationsdaten für eine ID existieren.
     *
     * @param animationId Die zu prüfende Animations-ID
     * @return true wenn Daten existieren
     */
    public boolean hasAnimation(String animationId) {
        return animations.containsKey(animationId);
    }
}