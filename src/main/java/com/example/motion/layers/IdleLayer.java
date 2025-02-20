package com.example.motion.layers;

import com.example.motion.interfaces.IMotionLayer;
import com.example.motion.model.*;

/**
 * Implementiert Idle-Animationen und Ruhezustände.
 */
public class IdleLayer implements IMotionLayer {
    
    private static final float IDLE_ANIMATION_SPEED = 0.5f;
    private static final float BREATHING_AMPLITUDE = 0.02f;
    
    @Override
    public MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime) {
        if (currentState.getSpeed() > 0) {
            return currentState;
        }
        
        // Implementiere subtile Atembewegung
        float breathingOffset = (float)Math.sin(System.currentTimeMillis() / 1000.0) * BREATHING_AMPLITUDE;
        
        Position currentPos = currentState.getPosition();
        Position newPosition = new Position(
            currentPos.getX(),
            currentPos.getY() + breathingOffset,
            currentPos.getZ()
        );
        
        // Füge gelegentliche Kopfbewegungen hinzu
        Rotation currentRot = currentState.getRotation();
        float headMovement = (float)Math.sin(System.currentTimeMillis() / 3000.0) * 2.0f;
        Rotation newRotation = new Rotation(
            currentRot.getPitch() + headMovement,
            currentRot.getYaw(),
            currentRot.getRoll()
        );
        
        return new MotionState(
            characterId,
            newPosition,
            newRotation,
            0.0f
        );
    }
    
    @Override
    public CollisionData checkCollision(UUID characterId, MotionState motionState) {
        // Minimale Kollisionsprüfung für stehende Position
        return null; // Dummy-Implementierung
    }
    
    @Override
    public MotionState processPhysics(UUID characterId, PhysicsData physicsData) {
        // Grundlegende Physik für stehende Position
        return new MotionState(
            characterId,
            physicsData.getPosition(),
            physicsData.getRotation(),
            0.0f
        );
    }
    
    @Override
    public boolean validateMotionState(MotionState motionState) {
        // Validiere Idle-Zustand
        return motionState.getSpeed() == 0.0f;
    }
    
    @Override
    public MotionState interpolateStates(MotionState start, MotionState end, float factor) {
        // Sanfte Interpolation für Idle-Animationen
        Position interpolatedPos = interpolatePosition(start.getPosition(), end.getPosition(), factor);
        Rotation interpolatedRot = interpolateRotation(start.getRotation(), end.getRotation(), factor);
        
        return new MotionState(
            start.getCharacterId(),
            interpolatedPos,
            interpolatedRot,
            0.0f
        );
    }
    
    private Position interpolatePosition(Position start, Position end, float factor) {
        // Verwende eine speziell gedämpfte Interpolation für ruhige Bewegungen
        float dampedFactor = (float)(1 - Math.pow(1 - factor, 3));
        return new Position(
            start.getX() + (end.getX() - start.getX()) * dampedFactor,
            start.getY() + (end.getY() - start.getY()) * dampedFactor,
            start.getZ() + (end.getZ() - start.getZ()) * dampedFactor
        );
    }
    
    private Rotation interpolateRotation(Rotation start, Rotation end, float factor) {
        // Sanfte Rotation für natürliche Bewegungen
        float dampedFactor = (float)(1 - Math.pow(1 - factor, 2));
        return new Rotation(
            start.getPitch() + (end.getPitch() - start.getPitch()) * dampedFactor,
            start.getYaw() + (end.getYaw() - start.getYaw()) * dampedFactor,
            start.getRoll() + (end.getRoll() - start.getRoll()) * dampedFactor
        );
    }
}
