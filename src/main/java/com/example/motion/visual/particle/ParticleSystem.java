package com.example.motion.visual.particle;

import com.example.motion.sys.model.Position;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Verwaltet und rendert Partikelsysteme für visuelle Effekte wie Staub beim Laufen
 * oder Glitzereffekte bei Zielerfassung.
 */
public class ParticleSystem {
    private static final int DEFAULT_MAX_PARTICLES = 500;
    private static final Random random = new Random();
    
    @Getter
    private final ConcurrentHashMap<UUID, List<Particle>> activeEmitters;
    private final int maxParticles;
    
    /**
     * Erstellt ein neues Partikelsystem mit den Standardeinstellungen.
     */
    public ParticleSystem() {
        this(DEFAULT_MAX_PARTICLES);
    }
    
    /**
     * Erstellt ein neues Partikelsystem mit benutzerdefinierten Einstellungen.
     * 
     * @param maxParticles Maximale Anzahl von Partikeln im System
     */
    public ParticleSystem(int maxParticles) {
        this.activeEmitters = new ConcurrentHashMap<>();
        this.maxParticles = maxParticles;
    }
    
    /**
     * Erstellt einen neuen Partikelemitter für Staubeffekte beim Laufen/Gehen.
     * 
     * @param characterId ID des Charakters
     * @param position Position des Emitters
     * @param speed Geschwindigkeit des Charakters
     * @param layerType Typ des aktiven Bewegungslayers
     * @return ID des erstellten Emitters
     */
    public UUID createDustEmitter(UUID characterId, Position position, float speed, String layerType) {
        // Konfiguriere Partikel basierend auf Layer-Typ und Geschwindigkeit
        ParticleConfig config = new ParticleConfig();
        
        config.setEmissionRate(Math.min(10, Math.max(1, (int)(speed * 5))));
        config.setParticleLifetime(1500);
        config.setParticleSize(0.05f, 0.2f);
        config.setSpread(15.0f);
        config.setGravity(0.05f);
        config.setStartAlpha(0.7f);
        config.setEndAlpha(0.0f);
        
        // Anpassungen basierend auf Layer-Typ
        switch (layerType) {
            case "RunningLayer":
                // Mehr, größere und schnellere Partikel beim Laufen
                config.setEmissionRate(config.getEmissionRate() * 2);
                config.setParticleSize(0.1f, 0.3f);
                config.setParticleSpeed(0.8f, 1.5f);
                config.setColor(0.8f, 0.8f, 0.7f); // Hellerer Staub
                break;
                
            case "AdvancedWalkingLayer":
                // Angepasste Partikel für fortgeschrittenes Gehen
                config.setEmissionRate(config.getEmissionRate() + 2);
                config.setParticleSize(0.08f, 0.25f);
                config.setParticleSpeed(0.5f, 1.2f);
                config.setColor(0.7f, 0.7f, 0.6f);
                break;
                
            case "IdleLayer":
                // Fast keine Partikel im Idle-Zustand
                config.setEmissionRate(0);
                break;
                
            default: // BasicWalkingLayer und andere
                // Standardwerte
                config.setParticleSpeed(0.3f, 0.8f);
                config.setColor(0.6f, 0.6f, 0.5f); // Bräunlicher Staub
                break;
        }
        
        // Erstelle den Emitter nur, wenn die Geschwindigkeit über dem Schwellenwert liegt
        if (speed < 0.1f) {
            config.setEmissionRate(0);
        }
        
        // Erstelle und registriere den Emitter
        UUID emitterId = UUID.randomUUID();
        activeEmitters.put(emitterId, new ArrayList<>());
        
        return emitterId;
    }
    
    /**
     * Erstellt einen neuen Partikelemitter für Glitzereffekte bei Zielerfassung.
     * 
     * @param position Position des Emitters
     * @param burstSize Anzahl der Partikel im Burst
     * @return ID des erstellten Emitters
     */
    public UUID createGoalReachedEmitter(Position position, int burstSize) {
        ParticleConfig config = new ParticleConfig();
        
        config.setEmissionRate(0); // Einmaliger Burst
        config.setParticleLifetime(2000);
        config.setParticleSize(0.08f, 0.2f);
        config.setParticleSpeed(0.5f, 2.0f);
        config.setSpread(360.0f); // In alle Richtungen
        config.setGravity(-0.02f); // Leicht aufsteigend
        config.setStartAlpha(1.0f);
        config.setEndAlpha(0.0f);
        config.setColor(1.0f, 0.8f, 0.2f); // Goldenes Glitzern
        
        // Erstelle und registriere den Emitter
        UUID emitterId = UUID.randomUUID();
        List<Particle> particles = new ArrayList<>();
        activeEmitters.put(emitterId, particles);
        
        // Erzeuge den initialen Burst von Partikeln
        for (int i = 0; i < burstSize; i++) {
            particles.add(createParticle(position, config));
        }
        
        return emitterId;
    }
    
