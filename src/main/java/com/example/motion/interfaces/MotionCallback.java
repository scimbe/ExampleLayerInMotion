package com.example.motion.interfaces;

import com.example.motion.sys.model.MotionState;
import java.util.UUID;

/**
 * Callback-Interface für Motion-Updates.
 * Ermöglicht die Benachrichtigung über Änderungen im Bewegungszustand eines Charakters.
 */
@FunctionalInterface
public interface MotionCallback {
    /**
     * Wird aufgerufen, wenn sich der Bewegungszustand eines Charakters ändert.
     *
     * @param characterId Die ID des Charakters
     * @param newState Der neue Bewegungszustand
     */
    void onMotionUpdate(UUID characterId, MotionState newState);
}