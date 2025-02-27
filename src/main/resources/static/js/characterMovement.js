/**
 * characterMovement.js
 * Enthält Funktionen zur Steuerung der Charakterbewegung
 */

import { 
    API_BASE_URL, 
    gameState, 
    canvas, 
    ctx,
    updateCharacterState, 
    updateStatusDisplay, 
    checkGoalCollision, 
    showMessage,
    updateLayerButtons,
    updateMovementTypeIndicator
} from './game.js';

// Bewegt den Charakter in die angegebene Richtung
export async function moveCharacter(dirX, dirY, dirZ, speed = 1.0) {
    if (!gameState.character.id) return;

    try {
        gameState.isMoving = true;

        if (!gameState.offlineMode) {
            try {
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

                if (response.ok) {
                    const data = await response.json();
                    updateCharacterState(data);
                    console.log(`Character moved to position (${data.x}, ${data.y}, ${data.z}) with speed ${data.speed}`);
                    checkGoalCollision();
                    return data;
                }
            } catch (e) {
                console.warn("API not reachable, switching to offline mode", e);
                gameState.offlineMode = true;
            }
        }

        if (gameState.offlineMode) {
            // Berechne Rotationswinkel basierend auf Bewegungsrichtung
            const angle = Math.atan2(dirX, dirZ);
            gameState.character.rotationY = angle * (180 / Math.PI);

            // Bestimme Geschwindigkeit basierend auf aktivem Layer
            let movementSpeed = speed;
            if (gameState.activeLayer === "RunningLayer") {
                movementSpeed = speed * 2;
            } else if (gameState.activeLayer === "IdleLayer") {
                movementSpeed = 0;
            } else if (gameState.activeLayer === "AdvancedWalkingLayer") {
                // Anpassung basierend auf Gangart
                if (gameState.activeGait === "SNEAKING") {
                    movementSpeed = speed * 0.6;
                } else if (gameState.activeGait === "LIMPING") {
                    movementSpeed = speed * 0.8;
                }
            }

            // Verstärke die Bewegung für bessere Sichtbarkeit
            const moveScale = 5;
            
            // Debug-Ausgabe vor der Bewegung
            console.log("Vor Bewegung:", 
                "x =", gameState.character.x, 
                "z =", gameState.character.z, 
                "Richtung =", dirX, dirZ, 
                "Speed =", movementSpeed
            );
            
            // Stellen wir sicher, dass die Koordinaten korrekt initialisiert sind
            if (isNaN(gameState.character.x) || isNaN(gameState.character.z)) {
                console.warn("Charakter-Position ungültig, setze auf Standardwerte");
                gameState.character.x = canvas ? canvas.width / 2 : 300;
                gameState.character.z = canvas ? canvas.height / 2 : 200;
            }
            
            // Neue Position berechnen
            const newX = gameState.character.x + (dirX * movementSpeed * moveScale);
            const newZ = gameState.character.z + (dirZ * movementSpeed * moveScale);

            // Begrenzung auf den Canvas
            if (canvas) {
                const paddedWidth = canvas.width - 20;
                const paddedHeight = canvas.height - 20;
                
                // Neue Position innerhalb der Grenzen setzen
                gameState.character.x = Math.max(20, Math.min(newX, paddedWidth));
                gameState.character.z = Math.max(20, Math.min(newZ, paddedHeight));
            } else {
                gameState.character.x = newX;
                gameState.character.z = newZ;
            }
            
            gameState.character.speed = movementSpeed;

            // Animationseffekt für BasicWalkingLayer
            if (gameState.activeLayer === "BasicWalkingLayer" && movementSpeed > 0) {
                const time = Date.now() / 500;
                gameState.character.y = Math.sin(time) * 0.05;
            }
            
            // Debug-Ausgabe nach der Bewegung
            console.log("Nach Bewegung:", 
                "x =", gameState.character.x, 
                "z =", gameState.character.z, 
                "Speed =", gameState.character.speed
            );

            // Erzeuge visuelle Effekte bei Bewegung
            const visualEffects = window.visualEffects;
            if (visualEffects && movementSpeed > 0) {
                // Wechsel zwischen linkem und rechtem Fuß basierend auf Bewegung
                const now = Date.now();
                const isLeftFoot = Math.floor(now / 300) % 2 === 0;
                
                visualEffects.createFootstep(
                    gameState.character.x, 
                    gameState.character.z, 
                    gameState.character.rotationY, 
                    isLeftFoot, 
                    gameState.activeLayer
                );
                
                // Staubeffekte
                visualEffects.createDustParticles(
                    gameState.character.x, 
                    gameState.character.z, 
                    gameState.character.rotationY,
                    movementSpeed,
                    gameState.activeLayer,
                    Math.floor(movementSpeed * 3)
                );
            }

            // Zeichnen einer kleinen Punktmarkierung an der aktuellen Position (Debug)
            if (ctx) {
                ctx.save();
                ctx.fillStyle = "red";
                ctx.fillRect(gameState.character.x - 2, gameState.character.z - 2, 4, 4);
                ctx.restore();
            }

            console.log(`Character moved to position (${gameState.character.x}, ${gameState.character.y}, ${gameState.character.z}) with speed ${gameState.character.speed}`);
            updateStatusDisplay();
            checkGoalCollision();
        }
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error during movement:", error);
    }
}

