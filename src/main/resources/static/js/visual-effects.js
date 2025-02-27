/**
 * visual-effects.js
 * Frontend-Komponente für die Darstellung und Verwaltung der visuellen Effekte
 */

import { gameState, canvas, ctx } from './game.js';

// Globale Variable für den Zugriff aus anderen Modulen
window.visualEffects = null;

// Initialisiert die visuellen Effekte
export function initVisualEffects(canvas) {
    const visualEffects = new VisualEffects(canvas);
    window.visualEffects = visualEffects;
    return visualEffects;
}

class VisualEffects {
    constructor(gameCanvas) {
        this.canvas = gameCanvas;
        this.ctx = gameCanvas.getContext('2d');
        
        // Initialisiere Effektor-Objekte
        this.footsteps = [];
        this.particles = [];
        
        // Animation IDs für requestAnimationFrame
        this.animationFrameId = null;
        
        // Konfiguration
        this.config = {
            enabled: true,
            footstepsEnabled: true,
            particlesEnabled: true,
            maxFootsteps: 30,
            maxParticles: 100,
            footstepFadeOutTime: 5000,
            smokeParticleLife: 1000,
            goalParticleLife: 2000
        };
        
        // Starte die Render-Schleife
        this.startRenderLoop();
    }
    
    /**
     * Startet die kontinuierliche Render-Schleife für die Effekte
     */
    startRenderLoop() {
        const renderLoop = () => {
            if (this.config.enabled) {
                this.updateEffects();
                this.renderEffects();
            }
            this.animationFrameId = requestAnimationFrame(renderLoop);
        };
        
        this.animationFrameId = requestAnimationFrame(renderLoop);
    }
    
    /**
     * Stoppt die Render-Schleife
     */
    stopRenderLoop() {
        if (this.animationFrameId) {
            cancelAnimationFrame(this.animationFrameId);
            this.animationFrameId = null;
        }
    }
    
    /**
     * Aktualisiert alle visuellen Effekte (wird in jedem Frame ausgeführt)
     */
    updateEffects() {
        const currentTime = Date.now();
        
        // Aktualisiere Fußspuren (entferne alte)
        if (this.config.footstepsEnabled) {
            this.footsteps = this.footsteps.filter(footstep => 
                (currentTime - footstep.creationTime) < this.config.footstepFadeOutTime
            );
        } else {
            this.footsteps = [];
        }
        
        // Aktualisiere Partikel
        if (this.config.particlesEnabled) {
            // Physik für Partikel
            for (const particle of this.particles) {
                // Aktualisiere Position basierend auf Geschwindigkeit
                particle.x += particle.velocityX;
                particle.y += particle.velocityY;
                
                // Aktualisiere Geschwindigkeit basierend auf Gravitation
                particle.velocityY += particle.gravity;
                
                // Aktualisiere Verbleibende Lebensdauer
                particle.life -= 16.7; // Ungefähr 60 FPS (1000ms / 60)
            }
            
            // Entferne tote Partikel
            this.particles = this.particles.filter(particle => particle.life > 0);
            
            // Begrenze maximale Anzahl von Partikeln
            if (this.particles.length > this.config.maxParticles) {
                this.particles = this.particles.slice(-this.config.maxParticles);
            }
        } else {
            this.particles = [];
        }
    }
    
    /**
     * Rendert alle visuellen Effekte auf den Canvas
     */
    renderEffects() {
        // Zeichne Fußspuren
        if (this.config.footstepsEnabled && this.footsteps.length > 0) {
            this.renderFootsteps();
        }
        
        // Zeichne Partikel
        if (this.config.particlesEnabled && this.particles.length > 0) {
            this.renderParticles();
        }
    }
    
