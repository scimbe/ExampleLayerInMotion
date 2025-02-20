package com.example.motion.behavior;

import com.example.motion.interfaces.IMotionLayer;
import com.example.motion.model.*;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Erweiterte Implementierung des Walking Layers mit verschiedenen Gangarten.
 */
public class AdvancedWalkingLayer implements IMotionLayer {

    private final Map<UUID, GaitType> characterGaits;

    private final Map<UUID, Float> stepCycles;

    // Gangart-spezifische Parameter
    private static final float NORMAL_STEP_LENGTH = 0.6f;

    private static final float SNEAKING_STEP_LENGTH = 0.3f;

    private static final float LIMPING_STEP_LENGTH = 0.4f;

    private static final float NORMAL_STEP_HEIGHT = 0.1f;

    private static final float SNEAKING_STEP_HEIGHT = 0.05f;

    private static final float LIMPING_STEP_HEIGHT = 0.15f;

    public enum GaitType {
        NORMAL,
        SNEAKING,
        LIMPING
    }

    public AdvancedWalkingLayer() {
        this.characterGaits = new ConcurrentHashMap<>();
        this.stepCycles = new ConcurrentHashMap<>();
    }

    /**
     * Setzt die Gangart für einen Charakter.
     */
    public void setGaitType(UUID characterId, GaitType gaitType) {
        characterGaits.put(characterId, gaitType);
    }

    @Override
    public MotionState processMotion(UUID characterId, MotionState currentState, float deltaTime) {
        if (currentState.getSpeed() <= 0) {
            return currentState;
        }

        // Aktuelle Gangart abrufen oder Standard verwenden
        GaitType gaitType = characterGaits.getOrDefault(characterId, GaitType.NORMAL);

        // Schrittzyklus aktualisieren
        float cycle = stepCycles.getOrDefault(characterId, 0.0f);
        cycle = (cycle + deltaTime * currentState.getSpeed()) % (2 * (float) Math.PI);
        stepCycles.put(characterId, cycle);

        // Parameter für aktuelle Gangart
        float stepLength = getStepLength(gaitType);
        float stepHeight = getStepHeight(gaitType);

        // Basis-Bewegung berechnen
        Position currentPos = currentState.getPosition();
        Rotation rotation = currentState.getRotation();

        float distance = currentState.getSpeed() * stepLength * deltaTime;
        float newX = currentPos.getX()
                + distance * (float) Math.cos(Math.toRadians(rotation.getYaw()));
        float newZ = currentPos.getZ()
                + distance * (float) Math.sin(Math.toRadians(rotation.getYaw()));

        // Vertikale Bewegung basierend auf Gangart
        float verticalOffset = calculateVerticalOffset(cycle, stepHeight, gaitType);
        float newY = currentPos.getY() + verticalOffset;

        // Rotation basierend auf Gangart
        Rotation newRotation = calculateGaitRotation(rotation, cycle, gaitType);

        return new MotionState(
                characterId,
                new Position(newX, newY, newZ),
                newRotation,
                currentState.getSpeed());
    }

    private float getStepLength(GaitType gaitType) {
        switch (gaitType) {
        case SNEAKING:
            return SNEAKING_STEP_LENGTH;
        case LIMPING:
            return LIMPING_STEP_LENGTH;
        default:
            return NORMAL_STEP_LENGTH;
        }
    }

    private float getStepHeight(GaitType gaitType) {
        switch (gaitType) {
        case SNEAKING:
            return SNEAKING_STEP_HEIGHT;
        case LIMPING:
            return LIMPING_STEP_HEIGHT;
        default:
            return NORMAL_STEP_HEIGHT;
        }
    }

    private float calculateVerticalOffset(float cycle, float stepHeight, GaitType gaitType) {
        switch (gaitType) {
        case LIMPING:
            // Asymmetrische Bewegung für hinkende Gangart
            return stepHeight
                    * (float) Math.max(0, Math.sin(cycle) * (1 + 0.5f * Math.sin(cycle / 2)));
        case SNEAKING:
            // Flachere, gleichmäßigere Bewegung
            return stepHeight * (float) Math.abs(Math.sin(cycle));
        default:
            // Normale sinusförmige Auf/Ab-Bewegung
            return stepHeight * (float) Math.sin(cycle);
        }
    }

