/**
 * gameInit.js
 * Initialisierung des Spiels und Event-Handler
 */

import { 
    API_BASE_URL, 
    gameState, 
    canvas, 
    ctx,
    updateStatusDisplay, 
    showMessage, 
    updateLayerButtons,
    updateConnectionStatus,
    generateNewGoal,
    updateGoalsDisplay
} from './game.js';

import {
    moveCharacter,
    stopCharacter,
    setActiveLayer,
    removeLayer,
    playAnimation,
    setGaitType
} from './characterMovement.js';

// Initialisiert das Spiel
export async function initializeGame() {
    // Passe Canvas-Größe an Container an
    setupCanvas();
    
    // Initialisiere Verbindungsstatus
    try {
        // Prüfe API-Verfügbarkeit (später für WebSocket-Verbindung)
        const response = await fetch(`${API_BASE_URL}/layers`, {
            method: "GET"
        });
        if (response.ok) {
            gameState.offlineMode = false;
        } else {
            gameState.offlineMode = true;
        }
    } catch (e) {
        console.warn("API nicht verfügbar, starte im Offline-Modus", e);
        gameState.offlineMode = true;
    }
    
    // Aktualisiere Verbindungsanzeige
    updateConnectionStatus();
    
    // Erstelle Charakter
    await createCharacter();
    
    // Setze Standard-Layer
    await setActiveLayer("BasicWalkingLayer");
    
    // Generiere erstes Ziel
    generateNewGoal();
    
    // Starte Animation
    startGameLoop();
    
    // Aktiviere Steuerungen
    setupControls();
    
    // Aktiviere Canvas-Steuerung
    enableCanvasControls();
    
    // Initialisiere visuelle Effekte
    initializeVisualEffects();
    
    showMessage("Spiel gestartet! Erreiche die Ziele.");
}

// Erstellt einen neuen Charakter
async function createCharacter() {
    try {
        // Für Online-Modus
        if (!gameState.offlineMode) {
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
                    gameState.character.x = data.x;
                    gameState.character.y = data.y;
                    gameState.character.z = data.z;
                    gameState.character.speed = data.speed;
                    gameState.character.rotationX = data.rotationX;
                    gameState.character.rotationY = data.rotationY;
                    gameState.character.rotationZ = data.rotationZ;
                    
                    updateStatusDisplay();
                    console.log(`Character created with ID: ${data.characterId} at position (${data.x}, ${data.y}, ${data.z})`);
                    return data;
                }
            } catch (e) {
                console.warn("API nicht verfügbar, verwende Mock-Daten", e);
                gameState.offlineMode = true;
                updateConnectionStatus();
            }
        }
        
        // Fallback für Offline-Modus
        if (gameState.offlineMode) {
            const mockCharacterId = "mock-" + Math.random().toString(36).substring(2, 9);
            gameState.character.id = mockCharacterId;
            
            // Setze explizit die Startposition in die Mitte des Canvas
            gameState.character.x = canvas.width / 2;
            gameState.character.y = 0;
            gameState.character.z = canvas.height / 2;
            gameState.character.speed = 0;
            gameState.character.rotationX = 0;
            gameState.character.rotationY = 0;
            gameState.character.rotationZ = 0;
            
            updateStatusDisplay();
            console.log(`Mock character created with ID: ${mockCharacterId} at position (${gameState.character.x}, ${gameState.character.y}, ${gameState.character.z})`);
            showMessage("Offline-Modus aktiviert");
        }
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Erstellen des Charakters:", error);
    }
}

// Richtet das Canvas ein
function setupCanvas() {
    // Passe Canvas-Größe an Container an
    const container = document.querySelector(".game-board");
    if (container && canvas) {
        canvas.width = container.clientWidth;
        canvas.height = container.clientHeight;
        console.log(`Canvas size set to ${canvas.width}x${canvas.height}`);
    } else {
        console.error("Game board container or canvas not found");
    }
}

// Startet die Game Loop
function startGameLoop() {
    if (gameState.gameLoopId) {
        cancelAnimationFrame(gameState.gameLoopId);
    }
    
    function gameLoop() {
        // Canvas leeren
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        // Zeichne Hintergrundgitter
        drawGrid();
        
        // Zeichne Ziel
        drawGoal();
        
        // Zeichne Charakter
        drawCharacter();
        
        // Debug-Infos anzeigen
        drawDebugInfo();
        
        // Offline-Modus Hinweis
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

// Zeichnet ein Hintergrundgitter
function drawGrid() {
    const gridSize = 20; // Größe der Gitterzellen
    
    ctx.save();
    
    ctx.strokeStyle = "#ecf0f1";
    ctx.lineWidth = 0.5;
    
    // Vertikale Linien
    for (let x = 0; x < canvas.width; x += gridSize) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, canvas.height);
        ctx.stroke();
    }
    
    // Horizontale Linien
    for (let y = 0; y < canvas.height; y += gridSize) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(canvas.width, y);
        ctx.stroke();
    }
    
    ctx.restore();
}