    /**
     * Aktualisiert das Partikelsystem und bewegt alle aktiven Partikel.
     * 
     * @param deltaTime Vergangene Zeit seit dem letzten Update in Sekunden
     */
    public void update(float deltaTime) {
        // Iteriere über alle Emitter
        for (UUID emitterId : activeEmitters.keySet()) {
            List<Particle> particles = activeEmitters.get(emitterId);
            
            // Aktualisiere existierende Partikel
            Iterator<Particle> iterator = particles.iterator();
            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                
                // Aktualisiere die Partikelposition
                particle.update(deltaTime);
                
                // Entferne abgelaufene Partikel
                if (particle.isExpired()) {
                    iterator.remove();
                }
            }
        }
        
        // Entferne leere Emitter
        activeEmitters.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * Gibt eine Liste aller aktiven Partikel für alle Emitter zurück.
     * 
     * @return Liste aller aktiven Partikel
     */
    public List<Particle> getAllParticles() {
        List<Particle> allParticles = new ArrayList<>();
        
        for (List<Particle> particles : activeEmitters.values()) {
            allParticles.addAll(particles);
        }
        
        return allParticles;
    }
    
    /**
     * Gibt die Anzahl der aktiven Partikel im System zurück.
     * 
     * @return Anzahl der aktiven Partikel
     */
    public int getActiveParticleCount() {
        int count = 0;
        
        for (List<Particle> particles : activeEmitters.values()) {
            count += particles.size();
        }
        
        return count;
    }
    
    /**
     * Aktualisiert einen kontinuierlichen Emitter an einer neuen Position und erzeugt neue Partikel.
     * 
     * @param emitterId ID des Emitters
     * @param position Neue Position des Emitters
     * @param config Konfiguration für die Partikel
     */
    public void updateEmitter(UUID emitterId, Position position, ParticleConfig config) {
        List<Particle> particles = activeEmitters.get(emitterId);
        
        if (particles == null) {
            return;
        }
        
        // Begrenze die Gesamtzahl der Partikel im System
        if (getActiveParticleCount() >= maxParticles) {
            return;
        }
        
        // Erzeuge neue Partikel entsprechend der Emissionsrate
        for (int i = 0; i < config.getEmissionRate(); i++) {
            particles.add(createParticle(position, config));
        }
    }
    
    /**
     * Erzeugt ein neues Partikel mit den angegebenen Eigenschaften.
     */
    private Particle createParticle(Position position, ParticleConfig config) {
        // Zufällige Größe im angegebenen Bereich
        float size = config.getMinSize() + random.nextFloat() * (config.getMaxSize() - config.getMinSize());
        
        // Zufällige Geschwindigkeit im angegebenen Bereich
        float speed = config.getMinSpeed() + random.nextFloat() * (config.getMaxSpeed() - config.getMinSpeed());
        
        // Zufällige Richtung im angegebenen Spread-Bereich
        float angle = random.nextFloat() * config.getSpread() - (config.getSpread() / 2);
        float dirX = (float) Math.sin(Math.toRadians(angle));
        float dirY = (float) Math.cos(Math.toRadians(angle));
        
        // Erstelle und gib das Partikel zurück
        return new Particle(
            UUID.randomUUID(),
            position.getX(), position.getY(), position.getZ(),
            dirX * speed, dirY * speed, 0,
            0, config.getGravity(), 0,
            size,
            config.getStartAlpha(),
            config.getEndAlpha(),
            config.getRed(), config.getGreen(), config.getBlue(),
            System.currentTimeMillis(),
            config.getParticleLifetime()
        );
    }
    
    /**
     * Führt eine Aktion für jeden aktiven Partikel aus (z.B. für Rendering).
     * 
     * @param action Aktion, die für jeden Partikel ausgeführt werden soll
     */
    public void forEachParticle(Consumer<Particle> action) {
        for (List<Particle> particles : activeEmitters.values()) {
            for (Particle particle : particles) {
                action.accept(particle);
            }
        }
    }
    
    /**
     * Entfernt alle Partikel für einen bestimmten Emitter.
     * 
     * @param emitterId ID des Emitters
     */
    public void removeEmitter(UUID emitterId) {
        activeEmitters.remove(emitterId);
    }
    
    /**
     * Entfernt alle Partikel und Emitter.
     */
    public void clearAll() {
        activeEmitters.clear();
    }
}
