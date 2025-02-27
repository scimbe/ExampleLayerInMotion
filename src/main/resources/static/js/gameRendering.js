// Zeichnet den Charakter
export function drawCharacter() {
    if (!ctx) return;
    
    // Überprüfe, ob die Charakterposition gültig ist
    if (isNaN(gameState.character.x) || isNaN(gameState.character.z)) {
        console.error("Ungültige Charakterposition:", gameState.character);
        // Zurücksetzen zur Mitte als Fallback
        gameState.character.x = canvas.width / 2;
        gameState.character.z = canvas.height / 2;
    }
    
    const x = gameState.character.x;
    const y = gameState.character.z; // Z-Position for Y-coordinate in 2D context

    // Charakter in der Ecke erkennen und warnen
    const minThreshold = 20;
    if (x <= minThreshold && y <= minThreshold) {
        console.warn("Charakter in der Ecke erkannt, Position:", x, y);
    }
    
    ctx.save();

    // Hintergrundkreis für bessere Sichtbarkeit
    ctx.beginPath();
    ctx.arc(x, y, 17, 0, Math.PI * 2);
    ctx.fillStyle = "rgba(255, 255, 255, 0.5)";
    ctx.fill();

    // Character circle
    ctx.beginPath();
    ctx.arc(x, y, 15, 0, Math.PI * 2);

    // Color based on active layer
    if (gameState.activeLayer === "RunningLayer") {
        ctx.fillStyle = "#e74c3c"; // Red for running
    } else if (gameState.activeLayer === "IdleLayer") {
        ctx.fillStyle = "#2ecc71"; // Green for idle
    } else if (gameState.activeLayer === "AdvancedWalkingLayer") {
        ctx.fillStyle = "#9b59b6"; // Purple for advanced walking
    } else {
        ctx.fillStyle = "#3498db"; // Blue for basic walking
    }

    ctx.fill();
    ctx.strokeStyle = "#2c3e50";
    ctx.lineWidth = 2;
    ctx.stroke();

    // Direction indicator
    const angle = Math.PI * gameState.character.rotationY / 180;
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x + Math.sin(angle) * 20, y + Math.cos(angle) * 20);
    ctx.strokeStyle = "#e74c3c";
    ctx.lineWidth = 3;
    ctx.stroke();

    // Show active layer as text
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
    
    // Debug-Ausgabe
    if (gameState.isMoving) {
        console.log("Charakter gezeichnet bei:", x, y, "Layer:", gameState.activeLayer);
    }
}
