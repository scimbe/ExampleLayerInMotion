import { gameState, canvas, ctx } from './game.js';

// Zeichnet das Gitternetz
export function drawGrid() {
    if (!ctx || !canvas) return;
    
    ctx.save();

    ctx.strokeStyle = "#ecf0f1";
    ctx.lineWidth = 0.5;

    // Vertikale Linien
    for (let x = 0; x < canvas.width; x += 20) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, canvas.height);
        ctx.stroke();
    }

    // Horizontale Linien
    for (let y = 0; y < canvas.height; y += 20) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(canvas.width, y);
        ctx.stroke();
    }

    ctx.restore();
}

// Zeichnet das Ziel
export function drawGoal() {
    if (!ctx || !gameState.goalPosition) return;
    
    const x = gameState.goalPosition.x;
    const y = gameState.goalPosition.y;

    ctx.save();

    // Pulsierender Effekt
    const time = Date.now() / 1000;
    const scale = 1 + Math.sin(time * 3) * 0.1;

    // Metallisches Objekt (als Magnetanziehung)
    ctx.beginPath();
    ctx.arc(x, y, 12 * scale, 0, Math.PI * 2);
    
    // Metallischer Verlauf
    const gradient = ctx.createRadialGradient(x, y, 0, x, y, 15 * scale);
    gradient.addColorStop(0, '#e0e0e0'); // Helles Silber
    gradient.addColorStop(0.7, '#a0a0a0'); // Mittleres Silber
    gradient.addColorStop(1, '#707070'); // Dunkles Silber
    
    ctx.fillStyle = gradient;
    ctx.fill();
    ctx.strokeStyle = "#505050";
    ctx.lineWidth = 2;
    ctx.stroke();

    // Metallische Reflexion
    ctx.beginPath();
    ctx.arc(x - 3, y - 3, 5 * scale, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(255, 255, 255, 0.4)';
    ctx.fill();

    // Zieltext
    ctx.font = "10px Arial";
    ctx.fillStyle = "#333";
    ctx.textAlign = "center";
    ctx.fillText("ZIEL", x, y - 18);

    ctx.restore();
}

// Zeichnet den Charakter als Magneten
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
    const y = gameState.character.z; // Z-Position für Y-Koordinate im 2D-Kontext

    ctx.save();

    // Grundform des Magneten
    const magnetHeight = 25;
    const magnetWidth = 15;

    // Rotation anwenden
    ctx.translate(x, y);
    ctx.rotate(Math.PI * gameState.character.rotationY / 180);
    
    // Festlegen der Farbe basierend auf dem aktiven Layer
    let magnetColor;
    let magnetGradient;
    
    if (gameState.activeLayer === "RunningLayer") {
        magnetColor = "#e74c3c"; // Rot für Laufen
        magnetGradient = ctx.createLinearGradient(0, -magnetHeight/2, 0, magnetHeight/2);
        magnetGradient.addColorStop(0, "#e74c3c");
        magnetGradient.addColorStop(1, "#c0392b");
    } else if (gameState.activeLayer === "IdleLayer") {
        magnetColor = "#2ecc71"; // Grün für Idle
        magnetGradient = ctx.createLinearGradient(0, -magnetHeight/2, 0, magnetHeight/2);
        magnetGradient.addColorStop(0, "#2ecc71");
        magnetGradient.addColorStop(1, "#27ae60");
    } else if (gameState.activeLayer === "AdvancedWalkingLayer") {
        magnetColor = "#9b59b6"; // Lila für erweitertes Gehen
        magnetGradient = ctx.createLinearGradient(0, -magnetHeight/2, 0, magnetHeight/2);
        magnetGradient.addColorStop(0, "#9b59b6");
        magnetGradient.addColorStop(1, "#8e44ad");
    } else {
        magnetColor = "#3498db"; // Blau für normales Gehen
        magnetGradient = ctx.createLinearGradient(0, -magnetHeight/2, 0, magnetHeight/2);
        magnetGradient.addColorStop(0, "#3498db");
        magnetGradient.addColorStop(1, "#2980b9");
    }
    
    // Magnetisches Kraftfeld zeichnen (wenn Bewegung aktiv)
    if (gameState.isMoving && gameState.character.speed > 0.1) {
        // Kraftfeld-Linien
        const fieldIntensity = Math.min(1, gameState.character.speed / 2);
        const fieldSize = magnetHeight * (1 + fieldIntensity);
        const fieldCount = Math.floor(5 + fieldIntensity * 3);
        
        ctx.strokeStyle = magnetColor;
        ctx.globalAlpha = 0.4 * fieldIntensity;
        ctx.lineWidth = 1;
        
        for (let i = 0; i < fieldCount; i++) {
            const angle = (Math.PI * i) / fieldCount;
            const fieldX = magnetWidth * 1.2 * Math.cos(angle);
            const fieldY = fieldSize * 1.5 * Math.sin(angle) - magnetHeight * 0.6;
            
            ctx.beginPath();
            ctx.moveTo(fieldX, fieldY);
            ctx.lineTo(fieldX * 1.3, fieldY * 1.3);
            ctx.stroke();
            
            // Spiegelung für die andere Seite des Magneten
            ctx.beginPath();
            ctx.moveTo(-fieldX, fieldY);
            ctx.lineTo(-fieldX * 1.3, fieldY * 1.3);
            ctx.stroke();
        }
        
        ctx.globalAlpha = 1.0;
    }
    
    // Zeichne den Hufeisenmagneten
    ctx.lineWidth = 2;
    ctx.strokeStyle = "#2c3e50";
    
    // Linker Arm des Hufeisenmagneten
    ctx.beginPath();
    ctx.moveTo(-magnetWidth/2, -magnetHeight/2);
    ctx.lineTo(-magnetWidth/2, magnetHeight/2);
    ctx.lineTo(-magnetWidth/2 + magnetWidth/3, magnetHeight/2);
    ctx.lineTo(-magnetWidth/2 + magnetWidth/3, -magnetHeight/3);
    ctx.closePath();
    ctx.fillStyle = magnetGradient;
    ctx.fill();
    ctx.stroke();
    
    // Rechter Arm des Hufeisenmagneten
    ctx.beginPath();
    ctx.moveTo(magnetWidth/2, -magnetHeight/2);
    ctx.lineTo(magnetWidth/2, magnetHeight/2);
    ctx.lineTo(magnetWidth/2 - magnetWidth/3, magnetHeight/2);
    ctx.lineTo(magnetWidth/2 - magnetWidth/3, -magnetHeight/3);
    ctx.closePath();
    ctx.fillStyle = magnetGradient;
    ctx.fill();
    ctx.stroke();
    
    // Verbindungsstück oben
    ctx.beginPath();
    ctx.moveTo(-magnetWidth/2, -magnetHeight/2);
    ctx.lineTo(magnetWidth/2, -magnetHeight/2);
    ctx.lineWidth = 6;
    ctx.strokeStyle = "#c0392b";
    ctx.stroke();
    
    // Nord/Süd-Markierungen
    ctx.font = "bold 10px Arial";
    ctx.textAlign = "center";
    ctx.fillStyle = "#fff";
    
    // Polanzeige nur bei nicht zu kleinen Magneten
    if (magnetWidth >= 10) {
        ctx.fillText("N", -magnetWidth/2 + magnetWidth/5, magnetHeight/3);
        ctx.fillText("S", magnetWidth/2 - magnetWidth/5, magnetHeight/3);
    }
    
    // Layer-Anzeige bei nicht zu schneller Bewegung
    if (gameState.character.speed < 2) {
        ctx.font = "10px Arial";
        ctx.fillStyle = "#333";
        ctx.textAlign = "center";
        
        // Layer-Anzeige mit Gangart (für AdvancedWalkingLayer)
        let displayText = gameState.activeLayer.replace("Layer", "");
        if (gameState.activeLayer === "AdvancedWalkingLayer") {
            displayText = "Adv";
        } else if (gameState.activeLayer === "BasicWalkingLayer") {
            displayText = "Basic";
        }
        
        ctx.fillText(displayText, 0, -magnetHeight - 5);
    }

    ctx.restore();
    
    // Zeichne Magnetwirkung zum Ziel, wenn Bewegung aktiv
    if (gameState.pointer.active && gameState.character.speed > 0.1) {
        drawMagneticAttraction(x, y, gameState.pointer.x, gameState.pointer.y);
    }
}

