package com.example.motion.interfaces;

import java.util.UUID;
import com.example.motion.model.AnimationData;
import com.example.motion.model.CollisionData;
import com.example.motion.model.MotionState;
import com.example.motion.model.PhysicsData;

/**
 * Interface für die Logik-Schicht der Bewegungsverarbeitung.
 * Verarbeitet Bewegungsberechnungen und Physik.
 */
public interface IMotionLayer {
    
    /**
     * Verarbeitet Bewegungsdaten und berechnet den nächsten Zustand.
     *
     * @param characterId Die ID des Charakters
     * @param currentState Der aktuelle Bewegungszustand
     * @param deltaTime Die vergangene Zeit seit dem letzten Update
     * @return Der neue Bewegungszustand
     */
    MotionState processMotion(UUID characterId, 
                            MotionState currentState, 
                            float deltaTime);

    /**
     * Führt eine Kollisionsprüfung durch.
     *
     * @param characterId Die ID des Charakters
     * @param motionState Der zu prüfende Bewegungszustand
     * @return Kollisionsdaten oder null wenn keine Kollision
     */
    CollisionData checkCollision(UUID characterId, 
                                MotionState motionState);

    /**
     * Berechnet physikalische Interaktionen.
     *
     * @param characterId Die ID des Charakters
     * @param physicsData Die physikalischen Eingabedaten
     * @return Der resultierende Bewegungszustand
     */
    MotionState processPhysics(UUID characterId, 
                              PhysicsData physicsData);

    /**
     * Validiert einen Bewegungszustand auf Plausibilität.
     *
     * @param motionState Der zu validierende Zustand
     * @return true wenn der Zustand gültig ist
     */
    boolean validateMotionState(MotionState motionState);

    /**
     * Interpoliert zwischen zwei Bewegungszuständen.
     *
     * @param start Ausgangszustand
     * @param end Zielzustand
     * @param factor Interpolationsfaktor (0.0 - 1.0)
     * @return Der interpolierte Zustand
     */
    MotionState interpolateStates(MotionState start, 
                                 MotionState end, 
                                 float factor);
}