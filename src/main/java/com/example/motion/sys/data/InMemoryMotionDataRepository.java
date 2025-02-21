package com.example.motion.sys.data;

import com.example.motion.sys.model.AnimationData;
import com.example.motion.sys.model.MotionState;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMotionDataRepository implements IMotionDataRepository {
    private final Map<UUID, MotionState> motionStates;
    private final Map<String, AnimationData> animations;
    private final Map<UUID, Instant> lastUpdateTimes;

    public InMemoryMotionDataRepository() {
        this.motionStates = new ConcurrentHashMap<>();
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
    public void clearOldMotionStates(Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        motionStates.entrySet().removeIf(entry -> {
            Instant lastUpdate = lastUpdateTimes.get(entry.getKey());
            return lastUpdate != null && lastUpdate.isBefore(cutoff);
        });
    }

    @Override
    public void deleteCharacterData(UUID characterId) {
        motionStates.remove(characterId);
        lastUpdateTimes.remove(characterId);
    }
}
