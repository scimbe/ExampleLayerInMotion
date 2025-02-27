/**
 * game.js
 * Hauptdatei mit dem Spielstatus und gemeinsamen Funktionen
 */

// ---- Konfiguration ----
export const API_BASE_URL = "/api/v1";
export const GRID_SIZE = 20; // Größe der Gitterzellen in Pixeln
export const DEFAULT_SPEED = 1.0;
export const MAGNET_FORCE = 0.05; // Stärke des Magneteffekts (0-1)
export const MAX_SPEED = 3.0; // Maximale Geschwindigkeit

// ---- Spielzustand ----
export const gameState = {
    character: {
        id: null,
        x: 0,
        y: 0,
        z: 0,
        speed: 0,
        rotationX: 0,
        rotationY: 0,
        rotationZ: 0
    },
    pointer: {
        active: false,
        x: 0,
        y: 0
    },
    activeLayer: "BasicWalkingLayer",
    activeGait: "NORMAL",
    score: 0,
    goalPosition: { x: 0, y: 0 },
    webSocket: null,
    gameLoopId: null,
    isMoving: false,
    currentAnimation: null,
    goals: [],
    offlineMode: false,
    firstInteraction: true  // Tracking für erste Benutzerinteraktion
};

// ---- DOM-Elemente ----
export let canvas;
export let ctx;
export let posXElement;
export let posYElement;
export let posZElement;
export let speedElement;
export let activeLayerElement;
export let scoreElement;
export let messageElement;
export let goalsElement;
export let connectionStatusElement;

// ---- Hilfsfunktionen ----

// Zeigt eine Nachricht an
export function showMessage(message, duration = 3000) {
    if (!messageElement) return;
    
    messageElement.textContent = message;
    messageElement.style.opacity = 1;
    
    setTimeout(() => {
        messageElement.style.opacity = 0;
    }, duration);
}

// Aktualisiert die Statusanzeige
export function updateStatusDisplay() {
    if (!posXElement || !posYElement || !posZElement || !speedElement) return;
    
    posXElement.textContent = gameState.character.x.toFixed(2);
    posYElement.textContent = gameState.character.y.toFixed(2);
    posZElement.textContent = gameState.character.z.toFixed(2);
    speedElement.textContent = gameState.character.speed.toFixed(2);
    
    if (activeLayerElement) {
        activeLayerElement.textContent = gameState.activeLayer;
    }
}

// Aktualisiert den Charakter-Status
export function updateCharacterState(data) {
    gameState.character.x = data.x;
    gameState.character.y = data.y;
    gameState.character.z = data.z;
    gameState.character.speed = data.speed;
    gameState.character.rotationX = data.rotationX;
    gameState.character.rotationY = data.rotationY;
    gameState.character.rotationZ = data.rotationZ;
    
    updateStatusDisplay();
}

// Aktualisiert die Layer-Buttons
export function updateLayerButtons(activeLayer) {
    const layerButtons = document.querySelectorAll(".layer-button");
    
    layerButtons.forEach(button => {
        button.classList.remove("active");
        
        const buttonId = button.id;
        if ((buttonId === "walkingLayer" && activeLayer === "BasicWalkingLayer") ||
            (buttonId === "runningLayer" && activeLayer === "RunningLayer") ||
            (buttonId === "advancedWalkingLayer" && activeLayer === "AdvancedWalkingLayer") ||
            (buttonId === "idleLayer" && activeLayer === "IdleLayer")) {
            button.classList.add("active");
        }
    });
    
    // Gangarten-Bereich nur anzeigen, wenn AdvancedWalkingLayer aktiv ist
    const gaitControls = document.getElementById("gaitControls");
    if (gaitControls) {
        gaitControls.style.display = activeLayer === "AdvancedWalkingLayer" ? "block" : "none";
    }
    
    // Aktualisiere auch die Movement-Type-Anzeige
    updateMovementTypeIndicator(activeLayer);
}

