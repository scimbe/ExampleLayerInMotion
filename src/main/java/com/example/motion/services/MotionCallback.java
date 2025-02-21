package com.example.motion.services;

import com.example.motion.sys.model.MotionState;
import java.util.UUID;

@FunctionalInterface
public interface MotionCallback {
    void onMotionUpdate(UUID characterId, MotionState newState);
}
