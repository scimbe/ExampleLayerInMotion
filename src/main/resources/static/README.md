# Motion Master Game

Eine interaktive Demonstration des Layer-basierten Charakterbewegungssystems.

## Überblick

Das Motion Master Game ist eine Webanwendung, die das Layer-basierte Charakterbewegungssystem veranschaulicht. Benutzer können:

- Einen Charakter in einer 2D-Umgebung steuern
- Zwischen verschiedenen Bewegungslayern wechseln (Gehen, Laufen, Stehen)
- Animationen abspielen
- Ziele erreichen, um Punkte zu sammeln

Die Anwendung demonstriert die Kernkonzepte des Systems in einer anschaulichen, interaktiven Weise.

## Funktionen

- **Charakter-Steuerung**: Bewegung per Tastatur (WASD/Pfeiltasten) oder Richtungsbuttons
- **Layer-Wechsel**: Umschalten zwischen verschiedenen Bewegungsarten
- **Animationen**: Abspielen von vordefinierten Animationen
- **Ziel-System**: Erreichen von zufällig generierten Zielen für Punkte
- **Status-Anzeige**: Echtzeit-Anzeige von Position, Geschwindigkeit und aktivem Layer
- **Offline-Modus**: Funktioniert auch ohne Backend-Anbindung

## Architektur

Die Webanwendung kommuniziert mit dem Backend über:
- REST API für CRUD-Operationen (Charakter erstellen, Layer wechseln usw.)
- WebSockets für Echtzeit-Updates (Positionsänderungen, Animationen)

Bei nicht verfügbarem Backend wechselt die Anwendung automatisch in einen Offline-Modus mit simulierter Funktionalität.

## Technische Details

### Frontend
- Reines JavaScript (keine Frameworks)
- Canvas API für Rendering
- Fetch API für HTTP-Requests
- WebSocket API für Echtzeit-Kommunikation

### Offline-Funktionalität
Die Anwendung erkennt automatisch, wenn das Backend nicht verfügbar ist, und wechselt in einen Offline-Modus:
- Lokale Bewegungsberechnung
- Simulierte Layer-Wechsel
- Grundlegende Spielfunktionalität bleibt erhalten

## Nutzung

1. Öffne die Anwendung im Browser
2. Der Charakter wird automatisch erstellt und im Spielfeld angezeigt
3. Verwende die Steuerungselemente, um den Charakter zu bewegen
4. Wechsle zwischen verschiedenen Bewegungslayern
5. Erreiche die goldenen Ziele, um Punkte zu sammeln
6. Beobachte die Statusanzeige für aktuelle Positionsdaten

## API-Integration

Die Anwendung nutzt die folgenden API-Endpunkte:

- `POST /api/v1/characters` - Erstellt einen neuen Charakter
- `POST /api/v1/characters/{id}/move` - Bewegt einen Charakter
- `POST /api/v1/characters/{id}/animate` - Spielt eine Animation ab
- `POST /api/v1/characters/{id}/stop` - Stoppt die Bewegung
- `POST /api/v1/layers` - Fügt einen Motion Layer hinzu
- `DELETE /api/v1/layers/{className}` - Entfernt einen Motion Layer

## Erweiterungsmöglichkeiten

- Unterstützung für Mehrspielermodus
- Komplexere Spielmechaniken (Hindernisse, Sammelobjekte)
- Visualisierung der internen Zustände des Layering-Systems
- Integration mit 3D-Rendering-Systemen (Three.js)
