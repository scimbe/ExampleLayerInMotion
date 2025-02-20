package com.example.motion.services;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.interfaces.IMotionLayer;
import com.example.motion.interfaces.IMotionDataRepository;
import com.example.motion.model.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Implementierung des Character Motion Service mit Layer-Management.
 */
public class CharacterMotionServiceImpl implements ICharacterMotionService {
    
    private final IMotionDataRepository repository;
    private final Map<UUID, MotionState> characterStates;
    private final Map<UUID, MotionCallback> motionCallbacks;
    private final Map<UUID, AnimationPlayback> activeAnimations;
    private final Map<IMotionLayer, Integer> motionLayers;
    private final ScheduledExecutorService animator;
    private final ReentrantReadWriteLock layerLock;

    // Rest of the code remains the same
    ...
}