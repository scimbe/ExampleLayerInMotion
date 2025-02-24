package com.example.motion.sys.behavior;

import com.example.motion.sys.model.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementiert Laufbewegungen mit höherer Geschwindigkeit und Dynamik.
 */
public class RunningLayer implements IMotionLayer {

    private static final float RUNNING_SPEED = 3.0f;
    private static final float ACCELERATION = 2.0f;
    private static final float MAX_STAMINA = 100.0f;
    
    private final Map<UUID, Float> staminaLevels = new ConcurrentHashMap<>();

    @Override
    public MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime) {
        float speed = currentState.getSpeed();
        float currentStamina = getStamina(characterId);

        // Stamina-basierte Geschwindigkeitsanpassung
        if (speed > 0) {
            currentStamina = Math.max(0, currentStamina - deltaTime * 10);
            staminaLevels.put(characterId, currentStamina);
            
            if (currentStamina <= 0) {
                speed = Math.max(1.0f, speed - ACCELERATION * deltaTime);
            }
        } else {
            currentStamina = Math.min(MAX_STAMINA, currentStamina + deltaTime * 5);
            staminaLevels.put(characterId, currentStamina);
        }

        // Berechne neue Position mit Laufgeschwindigkeit
        Position currentPos = currentState.getPosition();
        Rotation rotation = currentState.getRotation();

        float distance = speed * RUNNING_SPEED * deltaTime;
        float newX = currentPos.getX()
                + distance * (float) Math.cos(Math.toRadians(rotation.getYaw()));
        float newZ = currentPos.getZ()
                + distance * (float) Math.sin(Math.toRadians(rotation.getYaw()));

        // Füge vertikale Oszillation für Laufbewegung hinzu
        float bobbing = (float) Math.sin(System.currentTimeMillis() / 200.0) * 0.1f * speed;
        float newY = currentPos.getY() + bobbing;

        return new MotionState(
                characterId,
                new Position(newX, newY, newZ),
                rotation,
                speed);
    }

    @Override
    public CollisionData checkCollision(UUID characterId, MotionState motionState) {
        // Erweiterte Kollisionsprüfung für schnellere Bewegungen
        return null; // Dummy-Implementierung
    }

    @Override
    public MotionState processPhysics(UUID characterId, PhysicsData physicsData) {
        // Physik mit Trägheit und Beschleunigung
        float speed = physicsData.getSpeed();
        if (speed > 0) {
            speed = Math.min(RUNNING_SPEED, speed + ACCELERATION * physicsData.getDeltaTime());
        }

        // Anwenden von Schwerkraft
        Position position = physicsData.getPosition();
        Position adjustedPosition = new Position(
            position.getX(),
            Math.max(0, position.getY() - 9.81f * physicsData.getDeltaTime() * 0.2f),
            position.getZ()
        );

        return new MotionState(
                characterId,
                adjustedPosition,
                physicsData.getRotation(),
                speed);
    }
    
    @Override
    public boolean validateMotionState(MotionState motionState) {
        // Sicherstellen, dass die Geschwindigkeit im gültigen Bereich liegt
        return motionState.getSpeed() >= 0.0f && motionState.getSpeed() <= RUNNING_SPEED * 1.2f;
    }
    
    @Override
    public MotionState interpolateStates(MotionState start, MotionState end, float factor) {
        // Beschleunigte Interpolation für dynamischeres Gefühl
        // Verwende eine quadratische Easing-Funktion
        float easedFactor = factor < 0.5f ? 2 * factor * factor : 1 - (float)Math.pow(-2 * factor + 2, 2) / 2;
        
        Position startPos = start.getPosition();
        Position endPos = end.getPosition();
        Position interpolatedPos = new Position(
            startPos.getX() + (endPos.getX() - startPos.getX()) * easedFactor,
            startPos.getY() + (endPos.getY() - startPos.getY()) * easedFactor,
            startPos.getZ() + (endPos.getZ() - startPos.getZ()) * easedFactor
        );
        
        Rotation startRot = start.getRotation();
        Rotation endRot = end.getRotation();
        Rotation interpolatedRot = new Rotation(
            startRot.getPitch() + (endRot.getPitch() - startRot.getPitch()) * easedFactor,
            startRot.getYaw() + (endRot.getYaw() - startRot.getYaw()) * easedFactor,
            startRot.getRoll() + (endRot.getRoll() - startRot.getRoll()) * easedFactor
        );
        
        float interpolatedSpeed = start.getSpeed() + (end.getSpeed() - start.getSpeed()) * easedFactor;
        
        return new MotionState(
            start.getCharacterId(),
            interpolatedPos,
            interpolatedRot,
            interpolatedSpeed
        );
    }
    
    @Override
    public void reset(UUID characterId) {
        // Stamina zurücksetzen
        staminaLevels.put(characterId, MAX_STAMINA);
    }
    
    private float getStamina(UUID characterId) {
        return staminaLevels.getOrDefault(characterId, MAX_STAMINA);
    }
}