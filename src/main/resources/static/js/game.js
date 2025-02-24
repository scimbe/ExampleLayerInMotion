/**
 * Motion Master Game
 * Ein Spiel zur Demonstration der Layer-basierten Charakterbewegung
 */

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
    goals: []
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
        
        if (!response.ok) {
            throw new Error("Fehler beim Erstellen des Charakters");
        }
        
        const data = await response.json();
        gameState.character.id = data.characterId;
        updateCharacterState(data);
        
        return data;
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Erstellen des Charakters:", error);
    }
}

// Bewegt den Charakter in eine Richtung
async function moveCharacter(dirX, dirY, dirZ, speed = DEFAULT_SPEED) {
    if (!gameState.character.id) return;
    
    try {
        gameState.isMoving = true;
        
        const response = await fetch(`${API_BASE_URL}/characters/${gameState.character.id}/move`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                directionX: dirX,
                directionY: dirY,
                directionZ: dirZ,
                speed: speed
            })
        });
        
        if (!response.ok) {
            throw new Error("Fehler bei der Bewegung");
        }
        
        const data = await response.json();
        updateCharacterState(data);
        
        return data;
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler bei der Bewegung:", error);
    }
}

// Spielt eine Animation ab
async function playAnimation(animationId, speed = 1.0) {
    if (!gameState.character.id) return;
    
    try {
        gameState.currentAnimation = animationId;
        
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
        
        if (!response.ok) {
            throw new Error("Fehler beim Abspielen der Animation");
        }
        
        const data = await response.json();
        updateCharacterState(data);
        
        return data;
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Abspielen der Animation:", error);
    }
}

// Stoppt alle Bewegungen und Animationen
async function stopCharacter() {
    if (!gameState.character.id) return;
    
    try {
        gameState.isMoving = false;
        gameState.currentAnimation = null;
        
        const response = await fetch(`${API_BASE_URL}/characters/${gameState.character.id}/stop`, {
            method: "POST"
        });
        
        if (!response.ok) {
            throw new Error("Fehler beim Stoppen");
        }
        
        const data = await response.json();
        updateCharacterState(data);
        
        return data;
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Stoppen:", error);
    }
}

// Ändert den aktiven Layer
async function setActiveLayer(layerClassName, priority = 1) {
    try {
        const response = await fetch(`${API_BASE_URL}/layers`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                className: `com.example.motion.sys.behavior.${layerClassName}`,
                priority: priority
            })
        });
        
        if (!response.ok) {
            throw new Error("Fehler beim Ändern des Layers");
        }
        
        const data = await response.json();
        gameState.activeLayer = layerClassName;
        activeLayerElement.textContent = layerClassName;
        
        updateLayerButtons(layerClassName);
        
        return data;
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Ändern des Layers:", error);
    }
}

// Entfernt einen Layer
async function removeLayer(layerClassName) {
    try {
        const response = await fetch(`${API_BASE_URL}/layers/${encodeURIComponent('com.example.motion.sys.behavior.' + layerClassName)}`, {
            method: "DELETE"
        });
        
        if (!response.ok) {
            throw new Error("Fehler beim Entfernen des Layers");
        }
        
        return true;
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Entfernen des Layers:", error);
    }
}

// ---- Websocket-Verbindung ----
function connectWebSocket() {
    if (gameState.webSocket) {
        gameState.webSocket.close();
    }
    
    gameState.webSocket = new WebSocket(WS_URL);
    
    gameState.webSocket.onopen = () => {
        console.log("WebSocket verbunden");
    };
    
    gameState.webSocket.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            
            switch(data.type) {
                case "POSITION_UPDATE":
                    if (data.characterId === gameState.character.id) {
                        gameState.character.x = data.position.x;
                        gameState.character.y = data.position.y;
                        gameState.character.z = data.position.z;
                        updateStatusDisplay();
                        checkGoalCollision();
                    }
                    break;
                case "ANIMATION_UPDATE":
                    // Animation-Updates verarbeiten
                    break;
                case "LAYER_UPDATE":
                    if (data.characterId === gameState.character.id) {
                        gameState.activeLayer = data.activeLayer;
                        activeLayerElement.textContent = data.activeLayer;
                        updateLayerButtons(data.activeLayer);
                    }
                    break;
            }
        } catch (error) {
            console.error("Fehler beim Verarbeiten der WebSocket-Nachricht:", error);
        }
    };
    
    gameState.webSocket.onerror = (error) => {
        console.error("WebSocket-Fehler:", error);
    };
    
    gameState.webSocket.onclose = () => {
        console.log("WebSocket-Verbindung geschlossen");
        // Automatische Wiederverbindung nach einer kurzen Verzögerung
        setTimeout(connectWebSocket, 3000);
    };
}

// ---- Spiellogik ----

// Initialisiert das Spiel
async function initGame() {
    setupCanvas();
    connectWebSocket();
    
    try {
        await createCharacter();
        await setActiveLayer("BasicWalkingLayer");
        generateNewGoal();
        startGameLoop();
        showMessage("Spiel gestartet! Erreiche die Ziele.");
    } catch (error) {
        showMessage("Fehler beim Starten des Spiels: " + error.message);
        console.error("Fehler beim Starten des Spiels:", error);
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

// ---- Rendering ----

// Zeichnet den Charakter
function drawCharacter() {
    const x = gameState.character.x;
    const y = gameState.character.z; // Z-Position für Y-Koordinate im 2D-Kontext
    
    ctx.save();
    
    // Charakter-Kreis
    ctx.beginPath();
    ctx.arc(x, y, 15, 0, Math.PI * 2);
    ctx.fillStyle = "#3498db";
    ctx.fill();
    ctx.strokeStyle = "#2980b9";
    ctx.lineWidth = 2;
    ctx.stroke();
    
    // Richtungsindikator
    const angle = Math.PI * gameState.character.rotationY / 180;
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x + Math.sin(angle) * 20, y + Math.cos(angle) * 20);
    ctx.strokeStyle = "#e74c3c";
    ctx.lineWidth = 3;
    ctx.stroke();
    
    ctx.restore();
}

// Zeichnet das Gitter
function drawGrid() {
    ctx.save();
    
    ctx.strokeStyle = "#ecf0f1";
    ctx.lineWidth = 0.5;
    
    // Vertikale Linien
    for (let x = 0; x < canvas.width; x += GRID_SIZE) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, canvas.height);
        ctx.stroke();
    }
    
    // Horizontale Linien
    for (let y = 0; y < canvas.height; y += GRID_SIZE) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(canvas.width, y);
        ctx.stroke();
    }
    
    ctx.restore();
}

// Zeichnet das aktuelle Ziel
function drawGoal() {
    const x = gameState.goalPosition.x;
    const y = gameState.goalPosition.y;
    
    ctx.save();
    
    // Pulsierender Effekt
    const time = Date.now() / 1000;
    const scale = 1 + Math.sin(time * 3) * 0.1;
    
    // Zielkreis
    ctx.beginPath();
    ctx.arc(x, y, 12 * scale, 0, Math.PI * 2);
    ctx.fillStyle = "gold";
    ctx.fill();
    ctx.strokeStyle = "orange";
    ctx.lineWidth = 2;
    ctx.stroke();
    
    ctx.restore();
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
