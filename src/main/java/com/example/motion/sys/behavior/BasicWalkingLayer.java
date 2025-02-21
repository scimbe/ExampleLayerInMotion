package com.example.motion.sys.behavior;

import com.example.motion.sys.model.*;
import java.util.UUID;

/**
 * Basis-Implementation eines Motion Layers für Gehbewegungen.
 */
public class BasicWalkingLayer implements IMotionLayer {
    
    @Override
    public MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime) {
        float speed = currentState.getSpeed();
        Rotation rotation = currentState.getRotation();
        Position currentPos = currentState.getPosition();
        
        // Berechne Bewegungsvektor basierend auf Rotation und Geschwindigkeit
        float dx = (float) Math.sin(Math.toRadians(rotation.getYaw())) * speed * deltaTime;
        float dz = (float) Math.cos(Math.toRadians(rotation.getYaw())) * speed * deltaTime;
        
        // Aktualisiere Position
        Position newPos = new Position(
            currentPos.getX() + dx,
            currentPos.getY(),
            currentPos.getZ() + dz
        );
        
        return new MotionState(
            characterId,
            newPos,
            rotation,
            speed
        );
    }

    @Override
    public CollisionData checkCollision(UUID characterId, MotionState proposedState) {
        // Basic Walking Layer implementiert keine Kollisionserkennung
        return null;
    }

    @Override
    public void reset(UUID characterId) {
        // Basic Walking Layer benötigt kein Reset
    }
}
