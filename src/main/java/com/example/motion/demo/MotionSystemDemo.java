package com.example.motion.demo;

import com.example.motion.data.InMemoryMotionDataRepository;
import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.interfaces.IMotionDataRepository;
import com.example.motion.layers.BasicWalkingLayer;
import com.example.motion.layers.RunningLayer;
import com.example.motion.layers.IdleLayer;
import com.example.motion.model.*;
import com.example.motion.services.CharacterMotionServiceImpl;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * Demonstrationsprogramm für das Motion-System.
 */
public class MotionSystemDemo {

    public static void main(String[] args) {
        // Repository und Service initialisieren
        IMotionDataRepository repository = new InMemoryMotionDataRepository();
        ICharacterMotionService motionService = new CharacterMotionServiceImpl(repository);

        // Motion Layer erstellen und registrieren
        IdleLayer idleLayer = new IdleLayer();
        BasicWalkingLayer walkingLayer = new BasicWalkingLayer();
        RunningLayer runningLayer = new RunningLayer();

        motionService.addMotionLayer(idleLayer, 1);
        motionService.addMotionLayer(walkingLayer, 2);
        motionService.addMotionLayer(runningLayer, 3);

        // Beispiel-Animation erstellen
        createSampleAnimations(repository);

        // Testcharakter erstellen
        UUID characterId = UUID.randomUUID();

        // Bewegungs-Callback registrieren
        motionService.registerMotionCallback(characterId, (id, state) -> {
            System.out.printf("Character %s moved to position %s with speed %.2f%n",
                id, state.getPosition(), state.getSpeed());
        });

        try {
            // Demo-Sequenz ausführen
            demonstrateMotionSequence(motionService, characterId);
        } catch (Exception e) {
            System.err.println("Error during demo: " + e.getMessage());
        } finally {
            ((CharacterMotionServiceImpl) motionService).shutdownAnimator(); // Pe84c
        }
    }

    private static void createSampleAnimations(IMotionDataRepository repository) {
        // Idle-Animation
        List<AnimationData.KeyFrame> idleFrames = new ArrayList<>();
        idleFrames.add(new AnimationData.KeyFrame(0.0f,
            new Position(0, 0, 0),
            new Rotation(0, 0, 0)));
        idleFrames.add(new AnimationData.KeyFrame(1.0f,
            new Position(0, 0.1f, 0),
            new Rotation(2, 0, 0)));
        idleFrames.add(new AnimationData.KeyFrame(2.0f,
            new Position(0, 0, 0),
            new Rotation(0, 0, 0)));

        AnimationData idleAnimation = new AnimationData(
            "idle_breathing",
            "Idle Breathing",
            2.0f,
            idleFrames,
            AnimationData.AnimationType.IDLE,
            true
        );

        // Walk-Animation
        List<AnimationData.KeyFrame> walkFrames = new ArrayList<>();
        walkFrames.add(new AnimationData.KeyFrame(0.0f,
            new Position(0, 0, 0),
            new Rotation(0, 0, 0)));
        walkFrames.add(new AnimationData.KeyFrame(0.5f,
            new Position(0.5f, 0.1f, 0),
            new Rotation(0, 0, 5)));
        walkFrames.add(new AnimationData.KeyFrame(1.0f,
            new Position(1.0f, 0, 0),
            new Rotation(0, 0, 0)));

        AnimationData walkAnimation = new AnimationData(
            "basic_walk",
            "Basic Walk Cycle",
            1.0f,
            walkFrames,
            AnimationData.AnimationType.WALK,
            true
        );

        repository.saveAnimationData("idle_breathing", idleAnimation);
        repository.saveAnimationData("basic_walk", walkAnimation);
    }

    private static void demonstrateMotionSequence(ICharacterMotionService service, UUID characterId) 
            throws InterruptedException {
        
        System.out.println("Starting motion demonstration...");

        // Idle-Animation abspielen
        System.out.println("\nPlaying idle animation...");
        service.playAnimation(characterId, "idle_breathing", 1.0f)
            .thenAccept(state -> System.out.println("Idle animation started"))
            .join();
        Thread.sleep(3000);

        // Laufbewegung starten
        System.out.println("\nStarting walk motion...");
        Direction walkDirection = new Direction(new Vector3D(1, 0, 0));
        service.setMovementDirection(characterId, walkDirection, 1.0f)
            .thenAccept(state -> System.out.println("Walk motion started"))
            .join();
        Thread.sleep(2000);

        // Walk-Animation mit Bewegung kombinieren
        System.out.println("\nPlaying walk animation with movement...");
        service.playAnimation(characterId, "basic_walk", 1.0f)
            .thenAccept(state -> System.out.println("Walk animation started"))
            .join();
        Thread.sleep(3000);

        // Richtung ändern
        System.out.println("\nChanging direction...");
        Direction newDirection = new Direction(new Vector3D(0, 0, 1));
        service.setMovementDirection(characterId, newDirection, 1.0f)
            .thenAccept(state -> System.out.println("Direction changed"))
            .join();
        Thread.sleep(2000);

        // Bewegung stoppen
        System.out.println("\nStopping all motion...");
        service.stopMotion(characterId)
            .thenAccept(state -> System.out.println("Motion stopped"))
            .join();
        Thread.sleep(1000);

        System.out.println("\nDemonstration complete!");
    }
}
