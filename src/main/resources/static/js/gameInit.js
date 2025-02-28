import {
    gameState,
    canvas,
    ctx,
    showMessage,
    updateConnectionStatus,
    updateCharacterState,
    updateLayerButtons,
    updateGaitButtons,
    setupCanvasControls,
    generateNewGoal,
    updateMagneticMovement
} from './game.js';

import { drawCharacter, drawGrid, drawGoal } from './gameRendering.js';
import { moveCharacter, stopCharacter, setActiveLayer, setGaitType, playAnimation } from './characterMovement.js';

// Initialisiert das Spiel
export function initializeGame() {
    setupCanvas();
    setupEventListeners();
    startInitializing();
}

// Richtet das Canvas ein - angepasst für Vollbild
function setupCanvas() {
    if (!canvas) return;
    
    // Passe Canvas-Größe an den vollständigen Viewport an
    const resizeCanvas = () => {
        // Für mobile Vollbildansicht
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        
        console.log(`Canvas resized to ${canvas.width}x${canvas.height}`);
    };
    
    // Initial anpassen
    resizeCanvas();
    
    // Canvas-Steuerung aktivieren
    setupCanvasControls();
    
    // Bei Größenänderung anpassen
    window.addEventListener('resize', resizeCanvas);
    
    // Auch bei Orientierungsänderung auf Mobilgeräten
    window.addEventListener('orientationchange', () => {
        // Kurz verzögern, um sicherzustellen, dass die Größenänderung abgeschlossen ist
        setTimeout(resizeCanvas, 100);
    });
}

// Richtet Event-Listener ein
function setupEventListeners() {
    // Layer-Buttons
    const walkingLayerBtn = document.getElementById("walkingLayer");
    const runningLayerBtn = document.getElementById("runningLayer");
    const idleLayerBtn = document.getElementById("idleLayer");
    const advancedLayerBtn = document.getElementById("advancedWalkingLayer");
    
    if (walkingLayerBtn) {
        walkingLayerBtn.addEventListener("click", async () => {
            await setActiveLayer("BasicWalkingLayer");
        });
    }
    
    if (runningLayerBtn) {
        runningLayerBtn.addEventListener("click", async () => {
            await setActiveLayer("RunningLayer");
        });
    }
    
    if (idleLayerBtn) {
        idleLayerBtn.addEventListener("click", async () => {
            await setActiveLayer("IdleLayer");
        });
    }
    
    if (advancedLayerBtn) {
        advancedLayerBtn.addEventListener("click", async () => {
            await setActiveLayer("AdvancedWalkingLayer");
        });
    }
    
    // Gait-Buttons (Gangarten)
    const normalGaitBtn = document.getElementById("normalGait");
    const sneakingGaitBtn = document.getElementById("sneakingGait");
    const limpingGaitBtn = document.getElementById("limpingGait");
    
    if (normalGaitBtn) {
        normalGaitBtn.addEventListener("click", () => {
            setGaitType("NORMAL");
        });
    }
    
    if (sneakingGaitBtn) {
        sneakingGaitBtn.addEventListener("click", () => {
            setGaitType("SNEAKING");
        });
    }
    
    if (limpingGaitBtn) {
        limpingGaitBtn.addEventListener("click", () => {
            setGaitType("LIMPING");
        });
    }
    
    // Animation-Buttons
    const idleAnimationBtn = document.getElementById("idleAnimation");
    const walkAnimationBtn = document.getElementById("walkAnimation");
    
    if (idleAnimationBtn) {
        idleAnimationBtn.addEventListener("click", () => {
            playAnimation("idle_breathing");
        });
    }
    
    if (walkAnimationBtn) {
        walkAnimationBtn.addEventListener("click", () => {
            playAnimation("basic_walk");
        });
    }
    
    // Spiel-Buttons
    const newGameBtn = document.getElementById("newGame");
    const nextGoalBtn = document.getElementById("nextGoal");
    const reconnectBtn = document.getElementById("reconnect");
    
    if (newGameBtn) {
        newGameBtn.addEventListener("click", () => {
            gameState.score = 0;
            const scoreValue = document.getElementById("scoreValue");
            if (scoreValue) {
                scoreValue.textContent = gameState.score;
            }
            stopCharacter();
            generateNewGoal();
            showMessage("Neues Spiel gestartet!");
        });
    }
    
    if (nextGoalBtn) {
        nextGoalBtn.addEventListener("click", () => {
            generateNewGoal();
            showMessage("Neues Ziel generiert!");
        });
    }
    
    if (reconnectBtn) {
        reconnectBtn.addEventListener("click", () => {
            try {
                import('./webSocketHandler.js')
                    .then(module => {
                        module.connectWebSocket();
                        showMessage("Verbindung wird wiederhergestellt...");
                    });
            } catch (e) {
                console.error("Fehler beim Verbinden:", e);
                showMessage("Verbindung fehlgeschlagen.");
            }
        });
    }
    
    // Visuelle Effekte Toggles
    const toggleFootsteps = document.getElementById("toggleFootsteps");
    const toggleParticles = document.getElementById("toggleParticles");
    
    if (toggleFootsteps) {
        toggleFootsteps.addEventListener("change", (e) => {
            const visualEffects = window.visualEffects;
            if (visualEffects) {
                visualEffects.setFootstepsEnabled(e.target.checked);
            }
        });
    }
    
    if (toggleParticles) {
        toggleParticles.addEventListener("change", (e) => {
            const visualEffects = window.visualEffects;
            if (visualEffects) {
                visualEffects.setParticlesEnabled(e.target.checked);
            }
        });
    }
}

