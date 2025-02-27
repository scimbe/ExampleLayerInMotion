import { gameState, updateConnectionStatus, updateCharacterState } from './game.js';

const WS_URL = "ws://" + window.location.host + "/motion-updates";

/**
 * Stellt eine WebSocket-Verbindung her
 */
export function connectWebSocket() {
    if (typeof WebSocket === "undefined") {
        console.warn("WebSocket is not supported by this browser");
        gameState.offlineMode = true;
        updateConnectionStatus();
        return;
    }

    if (gameState.webSocket) {
        try {
            gameState.webSocket.close();
        } catch (e) {
            console.error("Error closing WebSocket:", e);
        }
    }

    try {
        gameState.webSocket = new WebSocket(WS_URL);

        gameState.webSocket.onopen = () => {
            console.log("WebSocket connected");
            gameState.offlineMode = false;
            updateConnectionStatus();
        };

        gameState.webSocket.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);

                switch(data.type) {
                    case "POSITION_UPDATE":
                        if (data.characterId === gameState.character.id) {
                            updateWebSocketPosition(data);
                        }
                        break;
                    case "ANIMATION_UPDATE":
                        handleAnimationUpdate(data);
                        break;
                    case "LAYER_UPDATE":
                        handleLayerUpdate(data);
                        break;
                }
            } catch (error) {
                console.error("Error processing WebSocket message:", error);
            }
        };

        gameState.webSocket.onerror = (error) => {
            console.error("WebSocket error:", error);
            gameState.offlineMode = true;
            updateConnectionStatus();
        };

        gameState.webSocket.onclose = () => {
            console.log("WebSocket connection closed");
            gameState.offlineMode = true;
            updateConnectionStatus();
            setTimeout(connectWebSocket, 3000);
        };
    } catch (e) {
        console.warn("Error connecting to WebSocket:", e);
        gameState.offlineMode = true;
        updateConnectionStatus();
    }
}

/**
 * Verarbeitet Updates zur Charakterposition vom Server
 */
function updateWebSocketPosition(data) {
    if (!data.position) {
        console.error("Invalid position update:", data);
        return;
    }
    
    // Charakter-Position aktualisieren
    const updatedState = {
        x: data.position.x,
        y: data.position.y,
        z: data.position.z,
        speed: gameState.character.speed, // Geschwindigkeit beibehalten
        rotationX: gameState.character.rotationX,
        rotationY: gameState.character.rotationY,
        rotationZ: gameState.character.rotationZ
    };
    
    updateCharacterState(updatedState);
}

/**
 * Verarbeitet Animation-Updates vom Server
 */
function handleAnimationUpdate(data) {
    if (data.characterId !== gameState.character.id) return;
    
    gameState.currentAnimation = data.animationId;
    
    // Animation-spezifische Effekte könnten hier hinzugefügt werden
    const visualEffects = window.visualEffects;
    if (visualEffects && data.animationId) {
        visualEffects.createLayerChangeEffect(
            gameState.character.x,
            gameState.character.z,
            gameState.activeLayer,
            10
        );
    }
}

/**
 * Verarbeitet Layer-Updates vom Server
 */
function handleLayerUpdate(data) {
    if (data.characterId !== gameState.character.id) return;
    
    // Aktuelles Layer aktualisieren
    gameState.activeLayer = data.activeLayer;
    
    // UI-Elemente entsprechend aktualisieren
    const updateLayerButtons = window.updateLayerButtons;
    if (typeof updateLayerButtons === 'function') {
        updateLayerButtons(data.activeLayer);
    }
}
