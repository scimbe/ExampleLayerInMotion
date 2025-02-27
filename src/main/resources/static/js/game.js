/**
 * Motion Master Game
 * Ein Spiel zur Demonstration der Layer-basierten Charakterbewegung
 */

import { moveCharacter, stopCharacter, setActiveLayer } from './characterMovement.js';
import { connectWebSocket } from './webSocketHandler.js';
import { drawCharacter, drawGrid, drawGoal } from './gameRendering.js';

// ---- Konfiguration ----
const API_BASE_URL = "/api/v1";
const WS_URL = "ws://" + window.location.host + "/motion-updates";
const CANVAS_UPDATE_RATE = 60; // FPS
const GRID_SIZE = 20; // Größe der Gitterzellen in Pixeln
const DEFAULT_SPEED = 1.0;

// ---- Spielzustand ----
const gameState = {
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
    activeLayer: "BasicWalkingLayer",
    score: 0,
    goalPosition: { x: 0, y: 0 },
    webSocket: null,
    gameLoopId: null,
    isMoving: false,
    currentAnimation: null,
    goals: [],
    offlineMode: false
};

// ---- DOM-Elemente ----
const canvas = document.getElementById("gameCanvas");
const ctx = canvas.getContext("2d");
const posXElement = document.getElementById("posX");
const posYElement = document.getElementById("posY");
const posZElement = document.getElementById("posZ");
const speedElement = document.getElementById("speed");
const activeLayerElement = document.getElementById("activeLayer");
const scoreElement = document.getElementById("scoreValue");
const messageElement = document.getElementById("messages");
const goalsElement = document.getElementById("goals");

// ---- Canvas-Setup ----
function setupCanvas() {
    // Passe Canvas-Größe an Container an
    const container = document.querySelector(".game-board");
    canvas.width = container.clientWidth;
    canvas.height = container.clientHeight;
    
    // Initialen Charakter zeichnen
    drawCharacter();
}

// ---- Netzwerkkommunikation ----

// Erstellt einen neuen Charakter
async function createCharacter() {
    try {
        try {
            const response = await fetch(`${API_BASE_URL}/characters`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    x: canvas.width / 2,
                    y: 0,
                    z: canvas.height / 2
                })
            });
            
            if (response.ok) {
                const data = await response.json();
                gameState.character.id = data.characterId;
                updateCharacterState(data);
                gameState.offlineMode = false;
                console.log(`Character created with ID: ${data.characterId} at position (${data.x}, ${data.y}, ${data.z})`);
                return data;
            }
        } catch (e) {
            console.warn("API nicht verfügbar, verwende Mock-Daten", e);
            gameState.offlineMode = true;
        }
        
        // Fallback zu Mock-Daten
        if (gameState.offlineMode) {
            const mockCharacter = {
                characterId: "mock-" + Math.random().toString(36).substring(2, 9),
                x: canvas.width / 2,
                y: 0,
                z: canvas.height / 2,
                speed: 0,
                rotationX: 0,
                rotationY: 0,
                rotationZ: 0
            };
            gameState.character.id = mockCharacter.characterId;
            updateCharacterState(mockCharacter);
            showMessage("Offline-Modus aktiviert");
            return mockCharacter;
        }
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Erstellen des Charakters:", error);
    }
}

// Spielt eine Animation ab
async function playAnimation(animationId, speed = 1.0) {
    if (!gameState.character.id) return;
    
    try {
        gameState.currentAnimation = animationId;
        
        // Online-Modus: API aufrufen
        if (!gameState.offlineMode) {
            try {
                const response = await fetch(`${API_BASE_URL}/characters/${gameState.character.id}/animate`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        animationId: animationId,
                        speed: speed
                    })
                });
                
                if (response.ok) {
                    const data = await response.json();
                    updateCharacterState(data);
                    return data;
                }
            } catch (e) {
                console.warn("API nicht erreichbar, wechsle zu Offline-Modus", e);
                gameState.offlineMode = true;
            }
        }
        
        // Offline-Modus: Animation simulieren
        if (gameState.offlineMode) {
            showMessage(`Animation: ${animationId}`);
        }
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Abspielen der Animation:", error);
    }
}