// Aktualisiert die Gait-Buttons (Gangarten)
export function updateGaitButtons(gaitType) {
    const gaitButtons = document.querySelectorAll(".gait-button");
    
    gaitButtons.forEach(button => {
        button.classList.remove("active");
        
        const buttonId = button.id;
        if ((buttonId === "normalGait" && gaitType === "NORMAL") ||
            (buttonId === "sneakingGait" && gaitType === "SNEAKING") ||
            (buttonId === "limpingGait" && gaitType === "LIMPING")) {
            button.classList.add("active");
        }
    });
    
    gameState.activeGait = gaitType;
}

// Aktualisiert die Bewegungstyp-Anzeige im Spielfeld
export function updateMovementTypeIndicator(layerType) {
    // Finde oder erstelle die Anzeige
    let indicator = document.querySelector(".movement-type-indicator");
    if (!indicator) {
        indicator = document.createElement("div");
        indicator.className = "movement-type-indicator";
        const gameBoard = document.querySelector(".game-board");
        if (gameBoard) {
            gameBoard.appendChild(indicator);
        }
    }
    
    // Aktualisiere den Text basierend auf dem Layer-Typ
    let displayText = "";
    switch(layerType) {
        case "BasicWalkingLayer":
            displayText = "Gehen";
            break;
        case "RunningLayer":
            displayText = "Laufen";
            break;
        case "IdleLayer":
            displayText = "Stehen";
            break;
        case "AdvancedWalkingLayer":
            displayText = `Erw. Gehen (${gameState.activeGait})`;
            break;
        default:
            displayText = layerType.replace("Layer", "");
    }
    
    indicator.textContent = displayText;
    
    // Zeige kurz an, dann ausblenden
    indicator.classList.add("visible");
    setTimeout(() => {
        indicator.classList.remove("visible");
    }, 2000);
}

// Prüft auf Kollision mit dem Ziel
export function checkGoalCollision() {
    if (!gameState.goalPosition) return;
    
    const dx = gameState.character.x - gameState.goalPosition.x;
    const dy = gameState.character.z - gameState.goalPosition.y;
    const distance = Math.sqrt(dx * dx + dy * dy);
    
    if (distance < 30) { // Kollisionsradius
        // Ziel erreicht
        gameState.score += 10;
        if (scoreElement) {
            scoreElement.textContent = gameState.score;
        }
        
        // Visuelle Effekte für das Erreichen des Ziels
        createScorePopup(gameState.goalPosition.x, gameState.goalPosition.y);
        
        // Nächstes Ziel generieren
        generateNewGoal();
        
        showMessage("Ziel erreicht! +10 Punkte");
        
        // Visuelle Effekte für den Erfolg
        const visualEffects = window.visualEffects;
        if (visualEffects) {
            visualEffects.createGoalReachedEffect(gameState.character.x, gameState.character.z, 30);
        }
    }
}