    /**
     * Rendert alle Fußspuren
     */
    renderFootsteps() {
        const currentTime = Date.now();
        
        for (const footstep of this.footsteps) {
            const age = currentTime - footstep.creationTime;
            const opacity = 1 - (age / this.config.footstepFadeOutTime);
            
            this.ctx.save();
            
            // Transformiere zur Position der Fußspur
            this.ctx.translate(footstep.x, footstep.y);
            this.ctx.rotate(footstep.angle * Math.PI / 180);
            
            // Stil basierend auf Layer-Typ
            let color;
            switch (footstep.layerType) {
                case 'RunningLayer':
                    color = 'rgba(217, 87, 87, ' + opacity * 0.8 + ')';
                    break;
                case 'AdvancedWalkingLayer':
                    color = 'rgba(87, 157, 217, ' + opacity * 0.8 + ')';
                    break;
                case 'BasicWalkingLayer':
                    color = 'rgba(87, 217, 157, ' + opacity * 0.8 + ')';
                    break;
                default:
                    color = 'rgba(150, 150, 150, ' + opacity * 0.7 + ')';
            }
            
            // Zeichne Fußspur
            this.ctx.fillStyle = color;
            
            // Form basierend auf links/rechts
            if (footstep.isLeftFoot) {
                this.drawLeftFootprint(footstep.width, footstep.length);
            } else {
                this.drawRightFootprint(footstep.width, footstep.length);
            }
            
            this.ctx.restore();
        }
    }
    
    /**
     * Zeichnet einen linken Fußabdruck
     */
    drawLeftFootprint(width, length) {
        const w = width * 15; // Skalierung für bessere Sichtbarkeit
        const l = length * 25;
        
        this.ctx.beginPath();
        this.ctx.ellipse(0, 0, w / 2, l / 2, 0, 0, Math.PI * 2);
        this.ctx.fill();
        
        // Zehen für realistischeren Look
        this.ctx.beginPath();
        this.ctx.ellipse(0, -l / 2 - w / 4, w / 6, w / 4, 0, 0, Math.PI * 2);
        this.ctx.fill();
        
        this.ctx.beginPath();
        this.ctx.ellipse(w / 4, -l / 2 - w / 5, w / 8, w / 5, 0, 0, Math.PI * 2);
        this.ctx.fill();
    }
    
    /**
     * Zeichnet einen rechten Fußabdruck
     */
    drawRightFootprint(width, length) {
        const w = width * 15; // Skalierung für bessere Sichtbarkeit
        const l = length * 25;
        
        this.ctx.beginPath();
        this.ctx.ellipse(0, 0, w / 2, l / 2, 0, 0, Math.PI * 2);
        this.ctx.fill();
        
        // Zehen für realistischeren Look
        this.ctx.beginPath();
        this.ctx.ellipse(0, -l / 2 - w / 4, w / 6, w / 4, 0, 0, Math.PI * 2);
        this.ctx.fill();
        
        this.ctx.beginPath();
        this.ctx.ellipse(-w / 4, -l / 2 - w / 5, w / 8, w / 5, 0, 0, Math.PI * 2);
        this.ctx.fill();
    }
    
    /**
     * Rendert alle Partikel
     */
    renderParticles() {
        for (const particle of this.particles) {
            // Berechne Opazität basierend auf Lebensdauer
            let opacity;
            
            if (particle.type === 'dust') {
                // Staub-Partikel verblassen linear
                opacity = particle.life / this.config.smokeParticleLife;
            } else if (particle.type === 'goal') {
                // Ziel-Partikel haben eine komplexere Opazitätskurve
                const normalizedLife = particle.life / this.config.goalParticleLife;
                // Schnell einblenden, langsam ausblenden
                opacity = normalizedLife < 0.3 ? normalizedLife * 3.33 : normalizedLife;
            } else {
                opacity = particle.life / 1000; // Fallback
            }
            
            // Beschränke Opazität auf gültigen Bereich
            opacity = Math.max(0, Math.min(1, opacity));
            
            this.ctx.save();
            
            // Setze Stil basierend auf Partikeltyp
            if (particle.type === 'dust') {
                this.ctx.fillStyle = `rgba(${particle.color.r}, ${particle.color.g}, ${particle.color.b}, ${opacity})`;
            } else if (particle.type === 'goal') {
                this.ctx.fillStyle = `rgba(255, 215, 0, ${opacity})`;
            } else if (particle.type === 'layer_change') {
                // Layer-Wechsel-Partikel haben einen radialen Farbverlauf
                const gradientRadius = particle.size * 2;
                const gradient = this.ctx.createRadialGradient(
                    particle.x, particle.y, 0,
                    particle.x, particle.y, gradientRadius
                );
                gradient.addColorStop(0, `rgba(${particle.color.r}, ${particle.color.g}, ${particle.color.b}, ${opacity})`);
                gradient.addColorStop(1, `rgba(${particle.color.r}, ${particle.color.g}, ${particle.color.b}, 0)`);
                
                this.ctx.fillStyle = gradient;
            }
            
            // Zeichne Partikel (einfacher Kreis)
            this.ctx.beginPath();
            this.ctx.arc(particle.x, particle.y, particle.size, 0, Math.PI * 2);
            this.ctx.fill();
            
            this.ctx.restore();
        }
    }
    