    private Rotation calculateGaitRotation(Rotation baseRotation, float cycle, GaitType gaitType) {
        float pitchOffset = 0;
        float rollOffset = 0;

        switch (gaitType) {
        case LIMPING:
            // Deutlichere Neigung für hinkende Gangart
            pitchOffset = 3.0f * (float) Math.sin(cycle / 2);
            rollOffset = 4.0f * (float) Math.sin(cycle);
            break;
        case SNEAKING:
            // Minimale Bewegung für schleichende Gangart
            pitchOffset = 1.0f * (float) Math.sin(cycle);
            rollOffset = 1.0f * (float) Math.sin(cycle * 2);
            break;
        default:
            // Normale Gangbewegung
            pitchOffset = 2.0f * (float) Math.sin(cycle);
            rollOffset = 2.0f * (float) Math.sin(cycle * 2);
        }

        return new Rotation(
                baseRotation.getPitch() + pitchOffset,
                baseRotation.getYaw(),
                baseRotation.getRoll() + rollOffset);
    }

    @Override
    public CollisionData checkCollision(UUID characterId, MotionState motionState) {
        // Einfache Kollisionsprüfung
        return null;
    }

    @Override
    public MotionState processPhysics(UUID characterId, PhysicsData physicsData) {
        // Basis-Physik mit Gangart-spezifischen Anpassungen
        GaitType gaitType = characterGaits.getOrDefault(characterId, GaitType.NORMAL);

        float verticalAdjustment = switch (gaitType) {
        case SNEAKING -> 0.5f; // Niedrigerer Schwerpunkt
        case LIMPING -> 0.8f; // Leicht reduzierte Fallgeschwindigkeit
        default -> 1.0f; // Normale Physik
        };

        Position adjustedPosition = new Position(
                physicsData.getPosition().getX(),
                Math.max(0,
                        physicsData.getPosition().getY()
                                - 9.81f * physicsData.getDeltaTime() * verticalAdjustment),
                physicsData.getPosition().getZ());

        return new MotionState(
                characterId,
                adjustedPosition,
                physicsData.getRotation(),
                physicsData.getSpeed());
    }

    @Override
    public boolean validateMotionState(MotionState motionState) {
        // Validierung basierend auf Gangart
        UUID characterId = motionState.getCharacterId();
        GaitType gaitType = characterGaits.getOrDefault(characterId, GaitType.NORMAL);

        float maxSpeed = switch (gaitType) {
        case SNEAKING -> 0.5f;
        case LIMPING -> 0.7f;
        default -> 1.0f;
        };

        return motionState.getSpeed() <= maxSpeed;
    }

    @Override
    public MotionState interpolateStates(MotionState start, MotionState end, float factor) {
        Position interpolatedPos = interpolatePosition(start.getPosition(), end.getPosition(),
                factor);
        Rotation interpolatedRot = interpolateRotation(start.getRotation(), end.getRotation(),
                factor);
        float interpolatedSpeed = start.getSpeed() + (end.getSpeed() - start.getSpeed()) * factor;

        return new MotionState(
                start.getCharacterId(),
                interpolatedPos,
                interpolatedRot,
                interpolatedSpeed);
    }

    private Position interpolatePosition(Position start, Position end, float factor) {
        return new Position(
                start.getX() + (end.getX() - start.getX()) * factor,
                start.getY() + (end.getY() - start.getY()) * factor,
                start.getZ() + (end.getZ() - start.getZ()) * factor);
    }

    private Rotation interpolateRotation(Rotation start, Rotation end, float factor) {
        return new Rotation(
                start.getPitch() + (end.getPitch() - start.getPitch()) * factor,
                start.getYaw() + (end.getYaw() - start.getYaw()) * factor,
                start.getRoll() + (end.getRoll() - start.getRoll()) * factor);
    }
}
