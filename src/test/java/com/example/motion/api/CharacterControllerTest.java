package com.example.motion.api;

import com.example.motion.interfaces.ICharacterMotionService;
import com.example.motion.sys.model.*;
import com.example.motion.api.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CharacterControllerTest {

    @Mock
    private ICharacterMotionService motionService;

    private CharacterController controller;
    private UUID testCharacterId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CharacterController(motionService);
        testCharacterId = UUID.randomUUID();
    }

    @Test
    void createCharacter_ShouldReturnNewCharacter() {
        // Arrange
        CharacterRequest request = new CharacterRequest();
        request.setX(1.0f);
        request.setY(2.0f);
        request.setZ(3.0f);

        // Act
        ResponseEntity<CharacterResponse> response = controller.createCharacter(request);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(1.0f, response.getBody().getX());
        assertEquals(2.0f, response.getBody().getY());
        assertEquals(3.0f, response.getBody().getZ());
    }

    @Test
    void getCharacter_ShouldReturnCharacterState() {
        // Arrange
        MotionState state = new MotionState(
            testCharacterId,
            new Position(1, 2, 3),
            new Rotation(0, 45, 0),
            1.5f
        );
        when(motionService.getMotionState(testCharacterId)).thenReturn(state);

        // Act
        ResponseEntity<CharacterResponse> response = controller.getCharacter(testCharacterId);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(testCharacterId, response.getBody().getCharacterId());
        assertEquals(1.0f, response.getBody().getX());
        assertEquals(2.0f, response.getBody().getY());
        assertEquals(3.0f, response.getBody().getZ());
        assertEquals(1.5f, response.getBody().getSpeed());
    }

    @Test
    void moveCharacter_ShouldUpdateCharacterState() {
        // Arrange
        MovementRequest request = new MovementRequest();
        request.setDirectionX(1.0f);
        request.setDirectionY(0.0f);
        request.setDirectionZ(0.0f);
        request.setSpeed(1.5f);

        MotionState newState = new MotionState(
            testCharacterId,
            new Position(1, 0, 0),
            new Rotation(0, 0, 0),
            1.5f
        );

        when(motionService.setMovementDirection(
            eq(testCharacterId),
            any(Direction.class),
            eq(1.5f)
        )).thenReturn(CompletableFuture.completedFuture(newState));

        // Act
        CompletableFuture<ResponseEntity<CharacterResponse>> future = 
            controller.moveCharacter(testCharacterId, request);
        ResponseEntity<CharacterResponse> response = future.join();

        // Assert
        assertNotNull(response.getBody());
        assertEquals(1.0f, response.getBody().getX());
        assertEquals(1.5f, response.getBody().getSpeed());
    }

    @Test
    void playAnimation_ShouldStartAnimation() {
        // Arrange
        AnimationRequest request = new AnimationRequest();
        request.setAnimationId("walk_cycle");
        request.setSpeed(1.0f);

        MotionState animatedState = new MotionState(
            testCharacterId,
            new Position(0, 0, 0),
            new Rotation(0, 0, 0),
            1.0f
        );

        when(motionService.playAnimation(
            testCharacterId,
            "walk_cycle",
            1.0f
        )).thenReturn(CompletableFuture.completedFuture(animatedState));

        // Act
        CompletableFuture<ResponseEntity<CharacterResponse>> future = 
            controller.playAnimation(testCharacterId, request);
        ResponseEntity<CharacterResponse> response = future.join();

        // Assert
        assertNotNull(response.getBody());
        assertEquals(testCharacterId, response.getBody().getCharacterId());
        assertEquals(1.0f, response.getBody().getSpeed());
    }

    @Test
    void stopCharacter_ShouldStopMovement() {
        // Arrange
        MotionState stoppedState = new MotionState(
            testCharacterId,
            new Position(1, 0, 0),
            new Rotation(0, 0, 0),
            0.0f
        );

        when(motionService.stopMotion(testCharacterId))
            .thenReturn(CompletableFuture.completedFuture(stoppedState));

        // Act
        CompletableFuture<ResponseEntity<CharacterResponse>> future = 
            controller.stopCharacter(testCharacterId);
        ResponseEntity<CharacterResponse> response = future.join();

        // Assert
        assertNotNull(response.getBody());
        assertEquals(0.0f, response.getBody().getSpeed());
    }

    @Test
    void moveCharacter_ShouldHandleCharacterMovement() {
        // Arrange
        float dirX = 1.0f;
        float dirY = 0.0f;
        float dirZ = 0.0f;
        float speed = 1.0f;

        MotionState newState = new MotionState(
            testCharacterId,
            new Position(1, 0, 0),
            new Rotation(0, 0, 0),
            speed
        );

        when(motionService.setMovementDirection(
            eq(testCharacterId),
            any(Direction.class),
            eq(speed)
        )).thenReturn(CompletableFuture.completedFuture(newState));

        // Act
        CompletableFuture<ResponseEntity<CharacterResponse>> future = 
            controller.moveCharacter(testCharacterId, new MovementRequest(dirX, dirY, dirZ, speed));
        ResponseEntity<CharacterResponse> response = future.join();

        // Assert
        assertNotNull(response.getBody());
        assertEquals(1.0f, response.getBody().getX());
        assertEquals(0.0f, response.getBody().getY());
        assertEquals(0.0f, response.getBody().getZ());
        assertEquals(speed, response.getBody().getSpeed());
    }
}