// Entfernt einen Layer
async function removeLayer(layerClassName) {
    try {
        // Online-Modus: API aufrufen
        if (!gameState.offlineMode) {
            try {
                const response = await fetch(`${API_BASE_URL}/layers/${encodeURIComponent('com.example.motion.sys.behavior.' + layerClassName)}`, {
                    method: "DELETE"
                });
                
                if (response.ok) {
                    return true;
                }
            } catch (e) {
                console.warn("API nicht erreichbar, wechsle zu Offline-Modus", e);
                gameState.offlineMode = true;
            }
        }
        
        // Offline-Modus: Keine spezielle Behandlung nötig
        return true;
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Entfernen des Layers:", error);
    }
}

// ---- Spiellogik ----

// Initialisiert das Spiel
async function initGame() {
    setupCanvas();
    
    try {
        connectWebSocket();
    } catch (e) {
        console.warn("WebSocket konnte nicht initialisiert werden:", e);
        gameState.offlineMode = true;
    }
    
    try {
        await createCharacter();
        await setActiveLayer("BasicWalkingLayer");
        generateNewGoal();
        startGameLoop();
        showMessage("Spiel gestartet! Erreiche die Ziele.");
    } catch (error) {
        showMessage("Fehler beim Starten des Spiels: " + error.message);
        console.error("Fehler beim Starten des Spiels:", error);
        
        // Notfall-Fallback: Spiel trotzdem starten
        gameState.offlineMode = true;
        gameState.character.id = "fallback-" + Math.random().toString(36).substring(2, 9);
        gameState.character.x = canvas.width / 2;
        gameState.character.z = canvas.height / 2;
        generateNewGoal();
        startGameLoop();
    }
}

// Startet die Spielschleife
function startGameLoop() {
    if (gameState.gameLoopId) {
        cancelAnimationFrame(gameState.gameLoopId);
    }
    
    function gameLoop() {
        // Canvas leeren
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        // Spielfeld zeichnen
        drawGrid();
        
        // Ziel zeichnen
        drawGoal();
        
        // Charakter zeichnen
        drawCharacter();
        
        // Offline-Modus-Anzeige
        if (gameState.offlineMode) {
            ctx.save();
            ctx.font = "12px Arial";
            ctx.fillStyle = "#e74c3c";
            ctx.textAlign = "left";
            ctx.fillText("OFFLINE MODUS", 10, 20);
            ctx.restore();
        }
        
        // Weiter animieren
        gameState.gameLoopId = requestAnimationFrame(gameLoop);
    }
    
    gameState.gameLoopId = requestAnimationFrame(gameLoop);
}

// Generiert ein neues zufälliges Ziel
function generateNewGoal() {
    const padding = 50; // Abstand vom Rand
    
    gameState.goalPosition = {
        x: Math.random() * (canvas.width - 2 * padding) + padding,
        y: Math.random() * (canvas.height - 2 * padding) + padding
    };
    
    // Zeige Zielinformationen an
    updateGoalsDisplay();
}

// Prüft auf Kollision mit dem Ziel
function checkGoalCollision() {
    const dx = gameState.character.x - gameState.goalPosition.x;
    const dy = gameState.character.z - gameState.goalPosition.y;
    const distance = Math.sqrt(dx * dx + dy * dy);
    
    if (distance < 30) { // Kollisionsradius
        // Ziel erreicht
        gameState.score += 10;
        scoreElement.textContent = gameState.score;
        
        // Nächstes Ziel generieren
        generateNewGoal();
        
        showMessage("Ziel erreicht! +10 Punkte");
    }
}

// ---- UI-Updates ----

// Aktualisiert den Charakter-Status
function updateCharacterState(data) {
    gameState.character.x = data.x;
    gameState.character.y = data.y;
    gameState.character.z = data.z;
    gameState.character.speed = data.speed;
    gameState.character.rotationX = data.rotationX;
    gameState.character.rotationY = data.rotationY;
    gameState.character.rotationZ = data.rotationZ;
    
    updateStatusDisplay();
}

