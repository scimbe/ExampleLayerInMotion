package com.example.motion.demo;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.data.InMemoryMotionDataRepository;
import com.example.motion.services.CharacterMotionServiceImpl;
import com.example.motion.sys.behavior.BasicMotionLayer;
import com.example.motion.sys.model.Direction;

import java.util.UUID;

public class LayerSwitchingDemo {
    
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
        
        // Bewegung starten
        motionService.setMovementDirection(characterId, new Direction(1, 0, 0), 1.0f);
        
        System.out.println("Layer Switching Demo erfolgreich abgeschlossen!");
    }
}