// Zeichnet das Ziel
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
    
    // Zieltext
    ctx.font = "10px Arial";
    ctx.fillStyle = "#333";
    ctx.textAlign = "center";
    ctx.fillText("ZIEL", x, y - 15);
    
    ctx.restore();
}

// Zeichnet den Charakter
function drawCharacter() {
    // Überprüfe, ob die Position gültig ist
    if (isNaN(gameState.character.x) || isNaN(gameState.character.z)) {
        console.error("Ungültige Charakterposition:", gameState.character);
        // Zurücksetzen zur Mitte
        gameState.character.x = canvas.width / 2;
        gameState.character.z = canvas.height / 2;
    }
    
    // Position des Charakters
    const x = Math.round(gameState.character.x);
    const y = Math.round(gameState.character.z); // Z-Position für Y-Koordinate im 2D-Kontext
    
    // Debug-Ausgabe der aktuellen Position
    console.log("Zeichne Charakter bei:", x, y, "Ursprüngliche Koordinaten:", gameState.character.x, gameState.character.z);
    
    // Falls die Position außerhalb des Canvas liegt, korrigieren
    if (x < 0 || y < 0 || x > canvas.width || y > canvas.height) {
        console.warn("Charakter außerhalb des Canvas! Korrigiere Position.");
        gameState.character.x = Math.max(20, Math.min(gameState.character.x, canvas.width - 20));
        gameState.character.z = Math.max(20, Math.min(gameState.character.z, canvas.height - 20));
        return; // Erneutes Zeichnen beim nächsten Frame
    }
    
    ctx.save();
    
    // Hintergrundkreis für bessere Sichtbarkeit
    ctx.beginPath();
    ctx.arc(x, y, 17, 0, Math.PI * 2);
    ctx.fillStyle = "rgba(255, 255, 255, 0.5)";
    ctx.fill();
    
    // Charakterkreis
    ctx.beginPath();
    ctx.arc(x, y, 15, 0, Math.PI * 2);
    
    // Farbe basierend auf aktivem Layer
    if (gameState.activeLayer === "RunningLayer") {
        ctx.fillStyle = "#e74c3c"; // Rot fürs Laufen
    } else if (gameState.activeLayer === "IdleLayer") {
        ctx.fillStyle = "#2ecc71"; // Grün für Idle
    } else if (gameState.activeLayer === "AdvancedWalkingLayer") {
        ctx.fillStyle = "#9b59b6"; // Lila für fortgeschrittenes Gehen
    } else {
        ctx.fillStyle = "#3498db"; // Blau für normales Gehen
    }
    
    ctx.fill();
    ctx.strokeStyle = "#2c3e50";
    ctx.lineWidth = 2;
    ctx.stroke();
    
    // Richtungsanzeige
    const angle = Math.PI * gameState.character.rotationY / 180;
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x + Math.sin(angle) * 20, y + Math.cos(angle) * 20);
    ctx.strokeStyle = "#e74c3c";
    ctx.lineWidth = 3;
    ctx.stroke();
    
    // Layer-Typ anzeigen
    ctx.font = "10px Arial";
    ctx.fillStyle = "#333";
    ctx.textAlign = "center";
    
    // Layer-Anzeige mit Gangart (für AdvancedWalkingLayer)
    let displayText = gameState.activeLayer.replace("Layer", "");
    if (gameState.activeLayer === "AdvancedWalkingLayer") {
        displayText += ` (${gameState.activeGait})`;
    }
    
    ctx.fillText(displayText, x, y - 20);
    
    ctx.restore();
}

// Zeichnet Debug-Informationen
function drawDebugInfo() {
    ctx.save();
    
    // Zeichne Koordinaten in der oberen linken Ecke
    ctx.font = "12px monospace";
    ctx.fillStyle = "#333";
    ctx.textAlign = "left";
    
    const x = Math.round(gameState.character.x);
    const y = Math.round(gameState.character.z);
    
    ctx.fillText(`Position: (${x}, ${y})`, 10, 40);
    ctx.fillText(`Speed: ${gameState.character.speed.toFixed(2)}`, 10, 55);
    ctx.fillText(`Layer: ${gameState.activeLayer}`, 10, 70);
    
    // Zeichne Gitternetzlinien für den Ursprung
    ctx.strokeStyle = "rgba(255, 0, 0, 0.5)";
    ctx.lineWidth = 1;
    
    // Vertikale Linie durch Char
    ctx.beginPath();
    ctx.moveTo(x, 0);
    ctx.lineTo(x, canvas.height);
    ctx.stroke();
    
    // Horizontale Linie durch Char
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(canvas.width, y);
    ctx.stroke();
    
    ctx.restore();
}

