/**
 * webSocketHandler.js
 * Enthält Funktionen für die WebSocket-Kommunikation
 */

import { 
    WS_URL, 
    gameState, 
    updateStatusDisplay, 
    checkGoalCollision, 
    updateConnectionStatus, 
    updateLayerButtons 
} from './game.js';

// Stellt eine WebSocket-Verbindung her
export function connectWebSocket() {
    if (typeof WebSocket === "undefined") {
        console.warn("WebSocket wird von diesem Browser nicht unterstützt");
        gameState.offlineMode = true;
        updateConnectionStatus();
        return;
    }

    if (gameState.webSocket) {
        try {
            gameState.webSocket.close();
        } catch (e) {
            console.error("Fehler beim Schließen des WebSockets:", e);
        }
    }

    try {
        gameState.webSocket = new WebSocket(WS_URL);

        gameState.webSocket.onopen = () => {
            console.log("WebSocket verbunden");
            gameState.offlineMode = false;
            updateConnectionStatus();
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
                        // Behandle Animations-Updates
                        break;
                    case "LAYER_UPDATE":
                        if (data.characterId === gameState.character.id) {
                            gameState.activeLayer = data.activeLayer;
                            updateLayerButtons(data.activeLayer);
                            updateStatusDisplay();
                        }
                        break;
                }
            } catch (error) {
                console.error("Fehler bei der Verarbeitung der WebSocket-Nachricht:", error);
            }
        };

        gameState.webSocket.onerror = (error) => {
            console.error("WebSocket-Fehler:", error);
            gameState.offlineMode = true;
            updateConnectionStatus();
        };

        gameState.webSocket.onclose = () => {
            console.log("WebSocket-Verbindung geschlossen");
            gameState.offlineMode = true;
            updateConnectionStatus();
            setTimeout(() => connectWebSocket(), 3000);
        };
    } catch (e) {
        console.warn("Fehler beim Verbinden mit WebSocket:", e);
        gameState.offlineMode = true;
        updateConnectionStatus();
    }
}

// Sendet eine Nachricht über den WebSocket
export function sendWebSocketMessage(message) {
    if (gameState.webSocket && gameState.webSocket.readyState === WebSocket.OPEN) {
        gameState.webSocket.send(JSON.stringify(message));
    }
}
