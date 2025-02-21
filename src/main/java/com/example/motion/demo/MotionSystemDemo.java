package com.example.motion.demo;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.data.InMemoryMotionDataRepository;
import com.example.motion.services.CharacterMotionServiceImpl;
import com.example.motion.sys.behavior.BasicMotionLayer;
import com.example.motion.sys.model.Direction;
import com.example.motion.sys.model.MotionState;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MotionSystemDemo {
    
    public static void main(String[] args) {
        // Repository erstellen
        InMemoryMotionDataRepository repository = new InMemoryMotionDataRepository();
        
        // Service erstellen
        ICharacterMotionService motionService = new CharacterMotionServiceImpl(repository);
        
        // Character ID generieren
        UUID characterId = UUID.randomUUID();
        
        // Basic Layer hinzufügen
        BasicMotionLayer basicLayer = new BasicMotionLayer();
        motionService.addMotionLayer(basicLayer, 0);
        
        // Bewegung ausführen
        CompletableFuture<MotionState> motionFuture = motionService.setMovementDirection(
            characterId,
            new Direction(1, 0, 0),
            1.0f
        );
        
        // Auf Bewegungsabschluss warten
        try {
            MotionState finalState = motionFuture.get();
            System.out.println("Bewegung abgeschlossen!");
            System.out.println("Finale Position: " + finalState.getPosition());
        } catch (Exception e) {
            System.err.println("Fehler während der Bewegung: " + e.getMessage());
        }
    }
}