// Direkte Steuerung auf dem Canvas durch Touch/Maus
function enableCanvasControls() {
    let isDragging = false;
    let lastX, lastY;
    let currentDirectionX = 0;
    let currentDirectionZ = 0;
    let moveInterval = null;
    
    // Funktion für kontinuierliche Bewegung
    function continuousMove() {
        if (currentDirectionX !== 0 || currentDirectionZ !== 0) {
            moveCharacter(currentDirectionX, 0, currentDirectionZ);
        }
    }
    
    // Hilfsfunktion zur Berechnung der Richtung
    function calculateDirection(currentX, currentY) {
        const deltaX = currentX - lastX;
        const deltaY = currentY - lastY;
        
        // Berechne die Richtung als normalisierte Vektor
        const length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (length > 5) { // Mindestbewegung für Reaktion
            return { 
                dirX: deltaX / length, 
                dirZ: deltaY / length 
            };
        }
        
        return { dirX: 0, dirZ: 0 };
    }
    
    // Starte kontinuierliche Bewegung
    function startContinuousMove() {
        if (moveInterval === null) {
            moveInterval = setInterval(continuousMove, 50); // 20 FPS Bewegungsupdate
        }
    }
    
    // Stoppe kontinuierliche Bewegung
    function stopContinuousMove() {
        if (moveInterval !== null) {
            clearInterval(moveInterval);
            moveInterval = null;
        }
        currentDirectionX = 0;
        currentDirectionZ = 0;
    }
    
    // Maus-Events
    canvas.addEventListener('mousedown', (e) => {
        isDragging = true;
        lastX = e.offsetX;
        lastY = e.offsetY;
        startContinuousMove();
    });
    
    canvas.addEventListener('mousemove', (e) => {
        if (isDragging) {
            const { dirX, dirZ } = calculateDirection(e.offsetX, e.offsetY);
            currentDirectionX = dirX;
            currentDirectionZ = dirZ;
            lastX = e.offsetX;
            lastY = e.offsetY;
        }
    });
    
    canvas.addEventListener('mouseup', () => {
        isDragging = false;
        stopContinuousMove();
    });
    
    canvas.addEventListener('mouseleave', () => {
        isDragging = false;
        stopContinuousMove();
    });
    
    // Touch-Events für mobile Geräte
    canvas.addEventListener('touchstart', (e) => {
        e.preventDefault();
        isDragging = true;
        lastX = e.touches[0].clientX - canvas.getBoundingClientRect().left;
        lastY = e.touches[0].clientY - canvas.getBoundingClientRect().top;
        startContinuousMove();
    });
    
    canvas.addEventListener('touchmove', (e) => {
        e.preventDefault();
        if (isDragging) {
            const currentX = e.touches[0].clientX - canvas.getBoundingClientRect().left;
            const currentY = e.touches[0].clientY - canvas.getBoundingClientRect().top;
            
            const { dirX, dirZ } = calculateDirection(currentX, currentY);
            currentDirectionX = dirX;
            currentDirectionZ = dirZ;
            lastX = currentX;
            lastY = currentY;
        }
    });
    
    canvas.addEventListener('touchend', (e) => {
        e.preventDefault();
        isDragging = false;
        stopContinuousMove();
    });
    
    canvas.addEventListener('touchcancel', (e) => {
        e.preventDefault();
        isDragging = false;
        stopContinuousMove();
    });
    
    // Klick-Steuerung (bewegt den Charakter zum geklickten Punkt)
    canvas.addEventListener('click', (e) => {
        // Bei Klick (ohne Drag) - bewege den Charakter zum Zielpunkt
        if (!isDragging) {
            const targetX = e.offsetX;
            const targetY = e.offsetY;
            
            // Berechne Richtungsvektor zum Zielpunkt
            const dirX = targetX - gameState.character.x;
            const dirZ = targetY - gameState.character.z;
            
            // Normalisiere den Vektor
            const length = Math.sqrt(dirX * dirX + dirZ * dirZ);
            if (length > 0) {
                moveCharacter(dirX / length, 0, dirZ / length);
            }
        }
    });
}

