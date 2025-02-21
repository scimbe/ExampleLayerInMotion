package com.example.motion.sys.behavior;

import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.CollisionData;
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
     * Setzt den Layer für einen Charakter zurück.
     *
     * @param characterId ID des Charakters
     */
    void reset(UUID characterId);
}
