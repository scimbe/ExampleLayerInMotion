import { API_BASE_URL, gameState } from './game.js';

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

            const paddedWidth = canvas.width - 20;
            const paddedHeight = canvas.height - 20;

            gameState.character.x = Math.max(20, Math.min(newX, paddedWidth));
            gameState.character.z = Math.max(20, Math.min(newZ, paddedHeight));
            gameState.character.speed = movementSpeed;

            if (gameState.activeLayer === "BasicWalkingLayer" && movementSpeed > 0) {
                const time = Date.now() / 500;
                gameState.character.y = Math.sin(time) * 0.05;
            }

            updateStatusDisplay();
            checkGoalCollision();
        }
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error during movement:", error);
    }
}

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
                    activeLayerElement.textContent = layerClassName;
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
            activeLayerElement.textContent = layerClassName;
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

            showMessage(`Layer changed: ${layerClassName}`);
            updateStatusDisplay();
        }
    } catch (error) {
        showMessage("Error: " + error.message);
        console.error("Error changing layer:", error);
    }
}
