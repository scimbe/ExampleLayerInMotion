package com.example.motion.demo;

import com.example.motion.behavior.AdvancedWalkingLayer;
import com.example.motion.behavior.BasicWalkingLayer;
import com.example.motion.data.InMemoryMotionDataRepository;
import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.interfaces.IMotionDataRepository;
import com.example.motion.model.*;
import com.example.motion.services.CharacterMotionServiceImpl;

import java.util.UUID;

/**
 * Demonstriert den dynamischen Wechsel zwischen verschiedenen Motion Layers.
 */
public class LayerSwitchingDemo {

    public static void main(String[] args) {
        try {
            // Initialisierung
            IMotionDataRepository repository = new InMemoryMotionDataRepository();
            ICharacterMotionService motionService = new CharacterMotionServiceImpl(repository);

            // Layer erstellen
            BasicWalkingLayer basicLayer = new BasicWalkingLayer();
            AdvancedWalkingLayer advancedLayer = new AdvancedWalkingLayer();

            // Testcharakter erstellen
            UUID characterId = UUID.randomUUID();

            // Bewegungs-Callback für Statusausgaben
            motionService.registerMotionCallback(characterId, (id, state) -> {
                System.out.printf("Position: %s, Speed: %.2f%n",
                        state.getPosition(), state.getSpeed());
            });

            // Demo-Sequenz ausführen
            demonstrateLayerSwitching(motionService, characterId, basicLayer, advancedLayer);

        } catch (Exception e) {
            System.err.println("Error during demo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void demonstrateLayerSwitching(
            ICharacterMotionService service,
            UUID characterId,
            BasicWalkingLayer basicLayer,
            AdvancedWalkingLayer advancedLayer) throws InterruptedException {

        Direction walkDirection = new Direction(new Vector3D(1, 0, 0));

        // Start mit Basic Layer
        System.out.println("\n=== Starting with Basic Walking Layer ===");
        service.addMotionLayer(basicLayer, 1);

        // Normale Gehbewegung demonstrieren
        service.setMovementDirection(characterId, walkDirection, 0.5f).join();
        Thread.sleep(2000);

        // Zu Advanced Layer wechseln
        System.out.println("\n=== Switching to Advanced Walking Layer ===");
        service.removeMotionLayer(basicLayer);
        service.addMotionLayer(advancedLayer, 1);
        Thread.sleep(500);

        // Verschiedene Gangarten demonstrieren
        System.out.println("\n=== Demonstrating Normal Gait ===");
        advancedLayer.setGaitType(characterId, AdvancedWalkingLayer.GaitType.NORMAL);
        service.setMovementDirection(characterId, walkDirection, 0.5f).join();
        Thread.sleep(2000);

        System.out.println("\n=== Switching to Sneaking Gait ===");
        advancedLayer.setGaitType(characterId, AdvancedWalkingLayer.GaitType.SNEAKING);
        service.setMovementDirection(characterId, walkDirection, 0.3f).join();
        Thread.sleep(2000);

        System.out.println("\n=== Switching to Limping Gait ===");
        advancedLayer.setGaitType(characterId, AdvancedWalkingLayer.GaitType.LIMPING);
        service.setMovementDirection(characterId, walkDirection, 0.4f).join();
        Thread.sleep(2000);

        // Bewegung stoppen
        System.out.println("\n=== Stopping Movement ===");
        service.stopMotion(characterId).join();
        Thread.sleep(500);

        // Zurück zu Basic Layer
        System.out.println("\n=== Switching back to Basic Walking Layer ===");
        service.removeMotionLayer(advancedLayer);
        service.addMotionLayer(basicLayer, 1);

        // Finale Bewegung
        service.setMovementDirection(characterId, walkDirection, 0.5f).join();
        Thread.sleep(2000);

        service.stopMotion(characterId).join();
        System.out.println("\n=== Demo Complete ===");
    }
}
