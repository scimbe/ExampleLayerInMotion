package com.example.motion.config;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.interfaces.IMotionLayer;
import com.example.motion.data.InMemoryMotionDataRepository;
import com.example.motion.interfaces.IMotionDataRepository;
import com.example.motion.services.CharacterMotionServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MotionConfig {

    @Bean
    public IMotionDataRepository motionDataRepository() {
        return new InMemoryMotionDataRepository();
    }

    @Bean
    public ICharacterMotionService characterMotionService(IMotionDataRepository repository) {
        return new CharacterMotionServiceImpl(repository);
    }
}