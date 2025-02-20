package com.example.motion.interfaces;

import com.example.motion.model.*;
import java.util.UUID;

public interface IMotionLayer {
    MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime);
    CollisionData checkCollision(UUID characterId, MotionState motionState);
    MotionState processPhysics(UUID characterId, PhysicsData physicsData);
    boolean validateMotionState(MotionState motionState);
    MotionState interpolateStates(MotionState start, MotionState end, float factor);
}