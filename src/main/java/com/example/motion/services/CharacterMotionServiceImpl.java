package com.example.motion.services;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.interfaces.IMotionLayer;
import com.example.motion.model.Direction;
import com.example.motion.model.MotionState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementierung des Character Motion Service.
 * Verwaltet die Bewegungen und Animationen von Charakteren.
 */
public class CharacterMotionServiceImpl implements ICharacterMotionService {

    private final IMotionLayer motionLayer;
    private final Map<UUID, MotionState> characterStates;
    private final Map<UUID, MotionCallback> motionCallbacks;

    /**
     * Konstruktor für den Character Motion Service.
     *
     * @param motionLayer Die zu verwendende Motion Layer Implementation
     */
    public CharacterMotionServiceImpl(IMotionLayer motionLayer) {
        this.motionLayer = motionLayer;
        this.characterStates = new ConcurrentHashMap<>();
        this.motionCallbacks = new ConcurrentHashMap<>();
    }

    @Override
    public CompletableFuture<MotionState> playAnimation(UUID characterId, 
                                                       String animationId, 
                                                       float speed) {
        return CompletableFuture.supplyAsync(() -> {
            // Simuliere Animationsverarbeitung
            MotionState currentState = getOrCreateMotionState(characterId);
            
            // Dummy-Implementation: Erstelle einen neuen Zustand
            MotionState newState = new MotionState(
                characterId,
                currentState.getPosition(),
                currentState.getRotation(),
                speed
            );
            
            // Validiere den neuen Zustand
            if (motionLayer.validateMotionState(newState)) {
                updateCharacterState(characterId, newState);
                return newState;
            } else {
                throw new IllegalStateException("Ungültiger Bewegungszustand");
            }
        });
    }

    @Override
    public CompletableFuture<MotionState> setMovementDirection(UUID characterId, 
                                                              Direction direction, 
                                                              float speed) {
        return CompletableFuture.supplyAsync(() -> {
            MotionState currentState = getOrCreateMotionState(characterId);
            
            // Dummy-Implementation: Aktualisiere die Richtung
            MotionState newState = new MotionState(
                characterId,
                currentState.getPosition(),
                direction.toRotation(),
                speed
            );
            
            // Führe Kollisionsprüfung durch
            if (motionLayer.checkCollision(characterId, newState) == null) {
                updateCharacterState(characterId, newState);
                return newState;
            } else {
                throw new IllegalStateException("Kollision erkannt");
            }
        });
    }

    @Override
    public CompletableFuture<MotionState> stopMotion(UUID characterId) {
        return CompletableFuture.supplyAsync(() -> {
            MotionState currentState = getOrCreateMotionState(characterId);
            
            // Dummy-Implementation: Stoppe die Bewegung
            MotionState stoppedState = new MotionState(
                characterId,
                currentState.getPosition(),
                currentState.getRotation(),
                0.0f
            );
            
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

    /**
     * Aktualisiert den Zustand eines Charakters und benachrichtigt Callbacks.
     *
     * @param characterId Die ID des Charakters
     * @param newState Der neue Bewegungszustand
     */
    private void updateCharacterState(UUID characterId, MotionState newState) {
        characterStates.put(characterId, newState);
        
        // Benachrichtige registrierte Callbacks
        MotionCallback callback = motionCallbacks.get(characterId);
        if (callback != null) {
            callback.onMotionUpdate(characterId, newState);
        }
    }

    /**
     * Holt den aktuellen Zustand oder erstellt einen neuen.
     *
     * @param characterId Die ID des Charakters
     * @return Der aktuelle oder neue Bewegungszustand
     */
    private MotionState getOrCreateMotionState(UUID characterId) {
        return characterStates.computeIfAbsent(characterId, 
            this::createDefaultMotionState);
    }

    /**
     * Erstellt einen Standard-Bewegungszustand für einen Charakter.
     *
     * @param characterId Die ID des Charakters
     * @return Ein neuer Standard-Bewegungszustand
     */
    private MotionState createDefaultMotionState(UUID characterId) {
        return new MotionState(
            characterId,
            new Position(0, 0, 0),  // Startposition
            new Rotation(0, 0, 0),  // Standardausrichtung
            0.0f                    // Keine Bewegung
        );
    }
}