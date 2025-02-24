package com.example.motion.sys.behavior;

import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.CollisionData;
import com.example.motion.sys.model.PhysicsData;
import java.util.UUID;

/**
 * Interface für Motion Layer.
 * Definiert die grundlegenden Operationen für die Verarbeitung von Bewegungen.
 */
public interface IMotionLayer {
    
    /**
     * Verarbeitet den aktuellen Bewegungszustand und berechnet den nächsten Zustand.
     *
     * @param characterId ID des Charakters
     * @param currentState Aktueller Bewegungszustand
     * @param deltaTime Vergangene Zeit seit letztem Update in Sekunden
     * @return Neuer Bewegungszustand
     */
    MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime);
    
    /**
     * Prüft auf Kollisionen für einen vorgeschlagenen Bewegungszustand.
     *
     * @param characterId ID des Charakters
     * @param proposedState Vorgeschlagener Bewegungszustand
     * @return Kollisionsdaten oder null wenn keine Kollision
     */
    CollisionData checkCollision(UUID characterId, MotionState proposedState);
    
    /**
     * Verarbeitet physikalische Daten und wendet sie auf den Bewegungszustand an.
     *
     * @param characterId ID des Charakters
     * @param physicsData Physikalische Daten
     * @return Aktualisierter Bewegungszustand
     */
    default MotionState processPhysics(UUID characterId, PhysicsData physicsData) {
        return new MotionState(
            characterId,
            physicsData.getPosition(),
            physicsData.getRotation(),
            physicsData.getSpeed()
        );
    }
    
    /**
     * Validiert einen Bewegungszustand.
     *
     * @param motionState Zu validierender Bewegungszustand
     * @return true wenn der Zustand gültig ist, false sonst
     */
    default boolean validateMotionState(MotionState motionState) {
        return true;
    }
    
    /**
     * Interpoliert zwischen zwei Bewegungszuständen.
     *
     * @param start Ausgangszustand
     * @param end Zielzustand
     * @param factor Interpolationsfaktor (0-1)
     * @return Interpolierter Zustand
     */
    default MotionState interpolateStates(MotionState start, MotionState end, float factor) {
        return end;
    }
    
    /**
     * Setzt den Layer für einen Charakter zurück.
     *
     * @param characterId ID des Charakters
     */
    default void reset(UUID characterId) {
        // Standardimplementierung macht nichts
    }
}