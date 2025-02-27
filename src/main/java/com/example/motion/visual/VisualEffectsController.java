package com.example.motion.visual;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.Position;
import com.example.motion.visual.animation.EasingFunctions;
import com.example.motion.visual.animation.TransitionAnimator;
import com.example.motion.visual.footstep.Footstep;
import com.example.motion.visual.footstep.FootstepVisualizer;
import com.example.motion.visual.particle.Particle;
import com.example.motion.visual.particle.ParticleConfig;
import com.example.motion.visual.particle.ParticleSystem;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Zentraler Controller für die Verwaltung aller visuellen Effekte.
 * Koordiniert Bewegungsübergänge, Fußspuren und Partikelsysteme.
 */
@Component
public class VisualEffectsController {
    private static final Logger logger = LoggerFactory.getLogger(VisualEffectsController.class);
    private static final long UPDATE_INTERVAL_MS = 16; // ca. 60 FPS
    
    private final ICharacterMotionService motionService;
    private final TransitionAnimator transitionAnimator;
    private final FootstepVisualizer footstepVisualizer;
    private final ParticleSystem particleSystem;
    
    private final ScheduledExecutorService updateScheduler;
    private final Map<UUID, String> characterLayerTypes;
    private final Map<UUID, UUID> characterDustEmitters;
    
    @Getter
    private boolean enabled = true;
    
    /**
     * Erstellt einen neuen VisualEffectsController.
     */
    @Autowired
    public VisualEffectsController(ICharacterMotionService motionService) {
        this.motionService = motionService;
        this.transitionAnimator = new TransitionAnimator();
        this.footstepVisualizer = new FootstepVisualizer();
        this.particleSystem = new ParticleSystem();
        
        this.updateScheduler = Executors.newSingleThreadScheduledExecutor();
        this.characterLayerTypes = new ConcurrentHashMap<>();
        this.characterDustEmitters = new ConcurrentHashMap<>();
        
        // Starte regelmäßige Updates
        startUpdateCycle();
        
        logger.info("VisualEffectsController initialisiert");
    }
    
