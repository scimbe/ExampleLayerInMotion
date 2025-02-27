import { WS_URL, gameState } from './game.js';

export function connectWebSocket() {
    if (typeof WebSocket === "undefined") {
        console.warn("WebSocket is not supported by this browser");
        gameState.offlineMode = true;
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
                        // Handle animation updates
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
                console.error("Error processing WebSocket message:", error);
            }
        };

        gameState.webSocket.onerror = (error) => {
            console.error("WebSocket error:", error);
            gameState.offlineMode = true;
        };

        gameState.webSocket.onclose = () => {
            console.log("WebSocket connection closed");
            gameState.offlineMode = true;
            setTimeout(connectWebSocket, 3000);
        };
    } catch (e) {
        console.warn("Error connecting to WebSocket:", e);
        gameState.offlineMode = true;
    }
}
