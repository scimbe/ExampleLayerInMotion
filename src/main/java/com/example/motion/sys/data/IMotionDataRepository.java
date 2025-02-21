package com.example.motion.sys.data;

import com.example.motion.sys.model.AnimationData;
import com.example.motion.sys.model.MotionState;
import java.time.Duration;
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
     * Löscht alte Bewegungszustände.
     *
     * @param maxAge Maximales Alter der Daten
     */
    void clearOldMotionStates(Duration maxAge);
    
    /**
     * Löscht alle Daten eines Charakters.
     *
     * @param characterId ID des Charakters
     */
    void deleteCharacterData(UUID characterId);
}
