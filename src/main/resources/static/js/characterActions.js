import { 
    gameState, 
    API_BASE_URL, 
    showMessage, 
    updateCharacterState, 
    updateLayerButtons,
    updateGaitButtons,
    updateStatusDisplay
} from './game.js';

/**
 * Bewegt den Charakter in eine bestimmte Richtung
 */
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
                    return data;
                }
            } catch (e) {
                console.warn("API not reachable, switching to offline mode", e);
                gameState.offlineMode = true;
            }
        }

        if (gameState.offlineMode) {
            const angle = Math.atan2(dirX, dirZ);
            gameState.character.rotationY = angle * (180 / Math.PI);

            let movementSpeed = speed;
            if (gameState.activeLayer === "RunningLayer") {
                movementSpeed = speed * 2;
            } else if (gameState.activeLayer === "IdleLayer") {
                movementSpeed = 0;
            }

            const newX = gameState.character.x + (dirX * movementSpeed * 5);
            const newZ = gameState.character.z + (dirZ * movementSpeed * 5);

            if (canvas) {
                const paddedWidth = canvas.width - 20;
                const paddedHeight = canvas.height - 20;
                gameState.character.x = Math.max(20, Math.min(newX, paddedWidth));
                gameState.character.z = Math.max(20, Math.min(newZ, paddedHeight));
            } else {
                gameState.character.x = newX;
                gameState.character.z = newZ;
            }
            
            gameState.character.speed = movementSpeed;

            if (gameState.activeLayer === "BasicWalkingLayer" && movementSpeed > 0) {
                const time = Date.now() / 500;
                gameState.character.y = Math.sin(time) * 0.05;
            }

            console.log(`Character moved to position (${gameState.character.x}, ${gameState.character.y}, ${gameState.character.z}) with speed ${gameState.character.speed}`);
            updateStatusDisplay();
        }
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error during movement:", error);
    }
}

/**
 * Stoppt die Bewegung des Charakters
 */
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
        }
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error during stop:", error);
    }
}

/**
 * Setzt den aktiven Bewegungslayer
 */
export async function setActiveLayer(layerClassName, priority = 1) {
    try {
        if (!gameState.offlineMode) {
            try {
                // Entferne vorhandene Layer
                if (layerClassName !== "BasicWalkingLayer") {
                    await removeLayer("BasicWalkingLayer");
                }
                if (layerClassName !== "RunningLayer") {
                    await removeLayer("RunningLayer");
                }
                if (layerClassName !== "IdleLayer") {
                    await removeLayer("IdleLayer");
                }
                if (layerClassName !== "AdvancedWalkingLayer") {
                    await removeLayer("AdvancedWalkingLayer");
                }
                
                // Neuen Layer hinzufügen
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
                    return data;
                }
            } catch (e) {
                console.warn("API not reachable, switching to offline mode", e);
                gameState.offlineMode = true;
            }
        }

        if (gameState.offlineMode) {
            gameState.activeLayer = layerClassName;
            updateLayerButtons(layerClassName);

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

            showMessage(`Layer geändert: ${layerClassName}`);
            updateStatusDisplay();
        }
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error changing layer:", error);
    }
}

/**
 * Setzt die Gangart (für AdvancedWalkingLayer)
 */
export function setGaitType(gaitType) {
    try {
        if (gameState.activeLayer !== "AdvancedWalkingLayer") {
            showMessage("Gangart kann nur im erweiterten Geh-Modus geändert werden.");
            return;
        }
        
        if (!gameState.offlineMode) {
            // Im Online-Modus müsste hier eine API-Anfrage erfolgen
            // Da keine entsprechende API vorhanden ist, wir speichern es nur lokal
            console.log("Changing gait type to:", gaitType);
        }
        
        gameState.activeGait = gaitType;
        updateGaitButtons(gaitType);
        showMessage(`Gangart geändert: ${gaitType}`);
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error changing gait type:", error);
    }
}

/**
 * Entfernt einen Layer
 */
export async function removeLayer(layerClassName) {
    try {
        if (!gameState.offlineMode) {
            try {
                const response = await fetch(`${API_BASE_URL}/layers/${encodeURIComponent(`com.example.motion.sys.behavior.${layerClassName}`)}`, {
                    method: "DELETE"
                });

                if (response.ok) {
                    return true;
                }
            } catch (e) {
                console.warn("API not reachable, switching to offline mode", e);
                gameState.offlineMode = true;
            }
        }

        return true;
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error removing layer:", error);
        return false;
    }
}

/**
 * Spielt eine Animation ab
 */
export async function playAnimation(animationId, speed = 1.0) {
    if (!gameState.character.id) return;
    
    try {
        gameState.currentAnimation = animationId;
        
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
                console.warn("API not reachable, switching to offline mode", e);
                gameState.offlineMode = true;
            }
        }
        
        if (gameState.offlineMode) {
            showMessage(`Animation: ${animationId}`);
            
            // Visuellen Effekt für Animation hinzufügen
            const visualEffects = window.visualEffects;
            if (visualEffects) {
                const bobbing = animationId === "idle_breathing" ? 0.5 : 1.0;
                
                // Animation-spezifischer Effekt
                if (animationId === "idle_breathing") {
                    // Sanftes Pulsieren für Atmen
                    visualEffects.createLayerChangeEffect(
                        gameState.character.x,
                        gameState.character.z,
                        "IdleLayer",
                        5
                    );
                } else if (animationId === "basic_walk") {
                    // Fußspuren für Gehen
                    for (let i = 0; i < 3; i++) {
                        const offset = 10 + i * 20;
                        const angle = gameState.character.rotationY;
                        const dirRad = angle * Math.PI / 180;
                        
                        const footX = gameState.character.x - Math.sin(dirRad) * offset;
                        const footY = gameState.character.z - Math.cos(dirRad) * offset;
                        
                        visualEffects.createFootstep(
                            footX,
                            footY,
                            angle,
                            i % 2 === 0, // Abwechselnd links/rechts
                            gameState.activeLayer
                        );
                    }
                }
            }
        }
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error playing animation:", error);
    }
}