// Magneteffekt implementieren - zieht den Charakter zum Zielpunkt
export function updateMagneticMovement() {
    if (!gameState.pointer.active) {
        // Wenn kein aktiver Pointer, Bewegung verlangsamen
        gameState.character.speed *= 0.95;
        if (gameState.character.speed < 0.01) {
            gameState.character.speed = 0;
            gameState.isMoving = false;
        }
        return;
    }
    
    // Berechne Distanz und Richtung zum Zielpunkt
    const dx = gameState.pointer.x - gameState.character.x;
    const dy = gameState.pointer.y - gameState.character.z;
    const distance = Math.sqrt(dx * dx + dy * dy);
    
    // Wenn nahe am Ziel, Bewegung stoppen
    if (distance < 5) {
        gameState.pointer.active = false;
        gameState.character.speed *= 0.9;
        return;
    }
    
    // Magnetische Anziehung - je näher, desto schwächer
    const force = Math.min(MAGNET_FORCE * (1 + distance / 100), 0.2);
    
    // Normalisiere Richtungsvektor
    const dirX = dx / distance;
    const dirY = dy / distance;
    
    // Berechne neue Geschwindigkeit basierend auf Layer
    let targetSpeed = DEFAULT_SPEED;
    if (gameState.activeLayer === "RunningLayer") {
        targetSpeed = DEFAULT_SPEED * 2.0;
    } else if (gameState.activeLayer === "IdleLayer") {
        targetSpeed = 0;
        gameState.pointer.active = false;
        return;
    } else if (gameState.activeLayer === "AdvancedWalkingLayer") {
        switch (gameState.activeGait) {
            case "SNEAKING":
                targetSpeed = DEFAULT_SPEED * 0.5;
                break;
            case "LIMPING":
                targetSpeed = DEFAULT_SPEED * 0.7;
                break;
            default:
                targetSpeed = DEFAULT_SPEED * 1.1;
        }
    }
    
    // Geschwindigkeit anpassen - natürlichere Beschleunigung
    gameState.character.speed = gameState.character.speed * 0.95 + targetSpeed * 0.05;
    if (gameState.character.speed > MAX_SPEED) gameState.character.speed = MAX_SPEED;
    
    // Bewegung anwenden
    const moveSpeed = gameState.character.speed * gameState.pointer.active;
    gameState.character.x += dirX * moveSpeed * force * 10;
    gameState.character.z += dirY * moveSpeed * force * 10;
    
    // Bereichsgrenzen einhalten
    if (canvas) {
        const paddedWidth = canvas.width - 20;
        const paddedHeight = canvas.height - 20;
        gameState.character.x = Math.max(20, Math.min(gameState.character.x, paddedWidth));
        gameState.character.z = Math.max(20, Math.min(gameState.character.z, paddedHeight));
    }
    
    // Rotation an Bewegungsrichtung anpassen
    gameState.character.rotationY = Math.atan2(dirX, dirY) * (180 / Math.PI);
    
    // Vertikale Bobbing-Bewegung für natürlicheres Aussehen
    if (gameState.activeLayer === "BasicWalkingLayer" && moveSpeed > 0) {
        const time = Date.now() / 500;
        gameState.character.y = Math.sin(time) * 0.05;
    }
    
    // Status aktualisieren
    gameState.isMoving = moveSpeed > 0;
    updateStatusDisplay();
    checkGoalCollision();
    
    // Magnetfeld-Linien anzeigen
    drawMagneticField();
}

// Zeichnet das Magnetfeld zwischen Ziel und Charakter
function drawMagneticField() {
    if (!ctx || !gameState.pointer.active) return;
    
    const charX = gameState.character.x;
    const charY = gameState.character.z;
    const targetX = gameState.pointer.x;
    const targetY = gameState.pointer.y;
    
    // Zeichne Magnetfeld-Linien
    ctx.save();
    ctx.strokeStyle = 'rgba(65, 105, 225, 0.4)';
    ctx.lineWidth = 2;
    
    // Distanz berechnen
    const dx = targetX - charX;
    const dy = targetY - charY;
    const distance = Math.sqrt(dx * dx + dy * dy);
    
    // Anzahl der Linien basierend auf Distanz
    const lineCount = Math.min(Math.max(3, Math.floor(distance / 30)), 8);
    
    // Zeichne gekrümmte Magnetfeld-Linien
    for (let i = 0; i < lineCount; i++) {
        const offset = (i - lineCount / 2) * 10;
        
        // Normalisierte Richtungsvektoren
        const dirX = dx / distance;
        const dirY = dy / distance;
        
        // Senkrechte Vektoren für Verschiebung
        const perpX = -dirY;
        const perpY = dirX;
        
        // Kurvenpunkte
        const startX = charX + perpX * offset;
        const startY = charY + perpY * offset;
        const endX = targetX + perpX * offset;
        const endY = targetY + perpY * offset;
        
        // Kontrollpunkte für die Kurve
        const cp1x = charX + dx * 0.3 + perpX * offset * 1.5;
        const cp1y = charY + dy * 0.3 + perpY * offset * 1.5;
        const cp2x = charX + dx * 0.7 + perpX * offset * 1.5;
        const cp2y = charY + dy * 0.7 + perpY * offset * 1.5;
        
        // Zeichne die Kurve
        ctx.beginPath();
        ctx.moveTo(startX, startY);
        ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, endX, endY);
        ctx.stroke();
    }
    
    ctx.restore();
}

// Erzeugt eine Punkte-Popup-Animation
function createScorePopup(x, y) {
    const popup = document.createElement("div");
    popup.className = "score-popup";
    popup.textContent = "+10";
    popup.style.left = x + "px";
    popup.style.top = y + "px";
    
    const gameBoard = document.querySelector(".game-board");
    if (gameBoard) {
        gameBoard.appendChild(popup);
        
        // Entferne das Element nach der Animation
        setTimeout(() => {
            popup.remove();
        }, 1000);
    }
}

