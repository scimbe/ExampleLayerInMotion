/**
 * magnetInteraction.js
 * Spezielle Interaktionseffekte für die magnetische Steuerung
 */

import { gameState } from './game.js';

let magneticFieldLines = [];
const MAX_FIELD_LINES = 25;

/**
 * Initialisiert die magnetischen Effekte
 */
export function initMagneticEffects(canvas) {
    if (!canvas) return;
    
    const container = canvas.parentElement;
    
    // Erstelle Container für Magneteffekte
    const effectsContainer = document.createElement('div');
    effectsContainer.className = 'magnetic-effects-container';
    container.appendChild(effectsContainer);
    
    // Erstelle und initialisiere Magnetlinien
    for (let i = 0; i < MAX_FIELD_LINES; i++) {
        const fieldLine = document.createElement('div');
        fieldLine.className = 'magnetic-field-line';
        fieldLine.style.opacity = '0';
        effectsContainer.appendChild(fieldLine);
        magneticFieldLines.push(fieldLine);
    }
    
    return {
        updateMagneticField,
        showPulseEffect
    };
}

/**
 * Aktualisiert die Anzeige des magnetischen Felds zwischen Magnet und Ziel
 */
export function updateMagneticField(sourceX, sourceY, targetX, targetY, intensity = 1) {
    if (magneticFieldLines.length === 0) return;
    
    // Berechne Distanz und Richtung
    const dx = targetX - sourceX;
    const dy = targetY - sourceY;
    const distance = Math.sqrt(dx * dx + dy * dy);
    
    // Normalisierte Richtungsvektoren
    const dirX = dx / distance;
    const dirY = dy / distance;
    
    // Senkrechte Vektoren für Verschiebung
    const perpX = -dirY;
    const perpY = dirX;
    
    // Anzahl der aktiven Linien basierend auf Distanz und Intensität
    const activeLineCount = Math.min(
        Math.max(3, Math.ceil(distance / 30) + Math.ceil(intensity * 5)), 
        MAX_FIELD_LINES
    );
    
    // Winkel zwischen Quelle und Ziel in Grad
    const angle = Math.atan2(dy, dx) * 180 / Math.PI;
    
    // Für magnetische Anziehung ändern wir Farben basierend auf dem aktiven Layer
    let color = '#3498db'; // Standard-Blau
    switch (gameState.activeLayer) {
        case 'RunningLayer':
            color = '#e74c3c'; // Rot
            break;
        case 'IdleLayer':
            color = '#2ecc71'; // Grün
            break;
        case 'AdvancedWalkingLayer':
            color = '#9b59b6'; // Lila
            break;
    }
    
    // Aktualisiere die Linien
    for (let i = 0; i < magneticFieldLines.length; i++) {
        const line = magneticFieldLines[i];
        
        if (i < activeLineCount) {
            // Berechne Versatz für diese Linie
            const offset = ((i - (activeLineCount-1)/2) * 8); 
            
            // Die tatsächliche Länge und Position basierend auf der Bewegungsrichtung
            const horizontalOffset = perpX * offset;
            const verticalOffset = perpY * offset;
            
            // Jetzt aktualisieren wir die Linie mit CSS-Transformationen
            line.style.left = sourceX + 'px';
            line.style.top = sourceY + 'px';
            line.style.width = distance + 'px';
            line.style.height = '2px';
            line.style.transform = `rotate(${angle}deg) translateX(${offset}px)`;
            line.style.backgroundColor = color;
            
            // Intensität und Pulsieren
            const opacityBase = 0.7 - Math.abs(offset) / (activeLineCount * 4);
            const opacityPulse = Math.sin(Date.now() / 1000 + i) * 0.15 + 0.85;
            line.style.opacity = Math.max(0.1, opacityBase * opacityPulse * intensity);
        } else {
            // Verstecke überschüssige Linien
            line.style.opacity = '0';
        }
    }
}

/**
 * Zeigt einen Pulseffekt an der angegebenen Position an
 */
export function showPulseEffect(x, y) {
    const container = document.querySelector('.game-board');
    if (!container) return;
    
    const pulse = document.createElement('div');
    pulse.className = 'magnetic-pulse';
    pulse.style.left = x + 'px';
    pulse.style.top = y + 'px';
    
    // Wähle die Farbe basierend auf dem aktiven Layer
    let color = '#3498db'; // Standard-Blau
    switch (gameState.activeLayer) {
        case 'RunningLayer':
            color = '#e74c3c'; // Rot
            break;
        case 'IdleLayer':
            color = '#2ecc71'; // Grün
            break;
        case 'AdvancedWalkingLayer':
            color = '#9b59b6'; // Lila
            break;
    }
    
    // Hintergrundfarbe anpassen
    pulse.style.background = `radial-gradient(circle, ${color}99 0%, ${color}00 70%)`;
    
    container.appendChild(pulse);
    
    // Element nach Animation entfernen
    setTimeout(() => {
        pulse.remove();
    }, 1000);
}
