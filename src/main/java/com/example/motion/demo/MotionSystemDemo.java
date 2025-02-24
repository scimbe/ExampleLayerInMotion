package com.example.motion.demo;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.data.IMotionDataRepository;
import com.example.motion.sys.data.InMemoryMotionDataRepository;
import com.example.motion.services.CharacterMotionServiceImpl;
import com.example.motion.sys.behavior.BasicMotionLayer;
import com.example.motion.sys.behavior.IdleLayer;
import com.example.motion.sys.behavior.RunningLayer;
import com.example.motion.sys.model.Direction;
import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.AnimationData;
import com.example.motion.sys.model.Position;
import com.example.motion.sys.model.Rotation;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MotionSystemDemo {
    
    public static void main(String[] args) {
        // Repository erstellen
        InMemoryMotionDataRepository repository = new InMemoryMotionDataRepository();
        
        // Demo-Animationen erstellen
        createSampleAnimations(repository);
        
        // Service erstellen
        ICharacterMotionService motionService = new CharacterMotionServiceImpl(repository);
        
        // Character ID generieren
        UUID characterId = UUID.randomUUID();
        
        // Layers hinzufügen
        IdleLayer idleLayer = new IdleLayer();
        BasicMotionLayer basicLayer = new BasicMotionLayer();
        RunningLayer runningLayer = new RunningLayer();
        
        motionService.addMotionLayer(idleLayer, 1);
        motionService.addMotionLayer(basicLayer, 2);
        motionService.addMotionLayer(runningLayer, 3);
        
        // Bewegungssequenz demonstrieren
        demonstrateMotionSequence(motionService, characterId);
    }
    
    private static void createSampleAnimations(IMotionDataRepository repository) {
        // Idle-Animation
        AnimationData idleAnimation = new AnimationData("idle_breathing", 2.0f, true);
        
        // Keyframes hinzufügen
        idleAnimation.addKeyframe(new AnimationData.AnimationKeyframe(
            0.0f,
            new Position(0, 0, 0),
            new Rotation(0, 0, 0)
        ));
        
        idleAnimation.addKeyframe(new AnimationData.AnimationKeyframe(
            1.0f,
            new Position(0, 0.05f, 0),
            new Rotation(2, 0, 0)
        ));
        
        idleAnimation.addKeyframe(new AnimationData.AnimationKeyframe(
            2.0f,
            new Position(0, 0, 0),
            new Rotation(0, 0, 0)
        ));
        
        // Geh-Animation
        AnimationData walkAnimation = new AnimationData("basic_walk", 1.0f, true);
        
        // Keyframes hinzufügen
        walkAnimation.addKeyframe(new AnimationData.AnimationKeyframe(
            0.0f,
            new Position(0, 0, 0),
            new Rotation(-2, 0, -3)
        ));
        
        walkAnimation.addKeyframe(new AnimationData.AnimationKeyframe(
            0.5f,
            new Position(0, 0.1f, 0),
            new Rotation(2, 0, 3)
        ));
        
        walkAnimation.addKeyframe(new AnimationData.AnimationKeyframe(
            1.0f,
            new Position(0, 0, 0),
            new Rotation(-2, 0, -3)
        ));
        
        // Animationsdaten speichern
        repository.saveAnimationData(idleAnimation);
        repository.saveAnimationData(walkAnimation);
    }
    
    private static void demonstrateMotionSequence(ICharacterMotionService motionService, UUID characterId) {
        try {
            // Callback registrieren für Status-Ausgaben
            motionService.registerMotionCallback(characterId, (id, state) -> {
                System.out.println("Position: " + state.getPosition() + ", Geschwindigkeit: " + state.getSpeed());
            });
            
            // Idle-Animation abspielen
            System.out.println("\nIdle-Animation starten...");
            CompletableFuture<MotionState> idleFuture = motionService.playAnimation(characterId, "idle_breathing", 1.0f);
            idleFuture.join();
            Thread.sleep(3000);
            
            // Laufbewegung starten
            System.out.println("\nLaufbewegung starten...");
            Direction walkDirection = new Direction(1, 0, 0);
            CompletableFuture<MotionState> walkFuture = motionService.setMovementDirection(characterId, walkDirection, 1.0f);
            walkFuture.join();
            Thread.sleep(2000);
            
            // Animation mit Bewegung kombinieren
            System.out.println("\nWalk-Animation mit Bewegung kombinieren...");
            CompletableFuture<MotionState> combinedFuture = motionService.playAnimation(characterId, "basic_walk", 1.0f);
            combinedFuture.join();
            Thread.sleep(3000);
            
            // Richtung ändern
            System.out.println("\nRichtung ändern...");
            Direction newDirection = new Direction(0, 0, 1);
            CompletableFuture<MotionState> directionFuture = motionService.setMovementDirection(characterId, newDirection, 1.0f);
            directionFuture.join();
            Thread.sleep(2000);
            
            // Bewegung stoppen
            System.out.println("\nBewegung stoppen...");
            CompletableFuture<MotionState> stopFuture = motionService.stopMotion(characterId);
            MotionState finalState = stopFuture.join();
            
            System.out.println("\nDemo abgeschlossen!");
            System.out.println("Endposition: " + finalState.getPosition());
            
        } catch (Exception e) {
            System.err.println("Fehler während der Demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}