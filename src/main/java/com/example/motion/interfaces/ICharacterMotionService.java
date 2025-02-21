package com.example.motion.interfaces;

import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.Direction;
import com.example.motion.sys.behavior.IMotionLayer;
import com.example.motion.services.MotionCallback;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ICharacterMotionService {
    CompletableFuture<MotionState> playAnimation(UUID characterId, String animationId, float speed);
    CompletableFuture<MotionState> setMovementDirection(UUID characterId, Direction direction, float speed);
    CompletableFuture<MotionState> stopMotion(UUID characterId);
    MotionState getMotionState(UUID characterId);
    boolean addMotionLayer(IMotionLayer layer, int priority);
    boolean removeMotionLayer(IMotionLayer layer);
    boolean updateLayerPriority(IMotionLayer layer, int priority);
    List<IMotionLayer> getActiveLayers();
    void registerMotionCallback(UUID characterId, MotionCallback callback);
}
