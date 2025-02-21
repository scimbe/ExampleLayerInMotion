package com.example.motion.sys.model;

import java.util.UUID;

/**
 * Repräsentiert Kollisionsdaten für einen Charakter.
 */
public class CollisionData {
    private final UUID characterId;
    private final Position collisionPoint;
    private final Vector3D normal;
    private final float penetrationDepth;

    public CollisionData(UUID characterId, Position collisionPoint, Vector3D normal, float penetrationDepth) {
        this.characterId = characterId;
        this.collisionPoint = collisionPoint;
        this.normal = normal;
        this.penetrationDepth = penetrationDepth;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public Position getCollisionPoint() {
        return collisionPoint;
    }

    public Vector3D getNormal() {
        return normal;
    }

    public float getPenetrationDepth() {
        return penetrationDepth;
    }

    public MotionState toMotionState() {
        // Konvertiere Kollisionsdaten in einen validen Bewegungszustand
        return new MotionState(
            characterId,
            collisionPoint,
            new Rotation(0, 0, 0),
            0.0f
        );
    }

    @Override
    public String toString() {
        return String.format("CollisionData(characterId=%s, point=%s, normal=%s, depth=%.2f)",
            characterId, collisionPoint, normal, penetrationDepth);
    }
}