// Generiert ein neues zufälliges Ziel
export function generateNewGoal() {
    const padding = 50; // Abstand vom Rand
    
    if (!canvas) return;
    
    gameState.goalPosition = {
        x: Math.random() * (canvas.width - 2 * padding) + padding,
        y: Math.random() * (canvas.height - 2 * padding) + padding
    };
    
    // Zeige Zielinformationen an
    updateGoalsDisplay();
}

// Aktualisiert die Zielanzeige
export function updateGoalsDisplay() {
    if (!goalsElement || !gameState.goalPosition) return;
    
    goalsElement.innerHTML = `
        <div>
            <strong>Aktuelles Ziel</strong>
            <p>X: ${gameState.goalPosition.x.toFixed(0)}, Y: ${gameState.goalPosition.y.toFixed(0)}</p>
        </div>
    `;
}

// Aktualisiert die Verbindungsanzeige
export function updateConnectionStatus() {
    if (!connectionStatusElement) return;
    
    if (gameState.offlineMode) {
        connectionStatusElement.textContent = "Offline";
        connectionStatusElement.className = "connection-status status-offline";
    } else {
        connectionStatusElement.textContent = "Online";
        connectionStatusElement.className = "connection-status status-online";
    }
}

// Fügt die Touch/Maus-Event-Listener zum Canvas hinzu
export function setupCanvasControls() {
    if (!canvas) return;
    
    // Maus-Bewegung
    canvas.addEventListener('mousedown', (e) => {
        const rect = canvas.getBoundingClientRect();
        gameState.pointer.x = e.clientX - rect.left;
        gameState.pointer.y = e.clientY - rect.top;
        gameState.pointer.active = true;
        
        // Magneteffekt-Animation starten
        showMagneticPulse(gameState.pointer.x, gameState.pointer.y);
    });
    
    canvas.addEventListener('mousemove', (e) => {
        if (!gameState.pointer.active) return;
        
        const rect = canvas.getBoundingClientRect();
        gameState.pointer.x = e.clientX - rect.left;
        gameState.pointer.y = e.clientY - rect.top;
    });
    
    canvas.addEventListener('mouseup', () => {
        // Magnetwirkung langsam ausklingen lassen
        // Die eigentliche Bewegung wird über updateMagneticMovement() verlangsamt
    });
    
    canvas.addEventListener('mouseleave', () => {
        gameState.pointer.active = false;
    });
    
    // Touch-Bewegung
    canvas.addEventListener('touchstart', (e) => {
        e.preventDefault();
        const rect = canvas.getBoundingClientRect();
        gameState.pointer.x = e.touches[0].clientX - rect.left;
        gameState.pointer.y = e.touches[0].clientY - rect.top;
        gameState.pointer.active = true;
        
        // Magneteffekt-Animation starten
        showMagneticPulse(gameState.pointer.x, gameState.pointer.y);
    });
    
    canvas.addEventListener('touchmove', (e) => {
        e.preventDefault();
        if (!gameState.pointer.active) return;
        
        const rect = canvas.getBoundingClientRect();
        gameState.pointer.x = e.touches[0].clientX - rect.left;
        gameState.pointer.y = e.touches[0].clientY - rect.top;
    });
    
    canvas.addEventListener('touchend', () => {
        // Magnetwirkung ausklingen lassen
    });
}

// Zeigt eine visuelle Pulsanimation für den Magneten an
function showMagneticPulse(x, y) {
    const pulse = document.createElement("div");
    pulse.className = "magnetic-pulse";
    pulse.style.left = x + "px";
    pulse.style.top = y + "px";
    
    const gameBoard = document.querySelector(".game-board");
    if (gameBoard) {
        gameBoard.appendChild(pulse);
        
        // Nach der Animation entfernen
        setTimeout(() => {
            pulse.remove();
        }, 1000);
    }
}

