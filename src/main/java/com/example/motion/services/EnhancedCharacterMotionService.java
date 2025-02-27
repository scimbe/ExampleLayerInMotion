package com.example.motion.services;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.interfaces.MotionCallback;
import com.example.motion.sys.behavior.IMotionLayer;
import com.example.motion.sys.data.IMotionDataRepository;
import com.example.motion.sys.model.Direction;
import com.example.motion.sys.model.MotionState;
import com.example.motion.visual.VisualEffectsController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Erweiterte Implementierung des CharacterMotionService mit visuellen Effekten.
 * Dekoriert die Standard-Implementierung und fügt visuelle Erweiterungen hinzu.
 */
@Service
@Primary
public class EnhancedCharacterMotionService implements ICharacterMotionService {
    
    private final CharacterMotionServiceImpl baseService;
    private final VisualEffectsController visualEffects;
    
    /**
     * Erstellt einen neuen EnhancedCharacterMotionService.
     */
    @Autowired
    public EnhancedCharacterMotionService(IMotionDataRepository repository, VisualEffectsController visualEffects) {
        this.baseService = new CharacterMotionServiceImpl(repository);
        this.visualEffects = visualEffects;
    }
    
    @Override
    public boolean addMotionLayer(IMotionLayer layer, int priority) {
        return baseService.addMotionLayer(layer, priority);
    }
    
    @Override
    public boolean removeMotionLayer(IMotionLayer layer) {
        return baseService.removeMotionLayer(layer);
    }
    
    @Override
    public boolean updateLayerPriority(IMotionLayer layer, int priority) {
        return baseService.updateLayerPriority(layer, priority);
    }
    
    @Override
    public List<IMotionLayer> getActiveLayers() {
        return baseService.getActiveLayers();
    }
    
    @Override
    public CompletableFuture<MotionState> playAnimation(UUID characterId, String animationId, float speed) {
        MotionState initialState = getMotionState(characterId);
        
        return baseService.playAnimation(characterId, animationId, speed)
            .thenApply(finalState -> {
                // Visuelle Effekte für den Animationsübergang hinzufügen
                visualEffects.updateCharacterEffects(
                    characterId,
                    getActiveLayerType(),
                    finalState
                );
                
                return finalState;
            });
    }
    
    @Override
    public CompletableFuture<MotionState> setMovementDirection(UUID characterId, Direction direction, float speed) {
        MotionState initialState = getMotionState(characterId);
        
        return baseService.setMovementDirection(characterId, direction, speed)
            .thenApply(finalState -> {
                // Füge visuelle Effekte für die Bewegung hinzu
                visualEffects.updateCharacterEffects(
                    characterId,
                    getActiveLayerType(),
                    finalState
                );
                
                return finalState;
            });
    }
    
    @Override
    public CompletableFuture<MotionState> stopMotion(UUID characterId) {
        MotionState initialState = getMotionState(characterId);
        
        return baseService.stopMotion(characterId)
            .thenApply(finalState -> {
                // Aktualisiere visuelle Effekte beim Stoppen
                visualEffects.updateCharacterEffects(
                    characterId,
                    getActiveLayerType(),
                    finalState
                );
                
                return finalState;
            });
    }
    
    @Override
    public void registerMotionCallback(UUID characterId, MotionCallback callback) {
        // Wir registrieren einen erweiterten Callback, der sowohl den Original-Callback
        // als auch unsere visuelle Effekt-Logik ausführt
        baseService.registerMotionCallback(characterId, (id, state) -> {
            // Zuerst den Original-Callback ausführen
            callback.onMotionUpdate(id, state);
            
            // Dann die visuellen Effekte aktualisieren
            visualEffects.updateCharacterEffects(
                characterId,
                getActiveLayerType(),
                state
            );
        });
    }
    
    @Override
    public MotionState getMotionState(UUID characterId) {
        return baseService.getMotionState(characterId);
    }
    
    /**
     * Ermittelt den Typ des aktiven Layers.
     * 
     * @return Typ des aktiven Layers oder "BasicMotionLayer" als Fallback
     */
    private String getActiveLayerType() {
        List<IMotionLayer> layers = getActiveLayers();
        if (layers.isEmpty()) {
            return "BasicMotionLayer";
        }
        
        // Nimm den Layer mit der höchsten Priorität (der erste in der Liste)
        IMotionLayer activeLayer = layers.get(0);
        return activeLayer.getClass().getSimpleName();
    }
}
