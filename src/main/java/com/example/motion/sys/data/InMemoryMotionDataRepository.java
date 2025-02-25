package com.example.motion.sys.data;

import com.example.motion.sys.model.AnimationData;
import com.example.motion.sys.model.MotionState;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Primary
public class InMemoryMotionDataRepository implements IMotionDataRepository {
    private final Map<UUID, MotionState> motionStates;
    private final Map<UUID, List<MotionState>> motionHistories;
    private final Map<String, AnimationData> animations;
    private final Map<UUID, Instant> lastUpdateTimes;

    public InMemoryMotionDataRepository() {
        this.motionStates = new ConcurrentHashMap<>();
        this.motionHistories = new ConcurrentHashMap<>();
        this.animations = new ConcurrentHashMap<>();
        this.lastUpdateTimes = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<MotionState> loadMotionState(UUID characterId) {
        return Optional.ofNullable(motionStates.get(characterId));
    }

    @Override
    public void saveMotionState(UUID characterId, MotionState state) {
        motionStates.put(characterId, state);
        lastUpdateTimes.put(characterId, state.getTimestamp());
        
        // Aktualisiere Historie
        motionHistories.computeIfAbsent(characterId, k -> new ArrayList<>()).add(state);
    }

    @Override
    public Optional<AnimationData> getAnimationData(String animationId) {
        return Optional.ofNullable(animations.get(animationId));
    }

    @Override
    public void saveAnimationData(AnimationData animation) {
        animations.put(animation.getId(), animation);
    }
    
    @Override
    public List<MotionState> getMotionHistory(UUID characterId, int limit) {
        List<MotionState> history = motionHistories.getOrDefault(characterId, new ArrayList<>());
        if (history.size() <= limit) {
            return new ArrayList<>(history);
        }
        
        return history.subList(history.size() - limit, history.size());
    }
    
    @Override
    public void cleanupMotionData(UUID characterId, long olderThan) {
        List<MotionState> history = motionHistories.get(characterId);
        if (history != null) {
            Instant cutoff = Instant.now().minusMillis(olderThan);
            List<MotionState> filteredHistory = history.stream()
                .filter(state -> state.getTimestamp().isAfter(cutoff))
                .collect(Collectors.toList());
            motionHistories.put(characterId, filteredHistory);
        }
    }

    @Override
    public void clearOldMotionStates(Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        
        // Entferne alte Zustände
        motionStates.entrySet().removeIf(entry -> {
            Instant lastUpdate = lastUpdateTimes.get(entry.getKey());
            return lastUpdate != null && lastUpdate.isBefore(cutoff);
        });
        
        // Entferne zugehörige Zeitstempel
        lastUpdateTimes.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        
        // Entferne alte Historien oder filtere sie
        motionHistories.forEach((characterId, history) -> {
            history.removeIf(state -> state.getTimestamp().isBefore(cutoff));
        });
    }

    @Override
    public void deleteCharacterData(UUID characterId) {
        motionStates.remove(characterId);
        motionHistories.remove(characterId);
        lastUpdateTimes.remove(characterId);
    }
}