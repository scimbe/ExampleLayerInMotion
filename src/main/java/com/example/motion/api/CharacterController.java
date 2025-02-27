package com.example.motion.api;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.model.MotionState;
import com.example.motion.sys.model.Position;
import com.example.motion.sys.model.Rotation;
import com.example.motion.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.example.motion.sys.model.Direction;
import com.example.motion.sys.model.Vector3D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/characters")
@Tag(name = "Character Controller", description = "API zur Steuerung von Charakteren")
public class CharacterController {

    private final ICharacterMotionService motionService;
    private static final Logger logger = LoggerFactory.getLogger(CharacterController.class);

    public CharacterController(ICharacterMotionService motionService) {
        this.motionService = motionService;
    }

    @Operation(summary = "Erstellt einen neuen Charakter", 
              description = "Erstellt einen neuen Charakter an der angegebenen Position")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Charakter erfolgreich erstellt",
                    content = @Content(schema = @Schema(implementation = CharacterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Ungültige Eingabeparameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CharacterResponse> createCharacter(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Position des neuen Charakters")
            @RequestBody CharacterRequest request) {
        UUID characterId = UUID.randomUUID();
        MotionState initialState = new MotionState(
            characterId,
            new Position(request.getX(), request.getY(), request.getZ()),
            new Rotation(0, 0, 0),
            0.0f
        );
        
        logger.info("Character created with ID: {} at position ({}, {}, {})", characterId, request.getX(), request.getY(), request.getZ());
        
        return ResponseEntity.ok(new CharacterResponse(characterId, initialState));
    }

    @Operation(summary = "Ruft den Status eines Charakters ab",
              description = "Liefert den aktuellen Bewegungszustand eines Charakters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status erfolgreich abgerufen"),
        @ApiResponse(responseCode = "404", description = "Charakter nicht gefunden")
    })
    @GetMapping("/{characterId}")
    public ResponseEntity<CharacterResponse> getCharacter(
            @Parameter(description = "ID des Charakters")
            @PathVariable UUID characterId) {
        MotionState state = motionService.getMotionState(characterId);
        return ResponseEntity.ok(new CharacterResponse(characterId, state));
    }

    @Operation(summary = "Bewegt einen Charakter",
              description = "Setzt die Bewegungsrichtung und Geschwindigkeit eines Charakters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bewegung erfolgreich gestartet"),
        @ApiResponse(responseCode = "404", description = "Charakter nicht gefunden"),
        @ApiResponse(responseCode = "400", description = "Ungültige Bewegungsparameter")
    })
    @PostMapping("/{characterId}/move")
    public CompletableFuture<ResponseEntity<CharacterResponse>> moveCharacter(
            @Parameter(description = "ID des Charakters")
            @PathVariable UUID characterId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Bewegungsparameter")
            @RequestBody MovementRequest request) {
        
        Direction direction = new Direction(
            new Vector3D(request.getDirectionX(), request.getDirectionY(), request.getDirectionZ())
        );

        return motionService.setMovementDirection(characterId, direction, request.getSpeed())
            .thenApply(state -> ResponseEntity.ok(new CharacterResponse(characterId, state)));
    }

    @Operation(summary = "Spielt eine Animation ab",
              description = "Startet eine Animation für einen Charakter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Animation erfolgreich gestartet"),
        @ApiResponse(responseCode = "404", description = "Charakter oder Animation nicht gefunden")
    })
    @PostMapping("/{characterId}/animate")
    public CompletableFuture<ResponseEntity<CharacterResponse>> playAnimation(
            @Parameter(description = "ID des Charakters")
            @PathVariable UUID characterId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Animationsparameter")
            @RequestBody AnimationRequest request) {
        
        return motionService.playAnimation(characterId, request.getAnimationId(), request.getSpeed())
            .thenApply(state -> ResponseEntity.ok(new CharacterResponse(characterId, state)));
    }

    @Operation(summary = "Stoppt alle Bewegungen",
              description = "Stoppt alle aktiven Bewegungen und Animationen eines Charakters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bewegungen erfolgreich gestoppt"),
        @ApiResponse(responseCode = "404", description = "Charakter nicht gefunden")
    })
    @PostMapping("/{characterId}/stop")
    public CompletableFuture<ResponseEntity<CharacterResponse>> stopCharacter(
            @Parameter(description = "ID des Charakters")
            @PathVariable UUID characterId) {
        
        return motionService.stopMotion(characterId)
            .thenApply(state -> ResponseEntity.ok(new CharacterResponse(characterId, state)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("Error processing request", e.getMessage()));
    }
}
