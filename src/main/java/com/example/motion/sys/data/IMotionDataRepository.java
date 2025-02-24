package com.example.motion.sys.data;

import com.example.motion.sys.model.AnimationData;
import com.example.motion.sys.model.MotionState;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository-Interface für die Persistierung von Bewegungsdaten.
 */
public interface IMotionDataRepository {
    
    /**
     * Lädt den Bewegungszustand eines Charakters.
     *
     * @param characterId ID des Charakters
     * @return Bewegungszustand oder Optional.empty() wenn nicht gefunden
     */
    Optional<MotionState> loadMotionState(UUID characterId);
    
    /**
     * Speichert den Bewegungszustand eines Charakters.
     *
     * @param characterId ID des Charakters
     * @param state Zu speichernder Bewegungszustand
     */
    void saveMotionState(UUID characterId, MotionState state);
    
    /**
     * Lädt Animationsdaten.
     *
     * @param animationId ID der Animation
     * @return Animationsdaten oder Optional.empty() wenn nicht gefunden
     */
    Optional<AnimationData> getAnimationData(String animationId);
    
    /**
     * Speichert Animationsdaten.
     *
     * @param animation Zu speichernde Animationsdaten
     */
    void saveAnimationData(AnimationData animation);
    
    /**
     * Gibt die Bewegungshistorie eines Charakters zurück.
     *
     * @param characterId ID des Charakters
     * @param limit Maximale Anzahl der zurückzugebenden Zustände
     * @return Liste der letzten Bewegungszustände
     */
    default List<MotionState> getMotionHistory(UUID characterId, int limit) {
        return List.of();
    }
    
    /**
     * Löscht alte Bewegungsdaten.
     *
     * @param characterId ID des Charakters
     * @param olderThan Maximales Alter der zu behaltenden Daten
     */
    default void cleanupMotionData(UUID characterId, long olderThan) {
        // Standardimplementierung macht nichts
    }
    
    /**
     * Löscht alte Bewegungszustände.
     *
     * @param maxAge Maximales Alter der Daten
     */
    default void clearOldMotionStates(Duration maxAge) {
        // Standardimplementierung macht nichts
    }
    
    /**
     * Löscht alle Daten eines Charakters.
     *
     * @param characterId ID des Charakters
     */
    default void deleteCharacterData(UUID characterId) {
        // Standardimplementierung macht nichts
    }
}