// Zeigt eine Einführung für neue Benutzer an
export function showIntroductionOverlay() {
    // Erstelle Overlay-Element
    const overlay = document.createElement("div");
    overlay.className = "intro-overlay";
    
    overlay.innerHTML = `
        <div class="intro-content">
            <h2>Willkommen bei Motion Master!</h2>
            <p>Erkunde die verschiedenen Bewegungsarten in diesem interaktiven Spiel.</p>
            
            <ul>
                <li><strong>Ziel des Spiels:</strong> Bewege den Charakter zu den goldenen Zielen.</li>
                <li><strong>Steuerung:</strong> Der Charakter wird wie ein Magnet zu deiner Berührung gezogen.</li>
                <li><strong>Bewegungsarten:</strong> Wechsle zwischen Gehen, Laufen und anderen Bewegungstypen.</li>
                <li><strong>Gangarten:</strong> Im erweiterten Geh-Modus kannst du verschiedene Gangarten ausprobieren.</li>
            </ul>
            
            <p>Experimentiere mit den verschiedenen Bewegungsarten und beobachte die Unterschiede!</p>
            
            <div class="intro-buttons">
                <button class="intro-button" id="start-game-btn">Spiel starten</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(overlay);
    
    // Event-Listener für den Start-Button
    document.getElementById("start-game-btn").addEventListener("click", () => {
        overlay.remove();
        
        // Zeige Canvas-Steuerungshinweis
        showCanvasHint();
    });
}

// Zeigt einen Hinweis zur Canvas-Steuerung an
export function showCanvasHint() {
    const hint = document.createElement("div");
    hint.className = "canvas-hint";
    hint.innerHTML = `
        <p>Tippe auf dem Spielfeld, um den Magneten zu steuern</p>
        <span class="magnet-icon">🧲</span>
    `;
    
    const gameBoard = document.querySelector(".game-board");
    if (gameBoard) {
        gameBoard.appendChild(hint);
        
        // Nach einem kurzen Moment einblenden
        setTimeout(() => {
            hint.classList.add("visible");
        }, 100);
        
        // Nach einiger Zeit wieder ausblenden
        setTimeout(() => {
            hint.classList.remove("visible");
            setTimeout(() => hint.remove(), 1000);
        }, 5000);
    }
}

// Wird aufgerufen, wenn sich das Dokument geladen hat
document.addEventListener('DOMContentLoaded', () => {
    // DOM-Elemente initialisieren
    canvas = document.getElementById("gameCanvas");
    ctx = canvas? canvas.getContext("2d") : null;
    posXElement = document.getElementById("posX");
    posYElement = document.getElementById("posY");
    posZElement = document.getElementById("posZ");
    speedElement = document.getElementById("speed");
    activeLayerElement = document.getElementById("activeLayer");
    scoreElement = document.getElementById("scoreValue");
    messageElement = document.getElementById("messages");
    goalsElement = document.getElementById("goals");
    connectionStatusElement = document.getElementById("connectionStatus");
    
    // Tab-Steuerung einrichten
    setupTabs();
    
    // Import weiterer Module und Initialisierung des Spiels
    import('./gameInit.js')
        .then(module => {
            module.initializeGame();
            
            // Einführung anzeigen, wenn es der erste Besuch ist
            if (!localStorage.getItem('motionMasterIntroShown')) {
                localStorage.setItem('motionMasterIntroShown', 'true');
                showIntroductionOverlay();
            }
        })
        .catch(error => {
            console.error("Fehler beim Laden der Spielinitialisierung:", error);
            
            // Notfall-Nachricht anzeigen
            showMessage("Fehler beim Laden des Spiels! Bitte Seite neu laden.", 10000);
        });
});

// Tab-Steuerung einrichten
function setupTabs() {
    const tabs = document.querySelectorAll('.control-tab');
    const tabContents = document.querySelectorAll('.tab-content');
    
    // Wenn keine Tabs vorhanden sind, beende die Funktion
    if (tabs.length === 0) return;
    
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            // Aktiven Tab setzen
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            
            // Aktiven Tab-Inhalt anzeigen
            const targetId = tab.getAttribute('data-target');
            tabContents.forEach(content => {
                content.classList.remove('active');
                if (content.id === targetId) {
                    content.classList.add('active');
                }
            });
        });
    });
    
    // Initial den ersten Tab aktivieren
    if (tabs.length > 0 && tabContents.length > 0) {
        tabs[0].classList.add('active');
        tabContents[0].classList.add('active');
    }
}
