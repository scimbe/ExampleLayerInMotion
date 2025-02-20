package com.example.motion.services;

import com.example.motion.sys.behavior.IMotionLayer;
import com.example.motion.sys.data.IMotionDataRepository;
import com.example.motion.sys.model.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * Implementierung des Character Motion Service mit Layer-Management.
 */
@Service
public class CharacterMotionServiceImpl implements ICharacterMotionService {

    private final IMotionDataRepository repository;
    private final Map<UUID, MotionState> characterStates;
    private final Map<UUID, MotionCallback> motionCallbacks;
    private final Map<UUID, AnimationPlayback> activeAnimations;
    private final Map<IMotionLayer, Integer> motionLayers;
    private final ScheduledExecutorService animator;
    private final ReentrantReadWriteLock layerLock;

    /**
     * Konstruktor für den Character Motion Service.
     */
    public CharacterMotionServiceImpl(IMotionDataRepository repository) {
        this.repository = repository;
        this.characterStates = new ConcurrentHashMap<>();
        this.motionCallbacks = new ConcurrentHashMap<>();
        this.activeAnimations = new ConcurrentHashMap<>();
        this.motionLayers = new ConcurrentHashMap<>();
        this.animator = Executors.newScheduledThreadPool(1);
        this.layerLock = new ReentrantReadWriteLock();
    }

