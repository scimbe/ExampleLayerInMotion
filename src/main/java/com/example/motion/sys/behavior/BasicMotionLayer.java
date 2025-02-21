package com.example.motion.sys.behavior;

import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.Position;
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
        float rotation = currentState.getRotation().getY();
        
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
    public MotionState checkCollision(UUID characterId, MotionState proposedState) {
        // Basic Layer implementiert keine Kollisionserkennung
        return null;
    }
    
    @Override
    public void reset(UUID characterId) {
        // Basic Layer benötigt kein Reset
    }
}