// Aktualisiert die Statusanzeige
function updateStatusDisplay() {
    posXElement.textContent = gameState.character.x.toFixed(2);
    posYElement.textContent = gameState.character.y.toFixed(2);
    posZElement.textContent = gameState.character.z.toFixed(2);
    speedElement.textContent = gameState.character.speed.toFixed(2);
    activeLayerElement.textContent = gameState.activeLayer;
}

// Aktualisiert die Layer-Buttons
function updateLayerButtons(activeLayer) {
    const layerButtons = document.querySelectorAll(".layer-button");
    
    layerButtons.forEach(button => {
        button.classList.remove("active");
        
        const buttonId = button.id;
        if ((buttonId === "walkingLayer" && activeLayer === "BasicWalkingLayer") ||
            (buttonId === "runningLayer" && activeLayer === "RunningLayer") ||
            (buttonId === "idleLayer" && activeLayer === "IdleLayer")) {
            button.classList.add("active");
        }
    });
}

// Zeigt eine Nachricht an
function showMessage(message, duration = 3000) {
    messageElement.textContent = message;
    messageElement.style.opacity = 1;
    
    setTimeout(() => {
        messageElement.style.opacity = 0;
    }, duration);
}

// Aktualisiert die Zielanzeige
function updateGoalsDisplay() {
    goalsElement.innerHTML = `
        <div>
            <strong>Aktuelles Ziel:</strong>
            <p>X: ${gameState.goalPosition.x.toFixed(2)}, Y: ${gameState.goalPosition.y.toFixed(2)}</p>
        </div>
    `;
}

// ---- Event-Listener ----

// Bewegungsbuttons
document.getElementById("moveUp").addEventListener("click", () => moveCharacter(0, 0, -1));
document.getElementById("moveDown").addEventListener("click", () => moveCharacter(0, 0, 1));
document.getElementById("moveLeft").addEventListener("click", () => moveCharacter(-1, 0, 0));
document.getElementById("moveRight").addEventListener("click", () => moveCharacter(1, 0, 0));

// Layer-Buttons
document.getElementById("walkingLayer").addEventListener("click", async () => {
    // Andere Layer entfernen
    await removeLayer("RunningLayer");
    await removeLayer("IdleLayer");
    await setActiveLayer("BasicWalkingLayer");
});

document.getElementById("runningLayer").addEventListener("click", async () => {
    // Andere Layer entfernen
    await removeLayer("BasicWalkingLayer");
    await removeLayer("IdleLayer");
    await setActiveLayer("RunningLayer");
});

document.getElementById("idleLayer").addEventListener("click", async () => {
    // Andere Layer entfernen
    await removeLayer("BasicWalkingLayer");
    await removeLayer("RunningLayer");
    await setActiveLayer("IdleLayer");
});

// Animations-Buttons
document.getElementById("idleAnimation").addEventListener("click", () => playAnimation("idle_breathing"));
document.getElementById("walkAnimation").addEventListener("click", () => playAnimation("basic_walk"));

// Spiel-Buttons
document.getElementById("newGame").addEventListener("click", () => {
    gameState.score = 0;
    scoreElement.textContent = gameState.score;
    stopCharacter();
    generateNewGoal();
    showMessage("Neues Spiel gestartet!");
});

document.getElementById("nextGoal").addEventListener("click", () => {
    generateNewGoal();
    showMessage("Neues Ziel generiert!");
});

// Tastatur-Steuerung
document.addEventListener("keydown", (event) => {
    switch(event.key) {
        case "ArrowUp":
        case "w":
            moveCharacter(0, 0, -1);
            break;
        case "ArrowDown":
        case "s":
            moveCharacter(0, 0, 1);
            break;
        case "ArrowLeft":
        case "a":
            moveCharacter(-1, 0, 0);
            break;
        case "ArrowRight":
        case "d":
            moveCharacter(1, 0, 0);
            break;
        case " ": // Leertaste
            stopCharacter();
            break;
    }
});

// Fenster-Resize-Handling
window.addEventListener("resize", setupCanvas);

// ---- Spiel starten ----
window.addEventListener("load", initGame);
