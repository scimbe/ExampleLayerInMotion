package com.example.motion.sys.data;

import java.util.List;
import java.util.UUID;

import com.example.motion.sys.model.AnimationData;
import com.example.motion.sys.model.MotionState;

import java.util.Optional;

/**
 * Repository-Interface für die Datenpersistenz der Bewegungsdaten.
 * Verwaltet die Speicherung und den Abruf von Bewegungsinformationen.
 */
public interface IMotionDataRepository {

    /**
     * Speichert einen Bewegungszustand.
     *
     * @param characterId Die ID des Charakters
     * @param state Der zu speichernde Zustand
     * @return true wenn das Speichern erfolgreich war
     */
    boolean saveMotionState(UUID characterId, MotionState state);

    /**
     * Lädt den letzten Bewegungszustand eines Charakters.
     *
     * @param characterId Die ID des Charakters
     * @return Optional mit dem Bewegungszustand
     */
    Optional<MotionState> loadMotionState(UUID characterId);

    /**
     * Lädt Animationsdaten.
     *
     * @param animationId Die ID der Animation
     * @return Optional mit den Animationsdaten
     */
    Optional<AnimationData> getAnimationData(String animationId);

    /**
     * Lädt die Bewegungshistorie eines Charakters.
     *
     * @param characterId Die ID des Charakters
     * @param limit Maximale Anzahl der Einträge
     * @return Liste der historischen Bewegungszustände
     */
    List<MotionState> getMotionHistory(UUID characterId, int limit);

    /**
     * Löscht alte Bewegungsdaten eines Charakters.
     *
     * @param characterId Die ID des Charakters
     * @param maxAge Maximales Alter der Daten in Millisekunden
     * @return Anzahl der gelöschten Einträge
     */
    int cleanupMotionData(UUID characterId, long maxAge);

    /**
     * Speichert neue Animationsdaten.
     *
     * @param animationId Die ID der Animation
     * @param data Die zu speichernden Animationsdaten
     * @return true wenn das Speichern erfolgreich war
     */
    boolean saveAnimationData(String animationId, AnimationData data);
}