// Stoppt die Bewegung des Charakters
export async function stopCharacter() {
    if (!gameState.character.id) return;

    try {
        gameState.isMoving = false;
        gameState.currentAnimation = null;

        if (!gameState.offlineMode) {
            try {
                const response = await fetch(`${API_BASE_URL}/characters/${gameState.character.id}/stop`, {
                    method: "POST"
                });

                if (response.ok) {
                    const data = await response.json();
                    updateCharacterState(data);
                    return data;
                }
            } catch (e) {
                console.warn("API not reachable, switching to offline mode", e);
                gameState.offlineMode = true;
            }
        }

        if (gameState.offlineMode) {
            gameState.character.speed = 0;
            updateStatusDisplay();
            
            // Zeige Stoppstatus an
            showMessage("Bewegung angehalten");
        }
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error during stop:", error);
    }
}

// Setzt den aktiven Layer
export async function setActiveLayer(layerClassName, priority = 1) {
    try {
        if (!gameState.offlineMode) {
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

                if (response.ok) {
                    const data = await response.json();
                    gameState.activeLayer = layerClassName;
                    updateLayerButtons(layerClassName);
                    
                    // Visuelle Effekte für Layer-Wechsel
                    const visualEffects = window.visualEffects;
                    if (visualEffects) {
                        visualEffects.createLayerChangeEffect(
                            gameState.character.x,
                            gameState.character.z,
                            layerClassName,
                            20
                        );
                    }
                    
                    return data;
                }
            } catch (e) {
                console.warn("API not reachable, switching to offline mode", e);
                gameState.offlineMode = true;
            }
        }

        if (gameState.offlineMode) {
            // Speichere den vorherigen Layer für visuelle Effekte
            const prevLayer = gameState.activeLayer;
            
            // Aktualisiere aktiven Layer
            gameState.activeLayer = layerClassName;
            updateLayerButtons(layerClassName);

            // Passe Geschwindigkeit basierend auf Layer-Typ an
            if (layerClassName === "RunningLayer") {
                if (gameState.isMoving) {
                    gameState.character.speed = 1.0 * 2.0;
                }
            } else if (layerClassName === "IdleLayer") {
                gameState.character.speed = 0;
            } else {
                if (gameState.isMoving) {
                    gameState.character.speed = 1.0;
                }
            }

            // Visuelle Effekte für Layer-Wechsel
            const visualEffects = window.visualEffects;
            if (visualEffects && prevLayer !== layerClassName) {
                visualEffects.createLayerChangeEffect(
                    gameState.character.x,
                    gameState.character.z,
                    layerClassName,
                    20
                );
            }

            showMessage(`Layer geändert: ${layerClassName}`);
            updateStatusDisplay();
            updateMovementTypeIndicator(layerClassName);
        }
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error changing layer:", error);
    }
}

// Entfernt einen Layer
export async function removeLayer(layerClassName) {
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

// Spielt eine Animation ab
export async function playAnimation(animationId, speed = 1.0) {
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

// Setzt die Gangart für den Advanced Walking Layer
export async function setGaitType(gaitType) {
    try {
        if (gameState.activeLayer !== "AdvancedWalkingLayer") {
            showMessage("Gangarten sind nur im fortgeschrittenen Gehens-Layer verfügbar");
            return;
        }
        
        // Aktualisiere die aktive Gangart
        gameState.activeGait = gaitType;
        
        // Aktualisiere die Bewegungsanzeige
        updateMovementTypeIndicator(gameState.activeLayer);
        
        // Visuelles Feedback
        showMessage(`Gangart geändert: ${gaitType}`);
    } catch (error) {
        showMessage("Fehler: " + error.message);
        console.error("Fehler beim Ändern der Gangart:", error);
    }
}
