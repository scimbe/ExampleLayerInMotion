package com.example.motion.interfaces;

import java.util.UUID;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.example.motion.model.Direction;
import com.example.motion.model.MotionState;

/**
 * Service-Interface für die API-Schicht der Charakterbewegung.
 * Stellt die primäre Schnittstelle für externe Systeme dar.
 */
public interface ICharacterMotionService {
    
    /**
     * Fügt einen neuen Motion Layer hinzu.
     *
     * @param layer Der hinzuzufügende Layer
     * @param priority Die Priorität des Layers (höhere Werte = höhere Priorität)
     * @return true wenn der Layer erfolgreich hinzugefügt wurde
     */
    boolean addMotionLayer(IMotionLayer layer, int priority);

    /**
     * Entfernt einen Motion Layer.
     *
     * @param layer Der zu entfernende Layer
     * @return true wenn der Layer gefunden und entfernt wurde
     */
    boolean removeMotionLayer(IMotionLayer layer);

    /**
     * Aktualisiert die Priorität eines Layers.
     *
     * @param layer Der zu aktualisierende Layer
     * @param newPriority Die neue Priorität
     * @return true wenn der Layer gefunden und aktualisiert wurde
     */
    boolean updateLayerPriority(IMotionLayer layer, int priority);

    /**
     * Gibt eine Liste aller aktiven Layer zurück.
     *
     * @return Liste der Layer, sortiert nach Priorität
     */
    List<IMotionLayer> getActiveLayers();

    /**
     * Startet eine Animation für einen spezifischen Charakter.
     *
     * @param characterId Die eindeutige ID des Charakters
     * @param animationId Die ID der abzuspielenden Animation
     * @param speed Die Geschwindigkeit der Animation (1.0 = normal)
     * @return Future mit dem Bewegungszustand nach der Animation
     */
    CompletableFuture<MotionState> playAnimation(UUID characterId, 
                                                String animationId, 
                                                float speed);

    /**
     * Setzt die Bewegungsrichtung eines Charakters.
     *
     * @param characterId Die eindeutige ID des Charakters
     * @param direction Der Richtungsvektor der Bewegung
     * @param speed Die Geschwindigkeit der Bewegung
     * @return Future mit dem aktualisierten Bewegungszustand
     */
    CompletableFuture<MotionState> setMovementDirection(UUID characterId, 
                                                       Direction direction, 
                                                       float speed);

    /**
     * Stoppt alle aktiven Bewegungen eines Charakters.
     *
     * @param characterId Die eindeutige ID des Charakters
     * @return Future mit dem finalen Bewegungszustand
     */
    CompletableFuture<MotionState> stopMotion(UUID characterId);

    /**
     * Registriert einen Callback für Bewegungsänderungen.
     *
     * @param characterId Die eindeutige ID des Charakters
     * @param callback Der auszuführende Callback
     */
    void registerMotionCallback(UUID characterId, MotionCallback callback);

    /**
     * Fragt den aktuellen Bewegungszustand eines Charakters ab.
     *
     * @param characterId Die eindeutige ID des Charakters
     * @return Der aktuelle Bewegungszustand
     */
    MotionState getMotionState(UUID characterId);

    /**
     * Interface für Bewegungs-Callbacks.
     */
    interface MotionCallback {
        void onMotionUpdate(UUID characterId, MotionState newState);
    }
}