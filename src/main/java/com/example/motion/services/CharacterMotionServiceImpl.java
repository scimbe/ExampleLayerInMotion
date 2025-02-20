package com.example.motion.services;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.interfaces.IMotionLayer;
import com.example.motion.interfaces.IMotionDataRepository;
import com.example.motion.model.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Implementierung des Character Motion Service mit Animation-Support.
 */
public class CharacterMotionServiceImpl implements ICharacterMotionService {
    
    private final IMotionLayer motionLayer;
    private final IMotionDataRepository repository;
    private final Map<UUID, MotionState> characterStates;
    private final Map<UUID, MotionCallback> motionCallbacks;
    private final Map<UUID, AnimationPlayback> activeAnimations;
    private final ScheduledExecutorService animator;

    /**
     * Konstruktor für den Character Motion Service.
     */
    public CharacterMotionServiceImpl(IMotionLayer motionLayer, IMotionDataRepository repository) {
        this.motionLayer = motionLayer;
        this.repository = repository;
        this.characterStates = new ConcurrentHashMap<>();
        this.motionCallbacks = new ConcurrentHashMap<>();
        this.activeAnimations = new ConcurrentHashMap<>();
        this.animator = Executors.newScheduledThreadPool(1);
    }

    @Override
    public CompletableFuture<MotionState> playAnimation(UUID characterId, 
                                                       String animationId, 
                                                       float speed) {
        return CompletableFuture.supplyAsync(() -> {
            // Lade Animation aus Repository
            AnimationData animation = repository.getAnimationData(animationId)
                .orElseThrow(() -> new IllegalArgumentException("Animation nicht gefunden: " + animationId));

            // Hole aktuellen Zustand
            MotionState currentState = getOrCreateMotionState(characterId);

            // Erstelle neue Animation Playback
            AnimationPlayback playback = new AnimationPlayback(
                animation,
                currentState,
                speed,
                System.currentTimeMillis()
            );

            // Stoppe aktive Animation falls vorhanden
            stopActiveAnimation(characterId);

            // Starte neue Animation
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
            
            // Erstelle neuen Zustand mit aktualisierter Richtung
            MotionState newState = new MotionState(
                characterId,
                currentState.getPosition(),
                direction.toRotation(),
                speed
            );
            
            // Validiere und prüfe auf Kollisionen
            if (motionLayer.validateMotionState(newState) && 
                motionLayer.checkCollision(characterId, newState) == null) {
                
                updateCharacterState(characterId, newState);
                return newState;
            } else {
                throw new IllegalStateException("Ungültiger Bewegungszustand oder Kollision");
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

                // Berechne aktuelle Animationszeit
                float currentTime = (System.currentTimeMillis() - playback.getStartTime()) / 1000.0f * playback.getSpeed();

                // Interpoliere neuen Zustand
                MotionState newState = playback.getAnimation().interpolateAtTime(
                    currentTime, 
                    playback.getBaseState()
                );

                // Validiere und aktualisiere
                if (motionLayer.validateMotionState(newState)) {
                    updateCharacterState(characterId, newState);

                    // Prüfe ob Animation beendet ist
                    if (!playback.getAnimation().isLooping() && 
                        currentTime >= playback.getAnimation().getDuration()) {
                        stopActiveAnimation(characterId);
                    }
                }
            } catch (Exception e) {
                // Log error in production environment
                stopActiveAnimation(characterId);
            }
        }, 0, 16, TimeUnit.MILLISECONDS); // 60 FPS Update-Rate
    }

    private void stopActiveAnimation(UUID characterId) {
        AnimationPlayback playback = activeAnimations.remove(characterId);
        if (playback != null) {
            // Optional: Finale Animation State speichern
            repository.saveMotionState(characterId, getMotionState(characterId));
        }
    }

    private void updateCharacterState(UUID characterId, MotionState newState) {
        // Speichere Zustand
        characterStates.put(characterId, newState);
        repository.saveMotionState(characterId, newState);
        
        // Benachrichtige Callback
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