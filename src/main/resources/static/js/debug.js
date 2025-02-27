/**
 * debug.js
 * Hilfsfunktionen zur Fehlersuche
 */

// Fügt Debug-Ausgabe für den Charakter hinzu
export function logCharacterState() {
    console.log("Aktuelle Charakterposition:", 
        "x =", gameState.character.x, 
        "y =", gameState.character.y, 
        "z =", gameState.character.z, 
        "speed =", gameState.character.speed,
        "rotation =", gameState.character.rotationY
    );
}

// Fügt visuelle Debug-Indikatoren zum Canvas hinzu
export function addDebugVisualization(ctx) {
    if (!ctx) return;
    
    // Zeichne Koordinatenachsen
    ctx.save();
    
    // X-Achse (rot)
    ctx.beginPath();
    ctx.moveTo(0, 10);
    ctx.lineTo(100, 10);
    ctx.strokeStyle = "red";
    ctx.lineWidth = 2;
    ctx.stroke();
    
    // Z-Achse (blau)
    ctx.beginPath();
    ctx.moveTo(10, 0);
    ctx.lineTo(10, 100);
    ctx.strokeStyle = "blue";
    ctx.lineWidth = 2;
    ctx.stroke();
    
    // Koordinatenursprung
    ctx.fillStyle = "black";
    ctx.fillText("(0,0)", 15, 15);
    
    ctx.restore();
}

// Markiert die Charakterposition mit Koordinaten
export function markCharacterPosition(ctx, x, y) {
    if (!ctx) return;
    
    ctx.save();
    
    // Markiere die exakte Position
    ctx.beginPath();
    ctx.arc(x, y, 3, 0, Math.PI * 2);
    ctx.fillStyle = "red";
    ctx.fill();
    
    // Zeige Koordinaten an
    ctx.font = "10px monospace";
    ctx.fillStyle = "black";
    ctx.fillText(`(${Math.round(x)},${Math.round(y)})`, x + 10, y - 10);
    
    ctx.restore();
}
