package com.example.motion.demo;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.data.InMemoryMotionDataRepository;
import com.example.motion.services.CharacterMotionServiceImpl;
import com.example.motion.sys.behavior.BasicMotionLayer;
import com.example.motion.sys.behavior.AdvancedWalkingLayer;
import com.example.motion.sys.model.Direction;
import com.example.motion.sys.model.MotionState;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LayerSwitchingDemo {
    
    public static void main(String[] args) {
        // Repository erstellen
        InMemoryMotionDataRepository repository = new InMemoryMotionDataRepository();
        
        // Service erstellen
        ICharacterMotionService motionService = new CharacterMotionServiceImpl(repository);
        
        // Character ID generieren
        UUID characterId = UUID.randomUUID();
        
        // Verschiedene Layer erstellen
        BasicMotionLayer basicLayer = new BasicMotionLayer();
        AdvancedWalkingLayer advancedLayer = new AdvancedWalkingLayer();
        
        // Layer-Wechsel demonstrieren
        demonstrateLayerSwitching(motionService, characterId, basicLayer, advancedLayer);
    }
    
    public static void demonstrateLayerSwitching(ICharacterMotionService motionService, 
                                               UUID characterId, 
                                               BasicMotionLayer basicLayer, 
                                               AdvancedWalkingLayer advancedLayer) {
        try {
            // Status-Callback registrieren
            motionService.registerMotionCallback(characterId, (id, state) -> {
                System.out.printf("Position: %s, Rotation: %s, Geschwindigkeit: %.2f%n",
                                  state.getPosition(), state.getRotation(), state.getSpeed());
            });
            
            // Basic Layer hinzufügen
            System.out.println("\nBasic Layer aktivieren...");
            motionService.addMotionLayer(basicLayer, 1);
            
            // Bewegung mit Basic Layer
            System.out.println("\nBewegung mit Basic Layer...");
            Direction walkDirection = new Direction(1, 0, 0);
            CompletableFuture<MotionState> basicMoveFuture = 
                motionService.setMovementDirection(characterId, walkDirection, 0.5f);
            basicMoveFuture.join();
            Thread.sleep(2000);
            
            // Zu Advanced Layer wechseln
            System.out.println("\nWechsel zu Advanced Layer...");
            motionService.removeMotionLayer(basicLayer);
            motionService.addMotionLayer(advancedLayer, 1);
            
            // Bewegung mit Advanced Layer - Normal Gait
            System.out.println("\nBewegung mit Advanced Layer (Normal Gait)...");
            advancedLayer.setGaitType(characterId, AdvancedWalkingLayer.GaitType.NORMAL);
            CompletableFuture<MotionState> normalGaitFuture = 
                motionService.setMovementDirection(characterId, walkDirection, 0.5f);
            normalGaitFuture.join();
            Thread.sleep(2000);
            
            // Gangart ändern - Sneaking
            System.out.println("\nWechsel zu Sneaking Gait...");
            advancedLayer.setGaitType(characterId, AdvancedWalkingLayer.GaitType.SNEAKING);
            CompletableFuture<MotionState> sneakingGaitFuture = 
                motionService.setMovementDirection(characterId, walkDirection, 0.3f);
            sneakingGaitFuture.join();
            Thread.sleep(2000);
            
            // Gangart ändern - Limping
            System.out.println("\nWechsel zu Limping Gait...");
            advancedLayer.setGaitType(characterId, AdvancedWalkingLayer.GaitType.LIMPING);
            CompletableFuture<MotionState> limpingGaitFuture = 
                motionService.setMovementDirection(characterId, walkDirection, 0.4f);
            limpingGaitFuture.join();
            Thread.sleep(2000);
            
            // Bewegung stoppen
            System.out.println("\nBewegung stoppen...");
            CompletableFuture<MotionState> stopFuture = motionService.stopMotion(characterId);
            stopFuture.join();
            Thread.sleep(1000);
            
            // Zurück zu Basic Layer
            System.out.println("\nZurück zu Basic Layer...");
            motionService.removeMotionLayer(advancedLayer);
            motionService.addMotionLayer(basicLayer, 1);
            
            // Finale Bewegung
            System.out.println("\nFinale Bewegung mit Basic Layer...");
            CompletableFuture<MotionState> finalMoveFuture = 
                motionService.setMovementDirection(characterId, walkDirection, 0.5f);
            MotionState finalState = finalMoveFuture.join();
            Thread.sleep(2000);
            
            // Demo abschließen
            CompletableFuture<MotionState> finalStopFuture = motionService.stopMotion(characterId);
            finalState = finalStopFuture.join();
            
            System.out.println("\nLayer Switching Demo erfolgreich abgeschlossen!");
            System.out.println("Endposition: " + finalState.getPosition());
            
        } catch (Exception e) {
            System.err.println("Fehler während der Demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}