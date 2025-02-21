package com.example.motion.services;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.services.MotionCallback;
import com.example.motion.sys.behavior.IMotionLayer;
import com.example.motion.sys.data.IMotionDataRepository;
import com.example.motion.sys.model.*;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
public class CharacterMotionServiceImpl implements ICharacterMotionService {
    // ... Rest des Codes bleibt gleich ...

    @Override
    public void registerMotionCallback(UUID characterId, MotionCallback callback) {
        motionCallbacks.put(characterId, callback);
    }
}
