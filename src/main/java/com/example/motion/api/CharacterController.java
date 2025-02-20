package com.example.motion.api;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.model.*;
import com.example.motion.api.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/characters")
public class CharacterController {

    private final ICharacterMotionService motionService;

    public CharacterController(ICharacterMotionService motionService) {
        this.motionService = motionService;
    }

    @PostMapping
    public ResponseEntity<CharacterResponse> createCharacter(@RequestBody CharacterRequest request) {
        UUID characterId = UUID.randomUUID();
        MotionState initialState = new MotionState(
            characterId,
            new Position(request.getX(), request.getY(), request.getZ()),
            new Rotation(0, 0, 0),
            0.0f
        );
        
        return ResponseEntity.ok(new CharacterResponse(characterId, initialState));
    }

    @GetMapping("/{characterId}")
    public ResponseEntity<CharacterResponse> getCharacter(@PathVariable UUID characterId) {
        MotionState state = motionService.getMotionState(characterId);
        return ResponseEntity.ok(new CharacterResponse(characterId, state));
    }

    @PostMapping("/{characterId}/move")
    public CompletableFuture<ResponseEntity<CharacterResponse>> moveCharacter(
            @PathVariable UUID characterId,
            @RequestBody MovementRequest request) {
        
        Direction direction = new Direction(
            new Vector3D(request.getDirectionX(), request.getDirectionY(), request.getDirectionZ())
        );

        return motionService.setMovementDirection(characterId, direction, request.getSpeed())
            .thenApply(state -> ResponseEntity.ok(new CharacterResponse(characterId, state)));
    }

    @PostMapping("/{characterId}/animate")
    public CompletableFuture<ResponseEntity<CharacterResponse>> playAnimation(
            @PathVariable UUID characterId,
            @RequestBody AnimationRequest request) {
        
        return motionService.playAnimation(characterId, request.getAnimationId(), request.getSpeed())
            .thenApply(state -> ResponseEntity.ok(new CharacterResponse(characterId, state)));
    }

    @PostMapping("/{characterId}/stop")
    public CompletableFuture<ResponseEntity<CharacterResponse>> stopCharacter(
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