// Startet die Initialisierung des Spiels
async function startInitializing() {
    try {
        // WebSocket-Verbindung herstellen
        try {
            const wsModule = await import('./webSocketHandler.js');
            wsModule.connectWebSocket();
        } catch (e) {
            console.warn("WebSocket konnte nicht initialisiert werden:", e);
            gameState.offlineMode = true;
        }
        
        // Charakter erstellen
        await createCharacter();
        
        // Mit Walking Layer beginnen
        await setActiveLayer("BasicWalkingLayer");
        
        // Erstes Ziel generieren
        generateNewGoal();
        
        // Spielschleife starten
        startGameLoop();
        
        // Erfolgsbenachrichtigung
        showMessage("Spiel gestartet! Erreiche die Ziele mit dem Magneten.");
        
        // Verbindungsstatus aktualisieren
        updateConnectionStatus();
    } catch (error) {
        console.error("Fehler beim Starten des Spiels:", error);
        
        // Notfall-Fallback: Trotzdem versuchen zu starten
        gameState.offlineMode = true;
        gameState.character.id = "fallback-" + Math.random().toString(36).substring(2, 9);
        gameState.character.x = canvas.width / 2;
        gameState.character.z = canvas.height / 2;
        generateNewGoal();
        startGameLoop();
        updateConnectionStatus();
        showMessage("Offline-Modus aktiviert. Tippe auf das Spielfeld, um den Magneten zu steuern.");
    }
}

// Erstellt einen neuen Charakter
async function createCharacter() {
    try {
        // Online-Modus versuchen
        if (!gameState.offlineMode) {
            try {
                const response = await fetch(`/api/v1/characters`, {
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
            return mockCharacter;
        }
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Erstellen des Charakters:", error);
        throw error;
    }
}

// Startet die Spielschleife
function startGameLoop() {
    if (gameState.gameLoopId) {
        cancelAnimationFrame(gameState.gameLoopId);
    }
    
    function gameLoop() {
        // Canvas leeren
        if (ctx && canvas) {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
        }
        
        // Spielfeld zeichnen
        drawGrid();
        
        // Ziel zeichnen
        drawGoal();
        
        // Magnetische Bewegung aktualisieren
        updateMagneticMovement();
        
        // Charakter zeichnen
        drawCharacter();
        
        // Offline-Modus-Anzeige
        if (gameState.offlineMode && ctx && canvas) {
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
