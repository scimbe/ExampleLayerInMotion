package com.example.motion.sys.behavior;

import com.example.motion.sys.model.*;
import java.util.UUID;

/**
 * Basis-Implementierung eines Motion Layers.
 * Verarbeitet einfache Bewegungen ohne komplexe Physik oder Kollisionen.
 */
public class BasicMotionLayer implements IMotionLayer {
    
    @Override
    public MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime) {
        // Berechne neue Position basierend auf Geschwindigkeit und Rotation
        float speed = currentState.getSpeed();
        float rotation = currentState.getRotation().getYaw();
        
        // Berechne Bewegungsvektor
        float dx = (float) Math.sin(Math.toRadians(rotation)) * speed * deltaTime;
        float dz = (float) Math.cos(Math.toRadians(rotation)) * speed * deltaTime;
        
        // Aktualisiere Position
        Position currentPos = currentState.getPosition();
        Position newPos = new Position(
            currentPos.getX() + dx,
            currentPos.getY(),
            currentPos.getZ() + dz
        );
        
        // Erstelle neuen Zustand mit aktualisierter Position
        return new MotionState(
            characterId,
            newPos,
            currentState.getRotation(),
            speed
        );
    }
    
    @Override
    public CollisionData checkCollision(UUID characterId, MotionState proposedState) {
        // Basic Layer implementiert keine Kollisionserkennung
        return null;
    }
    
    @Override
    public MotionState processPhysics(UUID characterId, PhysicsData physicsData) {
        // Einfache Physik-Verarbeitung
        return new MotionState(
            characterId,
            physicsData.getPosition(),
            physicsData.getRotation(),
            physicsData.getSpeed()
        );
    }
    
    @Override
    public boolean validateMotionState(MotionState motionState) {
        // Validiere Geschwindigkeit
        return motionState.getSpeed() >= 0 && motionState.getSpeed() <= 10.0f;
    }
    
    @Override
    public MotionState interpolateStates(MotionState start, MotionState end, float factor) {
        // Lineare Interpolation zwischen Start- und Endzustand
        Position startPos = start.getPosition();
        Position endPos = end.getPosition();
        Position interpolatedPos = new Position(
            startPos.getX() + (endPos.getX() - startPos.getX()) * factor,
            startPos.getY() + (endPos.getY() - startPos.getY()) * factor,
            startPos.getZ() + (endPos.getZ() - startPos.getZ()) * factor
        );
        
        Rotation startRot = start.getRotation();
        Rotation endRot = end.getRotation();
        Rotation interpolatedRot = new Rotation(
            startRot.getPitch() + (endRot.getPitch() - startRot.getPitch()) * factor,
            startRot.getYaw() + (endRot.getYaw() - startRot.getYaw()) * factor,
            startRot.getRoll() + (endRot.getRoll() - startRot.getRoll()) * factor
        );
        
        float interpolatedSpeed = start.getSpeed() + (end.getSpeed() - start.getSpeed()) * factor;
        
        return new MotionState(
            start.getCharacterId(),
            interpolatedPos,
            interpolatedRot,
            interpolatedSpeed
        );
    }
    
    @Override
    public void reset(UUID characterId) {
        // Basic Layer benötigt kein Reset
    }
}