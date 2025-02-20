package com.example.motion.sys.behavior;

import java.util.UUID;

import com.example.motion.sys.model.CollisionData;
import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.PhysicsData;

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
}