    /**
     * Erstellt neue Fußspuren für einen Charakter in Bewegung
     */
    createFootstep(x, y, angle, isLeftFoot, layerType) {
        // Validiere Eingabe
        if (!this.config.footstepsEnabled) return;
        
        // Standard-Eigenschaften für verschiedene Layer-Typen
        let width = 0.25;  // Standardbreite
        let length = 0.45;  // Standardlänge
        
        // Anpasse Größe basierend auf Layer-Typ
        switch (layerType) {
            case 'RunningLayer':
                width *= 0.8;
                length *= 1.2;
                break;
            case 'AdvancedWalkingLayer':
                width *= 1.1;
                length *= 1.1;
                break;
            case 'IdleLayer':
                // Keine Fußspuren im Idle-Modus
                return;
            default:
                // Standardwerte für BasicWalkingLayer und andere
                break;
        }
        
        // Erstelle neue Fußspur
        const footstep = {
            x: x,
            y: y,
            angle: angle,
            isLeftFoot: isLeftFoot,
            width: width,
            length: length,
            layerType: layerType,
            creationTime: Date.now()
        };
        
        // Füge zur Liste hinzu und begrenze maximale Anzahl
        this.footsteps.push(footstep);
        
        // Begrenze die Anzahl der Fußspuren
        if (this.footsteps.length > this.config.maxFootsteps) {
            this.footsteps.shift();  // Entferne die älteste Fußspur
        }
    }
    
    /**
     * Erstellt Staubpartikel für die Bewegung
     */
    createDustParticles(x, y, directionAngle, speed, layerType, count = 5) {
        if (!this.config.particlesEnabled) return;
        
        // Basis-Konfiguration für Staubpartikel
        let particleSize = 0.05 + Math.random() * 0.15;  // Zufällige Größe
        let particleSpeed = 0.3 + Math.random() * 0.5;  // Zufällige Geschwindigkeit
        let particleLife = this.config.smokeParticleLife * (0.7 + Math.random() * 0.6);  // Zufällige Lebensdauer
        let color = { r: 150, g: 150, b: 130 };  // Bräunlicher Staub
        let gravity = 0.01 + Math.random() * 0.05;  // Leichte Aufwärtsbewegung
        
        // Anpassung basierend auf Layer-Typ
        switch (layerType) {
            case 'RunningLayer':
                // Mehr, größere und schnellere Partikel beim Laufen
                particleSize *= 1.5;
                particleSpeed *= 2;
                count *= 2;
                color = { r: 200, g: 200, b: 180 };  // Hellerer Staub
                break;
                
            case 'AdvancedWalkingLayer':
                // Angepasste Partikel für fortgeschrittenes Gehen
                particleSize *= 1.2;
                particleSpeed *= 1.3;
                color = { r: 180, g: 180, b: 160 };
                break;
                
            case 'IdleLayer':
                // Fast keine Partikel im Idle-Zustand
                return;
                
            default:
                // Standardwerte
                break;
        }
        
        // Geschwindigkeitsanpassung
        particleSpeed *= Math.min(3, Math.max(0.5, speed));
        
        // Erstelle mehrere Partikel
        for (let i = 0; i < count; i++) {
            // Zufälliger Richtungswinkel im Bereich des Hauptwinkels +/- 30 Grad
            const angle = (directionAngle + 180 + (Math.random() * 60 - 30)) * Math.PI / 180;
            
            // Zufällige Variationen für natürlicheres Aussehen
            const thisSize = particleSize * (0.7 + Math.random() * 0.6);
            const thisSpeed = particleSpeed * (0.8 + Math.random() * 0.4);
            const thisLife = particleLife * (0.8 + Math.random() * 0.4);
            const thisGravity = gravity * (0.7 + Math.random() * 0.6);
            
            // Zufällige Position um die Füße
            const offsetX = (Math.random() * 10) - 5;
            const offsetY = (Math.random() * 10) - 5;
            
            // Geschwindigkeitskomponenten
            const vx = Math.cos(angle) * thisSpeed;
            const vy = Math.sin(angle) * thisSpeed;
            
            // Erstelle Partikel
            const particle = {
                x: x + offsetX,
                y: y + offsetY,
                velocityX: vx,
                velocityY: vy,
                size: thisSize * 5,  // Skalierung für bessere Sichtbarkeit
                color: color,
                gravity: thisGravity,
                life: thisLife,
                type: 'dust'
            };
            
            this.particles.push(particle);
        }
    }
    