    @Override
    public boolean addMotionLayer(IMotionLayer layer, int priority) {
        try {
            layerLock.writeLock().lock();
            if (motionLayers.containsKey(layer)) {
                return false;
            }
            motionLayers.put(layer, priority);
            return true;
        } finally {
            layerLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeMotionLayer(IMotionLayer layer) {
        try {
            layerLock.writeLock().lock();
            return motionLayers.remove(layer) != null;
        } finally {
            layerLock.writeLock().unlock();
        }
    }

    @Override
    public boolean updateLayerPriority(IMotionLayer layer, int priority) {
        try {
            layerLock.writeLock().lock();
            if (!motionLayers.containsKey(layer)) {
                return false;
            }
            motionLayers.put(layer, priority);
            return true;
        } finally {
            layerLock.writeLock().unlock();
        }
    }

    @Override
    public List<IMotionLayer> getActiveLayers() {
        try {
            layerLock.readLock().lock();
            return motionLayers.entrySet().stream()
                .sorted(Map.Entry.<IMotionLayer, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        } finally {
            layerLock.readLock().unlock();
        }
    }

    @Override
    public CompletableFuture<MotionState> playAnimation(UUID characterId,
                                                       String animationId,
                                                       float speed) {
        return CompletableFuture.supplyAsync(() -> {
            AnimationData animation = repository.getAnimationData(animationId)
                .orElseThrow(() -> new IllegalArgumentException("Animation nicht gefunden: " + animationId));

            MotionState currentState = getOrCreateMotionState(characterId);

            AnimationPlayback playback = new AnimationPlayback(
                animation,
                currentState,
                speed,
                System.currentTimeMillis()
            );

            stopActiveAnimation(characterId);

            activeAnimations.put(characterId, playback);
            scheduleAnimationUpdates(characterId, playback);

            return currentState;
        });
    }

    @Override
    public CompletableFuture<MotionState> setMovementDirection(UUID characterId,
                                                              Direction direction,
                                                              float speed) {
        return CompletableFuture.supplyAsync(() -> {
            MotionState currentState = getOrCreateMotionState(characterId);
            MotionState newState = new MotionState(
                characterId,
                currentState.getPosition(),
                direction.toRotation(),
                speed
            );

            // Verarbeite Zustand durch alle Layer
            try {
                layerLock.readLock().lock();
                MotionState processedState = newState;
                for (IMotionLayer layer : getActiveLayers()) {
                    // Validiere und verarbeite den Zustand
                    if (layer.validateMotionState(processedState)) {
                        processedState = layer.processMotion(
                            characterId,
                            processedState,
                            1.0f/60.0f
                        );
                    }

                    // Prüfe auf Kollisionen
                    if (layer.checkCollision(characterId, processedState) != null) {
                        throw new IllegalStateException("Kollision erkannt");
                    }
                }

                updateCharacterState(characterId, processedState);
                return processedState;
            } finally {
                layerLock.readLock().unlock();
            }
        });
    }

    @Override
    public CompletableFuture<MotionState> stopMotion(UUID characterId) {
        return CompletableFuture.supplyAsync(() -> {
            stopActiveAnimation(characterId);

            MotionState currentState = getOrCreateMotionState(characterId);
            MotionState stoppedState = currentState.withSpeed(0.0f);

            updateCharacterState(characterId, stoppedState);
            return stoppedState;
        });
    }

    @Override
    public void registerMotionCallback(UUID characterId, MotionCallback callback) {
        motionCallbacks.put(characterId, callback);
    }

    @Override
    public MotionState getMotionState(UUID characterId) {
        return characterStates.getOrDefault(characterId, createDefaultMotionState(characterId));
    }

    private void scheduleAnimationUpdates(UUID characterId, AnimationPlayback playback) {
        animator.scheduleAtFixedRate(() -> {
            try {
                if (!activeAnimations.containsKey(characterId)) {
                    return;
                }

                float currentTime = (System.currentTimeMillis() - playback.getStartTime()) / 1000.0f * playback.getSpeed();

                // Interpoliere Animationszustand
                MotionState animatedState = playback.getAnimation().interpolateAtTime(
                    currentTime,
                    playback.getBaseState()
                );

                // Verarbeite durch alle Layer
                try {
                    layerLock.readLock().lock();
                    MotionState processedState = animatedState;
                    for (IMotionLayer layer : getActiveLayers()) {
                        if (layer.validateMotionState(processedState)) {
                            processedState = layer.processMotion(
                                characterId,
                                processedState,
                                1.0f/60.0f
                            );
                        }
                    }

                    updateCharacterState(characterId, processedState);
                } finally {
                    layerLock.readLock().unlock();
                }

                // Prüfe Animation Ende
                if (!playback.getAnimation().isLooping() &&
                    currentTime >= playback.getAnimation().getDuration()) {
                    stopActiveAnimation(characterId);
                }
            } catch (Exception e) {
                stopActiveAnimation(characterId);
            }
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void stopActiveAnimation(UUID characterId) {
        AnimationPlayback playback = activeAnimations.remove(characterId);
        if (playback != null) {
            repository.saveMotionState(characterId, getMotionState(characterId));
        }
    }

    private void updateCharacterState(UUID characterId, MotionState newState) {
        characterStates.put(characterId, newState);
        repository.saveMotionState(characterId, newState);

        MotionCallback callback = motionCallbacks.get(characterId);
        if (callback != null) {
            callback.onMotionUpdate(characterId, newState);
        }
    }

    private MotionState getOrCreateMotionState(UUID characterId) {
        return repository.loadMotionState(characterId)
            .orElseGet(() -> createDefaultMotionState(characterId));
    }

    private MotionState createDefaultMotionState(UUID characterId) {
        return new MotionState(
            characterId,
            new Position(0, 0, 0),
            new Rotation(0, 0, 0),
            0.0f
        );
    }

    /**
     * Hilfklasse für die Animation-Wiedergabe.
     */
    private static class AnimationPlayback {
        private final AnimationData animation;
        private final MotionState baseState;
        private final float speed;
        private final long startTime;

        public AnimationPlayback(AnimationData animation,
                               MotionState baseState,
                               float speed,
                               long startTime) {
            this.animation = animation;
            this.baseState = baseState;
            this.speed = speed;
            this.startTime = startTime;
        }

        public AnimationData getAnimation() { return animation; }
        public MotionState getBaseState() { return baseState; }
        public float getSpeed() { return speed; }
        public long getStartTime() { return startTime; }
    }
}
