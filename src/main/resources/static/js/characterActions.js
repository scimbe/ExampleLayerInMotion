/**
 * Entfernt einen Layer
 */
export async function removeLayer(layerClassName) {
    try {
        if (!gameState.offlineMode) {
            try {
                // Der Server erwartet nur den vollständigen Klassennamen
                const fullClassName = `com.example.motion.sys.behavior.${layerClassName}`;
                
                // Der API-Endpunkt verwendet PathVariable, daher muss der Pfad korrekt kodiert werden
                // Wir verwenden encodeURIComponent, um den Klassennamen korrekt zu kodieren
                const response = await fetch(`${API_BASE_URL}/layers/${encodeURIComponent(fullClassName)}`, {
                    method: "DELETE"
                });

                if (response.ok) {
                    return true;
                } else {
                    console.warn(`Failed to remove layer: ${layerClassName}, status: ${response.status}`);
                    // Obwohl die Anfrage fehlschlägt, wollen wir die App weiterlaufen lassen
                    return true;
                }
            } catch (e) {
                console.warn("API not reachable or error removing layer, switching to offline mode", e);
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