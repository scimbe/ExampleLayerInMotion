package com.example.motion.visual.animation;

import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.Position;
import com.example.motion.sys.model.Rotation;
import com.example.motion.visual.animation.EasingFunctions.EasingType;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * TransitionAnimator verwaltet interpolierte Übergänge zwischen verschiedenen Bewegungszuständen.
 * Dies erzeugt flüssigere und natürlichere Animationen beim Wechsel zwischen Lagen oder Bewegungen.
 */
public class TransitionAnimator {
    private static final long DEFAULT_TRANSITION_DURATION = 600; // ms
    private static final int DEFAULT_FPS = 60;
    private static final int DEFAULT_THREAD_POOL_SIZE = 2;
    
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<UUID, AnimationTask> activeTasks;
    
    public TransitionAnimator() {
        this.scheduler = new ScheduledThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE);
        this.activeTasks = new ConcurrentHashMap<>();
    }
    
    /**
     * Animiert einen Übergang zwischen zwei Bewegungszuständen mit Standard-Einstellungen.
     *
     * @param fromState Ausgangszustand
     * @param toState Zielzustand
     * @param onUpdate Callback für Animation-Updates
     * @param onComplete Callback nach Abschluss der Animation
     * @return ID der Animation (kann zum Abbrechen verwendet werden)
     */
    public String animateTransition(MotionState fromState, MotionState toState, 
                                   Consumer<MotionState> onUpdate, Runnable onComplete) {
        return animateTransition(fromState, toState, DEFAULT_TRANSITION_DURATION, EasingType.EASE_OUT_CUBIC, 
                                onUpdate, onComplete);
    }
    
    /**
     * Animiert einen Übergang zwischen zwei Bewegungszuständen mit benutzerdefinierten Einstellungen.
     *
     * @param fromState Ausgangszustand
     * @param toState Zielzustand
     * @param duration Dauer der Animation in Millisekunden
     * @param easingType Typ der Ease-Funktion für die Animation
     * @param onUpdate Callback für Animation-Updates
     * @param onComplete Callback nach Abschluss der Animation
     * @return ID der Animation (kann zum Abbrechen verwendet werden)
     */
    public String animateTransition(MotionState fromState, MotionState toState, 
                                   long duration, EasingType easingType,
                                   Consumer<MotionState> onUpdate, Runnable onComplete) {
        // Zuerst laufende Animation für diesen Charakter abbrechen
        cancelAnimation(fromState.getCharacterId());
        
        // Animations-ID erstellen
        String animationId = UUID.randomUUID().toString();
        
        // Neue Animationsaufgabe erstellen
        AnimationTask task = new AnimationTask(
            animationId,
            fromState,
            toState,
            duration,
            easingType,
            onUpdate,
            onComplete
        );
        
        // Animation starten
        activeTasks.put(fromState.getCharacterId(), task);
        task.start();
        
        return animationId;
    }
    
    /**
     * Bricht eine laufende Animation für einen Charakter ab.
     *
     * @param characterId ID des Charakters
     */
    public void cancelAnimation(UUID characterId) {
        AnimationTask task = activeTasks.get(characterId);
        if (task != null) {
            task.cancel();
            activeTasks.remove(characterId);
        }
    }
    
    /**
     * Beendet alle laufenden Animationen und bereinigt Ressourcen.
     */
    public void shutdown() {
        activeTasks.values().forEach(AnimationTask::cancel);
        activeTasks.clear();
        scheduler.shutdown();
    }
    
    /**
     * Hilfsmethode zur Interpolation von MotionState-Objekten.
     */
    private MotionState interpolateState(MotionState start, MotionState end, float factor, EasingType easingType) {
        // Ease-Wert berechnen
        float easedFactor = EasingFunctions.ease(factor, easingType);
        
        // Position interpolieren
        Position startPos = start.getPosition();
        Position endPos = end.getPosition();
        Position interpolatedPos = new Position(
            startPos.getX() + (endPos.getX() - startPos.getX()) * easedFactor,
            startPos.getY() + (endPos.getY() - startPos.getY()) * easedFactor,
            startPos.getZ() + (endPos.getZ() - startPos.getZ()) * easedFactor
        );
        
        // Rotation interpolieren
        Rotation startRot = start.getRotation();
        Rotation endRot = end.getRotation();
        Rotation interpolatedRot = new Rotation(
            startRot.getPitch() + (endRot.getPitch() - startRot.getPitch()) * easedFactor,
            startRot.getYaw() + (endRot.getYaw() - startRot.getYaw()) * easedFactor,
            startRot.getRoll() + (endRot.getRoll() - startRot.getRoll()) * easedFactor
        );
        
        // Geschwindigkeit interpolieren
        float interpolatedSpeed = start.getSpeed() + (end.getSpeed() - start.getSpeed()) * easedFactor;
        
        // Interpolierten Zustand zurückgeben
        return new MotionState(
            start.getCharacterId(),
            interpolatedPos,
            interpolatedRot,
            interpolatedSpeed
        );
    }
    
    /**
     * Interne Klasse, die einen einzelnen Animationsvorgang repräsentiert.
     */
    private class AnimationTask {
        private final String id;
        private final MotionState startState;
        private final MotionState endState;
        private final long duration;
        private final EasingType easingType;
        private final Consumer<MotionState> onUpdate;
        private final Runnable onComplete;
        
        private ScheduledFuture<?> future;
        private long startTime;
        
        /**
         * Erstellt eine neue Animationsaufgabe.
         */
        public AnimationTask(String id, MotionState startState, MotionState endState, 
                            long duration, EasingType easingType,
                            Consumer<MotionState> onUpdate, Runnable onComplete) {
            this.id = id;
            this.startState = startState;
            this.endState = endState;
            this.duration = duration;
            this.easingType = easingType;
            this.onUpdate = onUpdate;
            this.onComplete = onComplete;
        }
        
        /**
         * Startet die Animation.
         */
        public void start() {
            startTime = System.currentTimeMillis();
            long frameInterval = 1000 / DEFAULT_FPS;
            
            future = scheduler.scheduleAtFixedRate(this::update, 0, frameInterval, TimeUnit.MILLISECONDS);
        }
        
        /**
         * Aktualisiert den Animationszustand.
         */
        private void update() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            
            if (elapsedTime >= duration) {
                // Animation ist abgeschlossen
                onUpdate.accept(endState);
                cancel();
                
                if (onComplete != null) {
                    onComplete.run();
                }
                
                activeTasks.remove(startState.getCharacterId());
                return;
            }
            
            // Faktor zwischen 0 und 1 berechnen
            float factor = (float) elapsedTime / duration;
            
            // Interpolierten Zustand berechnen
            MotionState interpolatedState = interpolateState(startState, endState, factor, easingType);
            
            // Callback aufrufen
            onUpdate.accept(interpolatedState);
        }
        
        /**
         * Bricht die Animation ab.
         */
        public void cancel() {
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
        }
    }
}
