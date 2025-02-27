import { gameState } from './game.js';

export function drawCharacter() {
    const x = gameState.character.x;
    const y = gameState.character.z; // Z-Position for Y-coordinate in 2D context

    ctx.save();

    // Character circle
    ctx.beginPath();
    ctx.arc(x, y, 15, 0, Math.PI * 2);

    // Color based on active layer
    if (gameState.activeLayer === "RunningLayer") {
        ctx.fillStyle = "#e74c3c"; // Red for running
    } else if (gameState.activeLayer === "IdleLayer") {
        ctx.fillStyle = "#2ecc71"; // Green for idle
    } else {
        ctx.fillStyle = "#3498db"; // Blue for walking
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
    ctx.fillText(gameState.activeLayer.replace("Layer", ""), x, y - 20);

    ctx.restore();
}

export function drawGrid() {
    ctx.save();

    ctx.strokeStyle = "#ecf0f1";
    ctx.lineWidth = 0.5;

    // Vertical lines
    for (let x = 0; x < canvas.width; x += GRID_SIZE) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, canvas.height);
        ctx.stroke();
    }

    // Horizontal lines
    for (let y = 0; y < canvas.height; y += GRID_SIZE) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(canvas.width, y);
        ctx.stroke();
    }

    ctx.restore();
}

export function drawGoal() {
    const x = gameState.goalPosition.x;
    const y = gameState.goalPosition.y;

    ctx.save();

    // Pulsing effect
    const time = Date.now() / 1000;
    const scale = 1 + Math.sin(time * 3) * 0.1;

    // Goal circle
    ctx.beginPath();
    ctx.arc(x, y, 12 * scale, 0, Math.PI * 2);
    ctx.fillStyle = "gold";
    ctx.fill();
    ctx.strokeStyle = "orange";
    ctx.lineWidth = 2;
    ctx.stroke();

    // Goal text
    ctx.font = "10px Arial";
    ctx.fillStyle = "#333";
    ctx.textAlign = "center";
    ctx.fillText("GOAL", x, y - 15);

    ctx.restore();
}