// Zeichnet die magnetische Anziehung zwischen Charakter und Zielpunkt
export function drawMagneticAttraction(sourceX, sourceY, targetX, targetY) {
    if (!ctx) return;
    
    // Berechne Distanz und Richtung
    const dx = targetX - sourceX;
    const dy = targetY - sourceY;
    const distance = Math.sqrt(dx * dx + dy * dy);
    
    // Wenn zu nah, keine Anziehung zeichnen
    if (distance < 20) return;
    
    // Anzahl der Linien basierend auf Distanz
    const lineCount = Math.min(Math.max(2, Math.floor(distance / 40)), 5);
    
    ctx.save();
    
    // Normalisierte Richtungsvektoren
    const dirX = dx / distance;
    const dirY = dy / distance;
    
    // Senkrechte Vektoren für Verschiebung
    const perpX = -dirY;
    const perpY = dirX;
    
    // Zeichne magnetische Feldlinien
    ctx.strokeStyle = 'rgba(65, 105, 225, 0.3)';
    ctx.lineWidth = 1.5;
    
    for (let i = 0; i < lineCount; i++) {
        // Berechne Versatz
        const offset = ((i - (lineCount-1)/2) * 7);
        
        // Berechne Kurvenpunkte
        const startOffsetX = perpX * offset;
        const startOffsetY = perpY * offset;
        
        // Berechne Startpunkt (vom Magneten)
        const startX = sourceX + startOffsetX;
        const startY = sourceY + startOffsetY;
        
        // Berechne Endpunkt (zum Ziel)
        const endX = targetX + startOffsetX * 0.5; // Verringere den Versatz am Ziel
        const endY = targetY + startOffsetY * 0.5;
        
        // Berechne Kontrollpunkte für die Kurve
        const cp1x = sourceX + dx * 0.3 + perpX * offset * 1.2;
        const cp1y = sourceY + dy * 0.3 + perpY * offset * 1.2;
        const cp2x = sourceX + dx * 0.7 + perpX * offset * 0.7; // Verringere den Versatz zum Ziel hin
        const cp2y = sourceY + dy * 0.7 + perpY * offset * 0.7;
        
        // Zeichne die Kurve
        ctx.beginPath();
        ctx.moveTo(startX, startY);
        ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, endX, endY);
        ctx.stroke();
    }
    
    ctx.restore();
}