    /**
     * Startet den regelmäßigen Update-Zyklus für die visuellen Effekte.
     */
    private void startUpdateCycle() {
        updateScheduler.scheduleAtFixedRate(
            this::update,
            0,
            UPDATE_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Hauptupdate-Methode, die in regelmäßigen Abständen aufgerufen wird.
     */
    private void update() {
        if (!enabled) {
            return;
        }
        
        try {
            float deltaTime = UPDATE_INTERVAL_MS / 1000.0f;
            
            // Partikel aktualisieren
            particleSystem.update(deltaTime);
            
        } catch (Exception e) {
            logger.error("Fehler beim Update der visuellen Effekte", e);
        }
    }
    
    /**
     * Aktualisiert die visuellen Effekte für einen Charakter basierend auf seinem Bewegungszustand.
     * 
     * @param characterId ID des Charakters
     * @param layerType Typ des aktiven Bewegungslayers
     * @param state Aktueller Bewegungszustand
     */
    public void updateCharacterEffects(UUID characterId, String layerType, MotionState state) {
        if (!enabled) {
            return;
        }
        
        // Speichere den aktuellen Layer-Typ
        String previousLayerType = characterLayerTypes.put(characterId, layerType);
        
        // Prüfe auf Layer-Wechsel
        if (previousLayerType != null && !previousLayerType.equals(layerType)) {
            onLayerChanged(characterId, previousLayerType, layerType, state);
        }
        
        // Aktualisiere Fußspuren
        updateFootsteps(characterId, layerType, state);
        
        // Aktualisiere Partikeleffekte
        updateParticles(characterId, layerType, state);
    }
    
    /**
     * Aktualisiert die Fußspuren für einen Charakter.
     */
    private void updateFootsteps(UUID characterId, String layerType, MotionState state) {
        // Erstelle nur Fußspuren, wenn sich der Charakter bewegt
        if (state.getSpeed() > 0.1f) {
            footstepVisualizer.updateFootsteps(state, layerType);
        }
    }
    
    /**
     * Aktualisiert die Partikeleffekte für einen Charakter.
     */
    private void updateParticles(UUID characterId, String layerType, MotionState state) {
        Position position = state.getPosition();
        float speed = state.getSpeed();
        
        // Prüfe, ob bereits ein Staubemitter für diesen Charakter existiert
        UUID emitterId = characterDustEmitters.get(characterId);
        
        if (speed > 0.1f) {
            // Erstelle einen neuen Emitter, falls noch keiner existiert
            if (emitterId == null) {
                emitterId = particleSystem.createDustEmitter(characterId, position, speed, layerType);
                characterDustEmitters.put(characterId, emitterId);
            }
            
            // Konfiguriere den Emitter basierend auf Layer-Typ und Geschwindigkeit
            ParticleConfig config = ParticleConfig.createDustConfig();
            
            // Anpassen der Emissionsrate basierend auf Geschwindigkeit
            config.setEmissionRate(Math.min(10, Math.max(1, (int)(speed * 5))));
            
            // Anpassungen basierend auf Layer-Typ
            switch (layerType) {
                case "RunningLayer":
                    // Mehr, größere und schnellere Partikel beim Laufen
                    config.setEmissionRate(config.getEmissionRate() * 2);
                    config.setParticleSize(0.1f, 0.3f);
                    config.setParticleSpeed(0.8f, 1.5f);
                    config.setColor(0.8f, 0.8f, 0.7f); // Hellerer Staub
                    break;
                    
                case "AdvancedWalkingLayer":
                    // Angepasste Partikel für fortgeschrittenes Gehen
                    config.setEmissionRate(config.getEmissionRate() + 2);
                    config.setParticleSize(0.08f, 0.25f);
                    config.setParticleSpeed(0.5f, 1.2f);
                    config.setColor(0.7f, 0.7f, 0.6f);
                    break;
                    
                case "IdleLayer":
                    // Fast keine Partikel im Idle-Zustand
                    config.setEmissionRate(0);
                    break;
                    
                default: // BasicWalkingLayer und andere
                    // Standardwerte
                    config.setParticleSpeed(0.3f, 0.8f);
                    config.setColor(0.6f, 0.6f, 0.5f); // Bräunlicher Staub
                    break;
            }
            
            // Aktualisiere den Emitter
            particleSystem.updateEmitter(emitterId, position, config);
        } else {
            // Wenn der Charakter still steht, entferne den Emitter
            if (emitterId != null) {
                particleSystem.removeEmitter(emitterId);
                characterDustEmitters.remove(characterId);
            }
        }
    }
    
    /**
     * Wird aufgerufen, wenn ein Charakter das aktive Layer wechselt.
     */
    private void onLayerChanged(UUID characterId, String oldLayerType, String newLayerType, MotionState state) {
        logger.debug("Layer-Wechsel für Charakter {}: {} -> {}", characterId, oldLayerType, newLayerType);
        
        // Erzeuge einen Partikeleffekt für den Layer-Wechsel
        ParticleConfig config = ParticleConfig.createLayerTransitionConfig(newLayerType);
        UUID transitionEmitterId = UUID.randomUUID();
        
        particleSystem.updateEmitter(transitionEmitterId, state.getPosition(), config);
        
        // Plane das Entfernen des Emitters nach kurzer Zeit
        updateScheduler.schedule(() -> {
            particleSystem.removeEmitter(transitionEmitterId);
        }, 300, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Erstellt einen Partikeleffekt, wenn ein Charakter ein Ziel erreicht.
     * 
     * @param characterId ID des Charakters
     * @param position Position des Ziels
     */
    public void createGoalReachedEffect(UUID characterId, Position position) {
        if (!enabled) {
            return;
        }
        
        // Erzeuge einen Glitzer-Effekt
        UUID emitterId = particleSystem.createGoalReachedEmitter(position, 30);
        
        // Plane das Entfernen des Emitters nach der Lebensdauer der Partikel
        updateScheduler.schedule(() -> {
            particleSystem.removeEmitter(emitterId);
        }, 2000, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Führt einen animierten Übergang zwischen zwei Bewegungszuständen durch.
     * 
     * @param fromState Ausgangszustand
     * @param toState Zielzustand
     * @param onUpdate Callback für die Aktualisierung des Zustands
     * @param onComplete Callback nach Abschluss der Animation
     */
    public void animateTransition(MotionState fromState, MotionState toState, 
                                 java.util.function.Consumer<MotionState> onUpdate,
                                 Runnable onComplete) {
        if (!enabled) {
            // Bei deaktivierten Effekten direkt den Zielzustand setzen
            onUpdate.accept(toState);
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        
        // Wähle eine geeignete Easing-Funktion basierend auf der Geschwindigkeitsänderung
        EasingFunctions.EasingType easingType;
        
        if (toState.getSpeed() > fromState.getSpeed()) {
            // Beschleunigung
            easingType = EasingFunctions.EasingType.EASE_IN_CUBIC;
        } else if (toState.getSpeed() < fromState.getSpeed()) {
            // Abbremsen
            easingType = EasingFunctions.EasingType.EASE_OUT_CUBIC;
        } else {
            // Gleichbleibende Geschwindigkeit
            easingType = EasingFunctions.EasingType.EASE_IN_OUT_QUAD;
        }
        
        // Führe die Animation durch
        transitionAnimator.animateTransition(fromState, toState, 600, easingType, onUpdate, onComplete);
    }
    
    /**
     * Gibt alle aktiven Footsteps für einen Charakter zurück.
     * 
     * @param characterId ID des Charakters
     * @return Liste der aktiven Fußspuren
     */
    public List<Footstep> getFootsteps(UUID characterId) {
        return footstepVisualizer.getFootsteps(characterId);
    }
    
    /**
     * Gibt alle aktiven Partikel im System zurück.
     * 
     * @return Liste aller aktiven Partikel
     */
    public List<Particle> getAllParticles() {
        return particleSystem.getAllParticles();
    }
    
    /**
     * Aktiviert oder deaktiviert die visuellen Effekte.
     * 
     * @param enabled true zum Aktivieren, false zum Deaktivieren
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        
        if (!enabled) {
            // Bereinige alle Effekte
            footstepVisualizer.clearAllFootsteps();
            particleSystem.clearAll();
        }
    }
    
    /**
     * Bereinigt alle Ressourcen, wenn der Controller nicht mehr benötigt wird.
     */
    public void shutdown() {
        updateScheduler.shutdown();
        transitionAnimator.shutdown();
    }
}
