package com.example.motion.config;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.behavior.IMotionLayer;
import com.example.motion.sys.data.InMemoryMotionDataRepository;
import com.example.motion.sys.data.IMotionDataRepository;
import com.example.motion.services.CharacterMotionServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MotionConfig {

    /**
     * Diese Bean-Definition wird entfernt, da die Klasse InMemoryMotionDataRepository
     * bereits mit @Repository annotiert ist und automatisch als Bean erkannt wird.
     * Wenn zwei Implementierungen benötigt werden, können wir eine mit @Primary markieren
     * oder Qualifier verwenden.
     */
    /*
    @Bean
    public IMotionDataRepository motionDataRepository() {
        return new InMemoryMotionDataRepository();
    }
    */

    @Bean
    public ICharacterMotionService characterMotionService(IMotionDataRepository repository) {
        return new CharacterMotionServiceImpl(repository);
    }
}