// Aktiviert die Event-Handler für alle Steuerelemente
function setupControls() {
    // Movement-Buttons
    document.getElementById("moveUp").addEventListener("click", () => moveCharacter(0, 0, -1));
    document.getElementById("moveDown").addEventListener("click", () => moveCharacter(0, 0, 1));
    document.getElementById("moveLeft").addEventListener("click", () => moveCharacter(-1, 0, 0));
    document.getElementById("moveRight").addEventListener("click", () => moveCharacter(1, 0, 0));
    document.getElementById("moveStop").addEventListener("click", () => stopCharacter());
    
    // Layer-Buttons
    document.getElementById("walkingLayer").addEventListener("click", async () => {
        await removeLayer("RunningLayer");
        await removeLayer("IdleLayer");
        await removeLayer("AdvancedWalkingLayer");
        await setActiveLayer("BasicWalkingLayer");
    });
    
    document.getElementById("runningLayer").addEventListener("click", async () => {
        await removeLayer("BasicWalkingLayer");
        await removeLayer("IdleLayer");
        await removeLayer("AdvancedWalkingLayer");
        await setActiveLayer("RunningLayer");
    });
    
    document.getElementById("idleLayer").addEventListener("click", async () => {
        await removeLayer("BasicWalkingLayer");
        await removeLayer("RunningLayer");
        await removeLayer("AdvancedWalkingLayer");
        await setActiveLayer("IdleLayer");
    });
    
    document.getElementById("advancedWalkingLayer").addEventListener("click", async () => {
        await removeLayer("BasicWalkingLayer");
        await removeLayer("RunningLayer");
        await removeLayer("IdleLayer");
        await setActiveLayer("AdvancedWalkingLayer");
    });
    
    // Gait-Buttons
    document.getElementById("normalGait").addEventListener("click", () => setGaitType("NORMAL"));
    document.getElementById("sneakingGait").addEventListener("click", () => setGaitType("SNEAKING"));
    document.getElementById("limpingGait").addEventListener("click", () => setGaitType("LIMPING"));
    
    // Animation-Buttons
    document.getElementById("idleAnimation").addEventListener("click", () => playAnimation("idle_breathing"));
    document.getElementById("walkAnimation").addEventListener("click", () => playAnimation("basic_walk"));
    
    // Game-Buttons
    document.getElementById("newGame").addEventListener("click", () => {
        gameState.score = 0;
        document.getElementById("scoreValue").textContent = gameState.score;
        stopCharacter();
        generateNewGoal();
        showMessage("Neues Spiel gestartet!");
    });
    
    document.getElementById("nextGoal").addEventListener("click", () => {
        generateNewGoal();
        showMessage("Neues Ziel generiert!");
    });
    
    document.getElementById("reconnect").addEventListener("click", async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/layers`, {
                method: "GET"
            });
            if (response.ok) {
                gameState.offlineMode = false;
                updateConnectionStatus();
                showMessage("Verbindung hergestellt!");
            } else {
                throw new Error("API nicht erreichbar");
            }
        } catch (e) {
            gameState.offlineMode = true;
            updateConnectionStatus();
            showMessage("Verbindung fehlgeschlagen");
        }
    });
    
    // Visuelle Effekt-Steuerung
    const toggleFootsteps = document.getElementById("toggleFootsteps");
    if (toggleFootsteps) {
        toggleFootsteps.addEventListener("change", (e) => {
            if (window.visualEffects) {
                window.visualEffects.setFootstepsEnabled(e.target.checked);
            }
        });
    }
    
    const toggleParticles = document.getElementById("toggleParticles");
    if (toggleParticles) {
        toggleParticles.addEventListener("change", (e) => {
            if (window.visualEffects) {
                window.visualEffects.setParticlesEnabled(e.target.checked);
            }
        });
    }
    
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
}

// Initialisiert die visuellen Effekte
function initializeVisualEffects() {
    // Verzögert die Erstellung, um sicherzustellen, dass das Canvas fertig geladen ist
    setTimeout(() => {
        if (typeof VisualEffects !== 'undefined') {
            window.visualEffects = new VisualEffects(canvas);
            console.log("Visuelle Effekte initialisiert");
        } else {
            console.warn("VisualEffects-Klasse nicht gefunden");
        }
    }, 500);
}

// Fenster-Resize-Handling
window.addEventListener("resize", () => {
    setupCanvas();
    // Aktualisiere Zielposition für neues Canvas
    updateGoalsDisplay();
    
    // Stelle sicher, dass der Charakter im sichtbaren Bereich bleibt
    if (canvas) {
        if (gameState.character.x > canvas.width) {
            gameState.character.x = canvas.width - 50;
        }
        if (gameState.character.z > canvas.height) {
            gameState.character.z = canvas.height - 50;
        }
    }
});