    /**
     * Erstellt Partikel-Effekt für das Erreichen eines Ziels
     */
    createGoalReachedEffect(x, y, count = 30) {
        if (!this.config.particlesEnabled) return;
        
        // Goldene Glitzerpartikel
        const color = { r: 255, g: 215, b: 0 };  // Gold
        
        for (let i = 0; i < count; i++) {
            // Zufälliger Richtungswinkel in alle Richtungen
            const angle = Math.random() * 360 * Math.PI / 180;
            
            // Zufällige Variationen
            const size = 0.1 + Math.random() * 0.2;
            const speed = 1 + Math.random() * 3;
            const life = this.config.goalParticleLife * (0.7 + Math.random() * 0.6);
            
            // Geschwindigkeitskomponenten
            const vx = Math.cos(angle) * speed;
            const vy = Math.sin(angle) * speed;
            
            // Negative Gravitation für aufsteigende Partikel
            const gravity = -0.01 - Math.random() * 0.03;
            
            // Erstelle Partikel
            const particle = {
                x: x,
                y: y,
                velocityX: vx,
                velocityY: vy,
                size: size * 8,  // Größere Partikel für bessere Sichtbarkeit
                color: color,
                gravity: gravity,
                life: life,
                type: 'goal'
            };
            
            this.particles.push(particle);
        }
    }
    
    /**
     * Erstellt einen Partikel-Effekt beim Wechsel des Bewegungs-Layers
     */
    createLayerChangeEffect(x, y, newLayerType, count = 20) {
        if (!this.config.particlesEnabled) return;
        
        // Farbauswahl basierend auf Layer-Typ
        let color;
        switch (newLayerType) {
            case 'RunningLayer':
                color = { r: 255, g: 77, b: 77 };  // Rot
                break;
            case 'AdvancedWalkingLayer':
                color = { r: 77, g: 148, b: 255 };  // Blau
                break;
            case 'IdleLayer':
                color = { r: 77, g: 255, b: 148 };  // Grün
                break;
            default:
                color = { r: 180, g: 180, b: 180 };  // Grau
        }
        
        for (let i = 0; i < count; i++) {
            // Zufälliger Richtungswinkel in Halbkreis nach oben
            const angle = (270 + (Math.random() * 180 - 90)) * Math.PI / 180;
            
            // Zufällige Variationen
            const size = 0.1 + Math.random() * 0.25;
            const speed = 0.5 + Math.random() * 2;
            const life = 750 * (0.6 + Math.random() * 0.8);  // Kürzere Lebensdauer
            
            // Geschwindigkeitskomponenten
            const vx = Math.cos(angle) * speed;
            const vy = Math.sin(angle) * speed;
            
            // Leichte Aufwärtsbewegung
            const gravity = -0.01;
            
            // Erstelle Partikel
            const particle = {
                x: x,
                y: y,
                velocityX: vx,
                velocityY: vy,
                size: size * 10,  // Größere Partikel für bessere Sichtbarkeit
                color: color,
                gravity: gravity,
                life: life,
                type: 'layer_change'
            };
            
            this.particles.push(particle);
        }
    }
    
    /**
     * Aktiviert oder deaktiviert die visuellen Effekte
     */
    setEnabled(enabled) {
        this.config.enabled = enabled;
        if (!enabled) {
            // Bereinige alle Effekte
            this.footsteps = [];
            this.particles = [];
        }
    }
    
    /**
     * Aktiviert oder deaktiviert Fußspuren
     */
    setFootstepsEnabled(enabled) {
        this.config.footstepsEnabled = enabled;
        if (!enabled) {
            this.footsteps = [];
        }
    }
    
    /**
     * Aktiviert oder deaktiviert Partikeleffekte
     */
    setParticlesEnabled(enabled) {
        this.config.particlesEnabled = enabled;
        if (!enabled) {
            this.particles = [];
        }
    }
}
