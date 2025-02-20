# Motion System REST API

## Character-API

### Character erstellen
```http
POST /api/v1/characters
```

Request Body:
```json
{
  "x": 0.0,
  "y": 0.0,
  "z": 0.0
}
```

Response:
```json
{
  "characterId": "123e4567-e89b-12d3-a456-426614174000",
  "x": 0.0,
  "y": 0.0,
  "z": 0.0,
  "speed": 0.0,
  "rotationX": 0.0,
  "rotationY": 0.0,
  "rotationZ": 0.0
}
```

### Character Status abrufen
```http
GET /api/v1/characters/{characterId}
```

Response:
```json
{
  "characterId": "123e4567-e89b-12d3-a456-426614174000",
  "x": 10.0,
  "y": 0.0,
  "z": 5.0,
  "speed": 1.5,
  "rotationX": 0.0,
  "rotationY": 45.0,
  "rotationZ": 0.0
}
```

### Character bewegen
```http
POST /api/v1/characters/{characterId}/move
```

Request Body:
```json
{
  "directionX": 1.0,
  "directionY": 0.0,
  "directionZ": 0.0,
  "speed": 1.5
}
```

Response: Gleiche Struktur wie beim Character Status

### Animation abspielen
```http
POST /api/v1/characters/{characterId}/animate
```

Request Body:
```json
{
  "animationId": "walk_cycle",
  "speed": 1.0
}
```

Response: Gleiche Struktur wie beim Character Status

### Bewegung stoppen
```http
POST /api/v1/characters/{characterId}/stop
```

Response: Gleiche Struktur wie beim Character Status

## Layer-API

### Aktive Layer abrufen
```http
GET /api/v1/layers
```

Response:
```json
[
  {
    "name": "BasicWalkingLayer",
    "className": "com.example.motion.layers.BasicWalkingLayer"
  },
  {
    "name": "RunningLayer",
    "className": "com.example.motion.layers.RunningLayer"
  }
]
```

### Layer hinzufügen
```http
POST /api/v1/layers
```

Request Body:
```json
{
  "className": "com.example.motion.layers.BasicWalkingLayer",
  "priority": 1
}
```

Response:
```json
{
  "name": "BasicWalkingLayer",
  "className": "com.example.motion.layers.BasicWalkingLayer"
}
```

### Layer entfernen
```http
DELETE /api/v1/layers/{className}
```

Response: 204 No Content bei Erfolg

### Layer-Priorität ändern
```http
PUT /api/v1/layers/{className}/priority
```

Request Body:
```json
{
  "priority": 2
}
```

Response:
```json
{
  "name": "BasicWalkingLayer",
  "className": "com.example.motion.layers.BasicWalkingLayer"
}
```

## Fehlerbehandlung

Alle Endpoints geben bei Fehlern einen entsprechenden HTTP-Statuscode zurück:

```json
{
  "error": "Error processing request",
  "message": "Detaillierte Fehlerbeschreibung"
}
```

### HTTP Status Codes

- 200: Erfolgreiche Operation
- 201: Ressource erfolgreich erstellt
- 204: Erfolgreiche Operation ohne Rückgabewert
- 400: Ungültige Anfrage
- 404: Ressource nicht gefunden
- 500: Interner Server-Fehler

## Beispiele

### Vollständiger Bewegungsablauf

1. Character erstellen:
```bash
curl -X POST http://localhost:8080/api/v1/characters \
  -H "Content-Type: application/json" \
  -d '{"x": 0.0, "y": 0.0, "z": 0.0}'
```

2. Walking Layer aktivieren:
```bash
curl -X POST http://localhost:8080/api/v1/layers \
  -H "Content-Type: application/json" \
  -d '{"className": "com.example.motion.layers.BasicWalkingLayer", "priority": 1}'
```

3. Bewegung starten:
```bash
curl -X POST http://localhost:8080/api/v1/characters/{characterId}/move \
  -H "Content-Type: application/json" \
  -d '{"directionX": 1.0, "directionY": 0.0, "directionZ": 0.0, "speed": 1.5}'
```

4. Animation abspielen:
```bash
curl -X POST http://localhost:8080/api/v1/characters/{characterId}/animate \
  -H "Content-Type: application/json" \
  -d '{"animationId": "walk_cycle", "speed": 1.0}'
```

## Websocket-Events

Zusätzlich zu den REST-Endpunkten unterstützt das System Echtzeit-Updates über WebSocket:

```javascript
const socket = new WebSocket('ws://localhost:8080/motion-updates');

socket.onmessage = (event) => {
    const update = JSON.parse(event.data);
    console.log('Character Update:', update);
};
```

### Event Typen

1. Position Update
```json
{
  "type": "POSITION_UPDATE",
  "characterId": "123e4567-e89b-12d3-a456-426614174000",
  "position": {
    "x": 10.0,
    "y": 0.0,
    "z": 5.0
  }
}
```

2. Animation Update
```json
{
  "type": "ANIMATION_UPDATE",
  "characterId": "123e4567-e89b-12d3-a456-426614174000",
  "animationId": "walk_cycle",
  "progress": 0.75
}
```

3. Layer Update
```json
{
  "type": "LAYER_UPDATE",
  "characterId": "123e4567-e89b-12d3-a456-426614174000",
  "activeLayer": "BasicWalkingLayer"
}
```

## Rate Limiting

Die API implementiert Rate Limiting um Überlastung zu vermeiden:

- 100 Requests pro Minute pro IP-Adresse
- 1000 Requests pro Stunde pro API-Key
- WebSocket: 10 Updates pro Sekunde pro Character

Bei Überschreitung wird der HTTP Status Code 429 (Too Many Requests) zurückgegeben.

## Versionierung

Die API ist versioniert über den Pfad-Prefix `/api/v1/`. Zukünftige Versionen werden über neue Pfade bereitgestellt, während alte Versionen weiterhin unterstützt werden.