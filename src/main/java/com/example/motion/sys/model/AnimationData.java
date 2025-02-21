package com.example.motion.sys.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert Animation-Daten für Charakterbewegungen.
 */
public class AnimationData {
    private final String id;
    private final float duration;
    private final boolean looping;
    private final List<AnimationKeyframe> keyframes;

    public AnimationData(String id, float duration, boolean looping) {
        this.id = id;
        this.duration = duration;
        this.looping = looping;
        this.keyframes = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public float getDuration() {
        return duration;
    }

    public boolean isLooping() {
        return looping;
    }

    public void addKeyframe(AnimationKeyframe keyframe) {
        keyframes.add(keyframe);
    }

    public MotionState interpolateAtTime(float time, MotionState baseState) {
        if (keyframes.size() < 2) {
            return baseState;
        }

        // Für Looping-Animationen, normalisiere die Zeit
        if (looping && time > duration) {
            time = time % duration;
        }

        // Finde die zwei Keyframes für die aktuelle Zeit
        AnimationKeyframe start = keyframes.get(0);
        AnimationKeyframe end = keyframes.get(1);
        
        for (int i = 1; i < keyframes.size(); i++) {
            if (keyframes.get(i).getTime() > time) {
                start = keyframes.get(i - 1);
                end = keyframes.get(i);
                break;
            }
        }

        // Berechne Interpolationsfaktor
        float factor = (time - start.getTime()) / (end.getTime() - start.getTime());
        factor = Math.max(0, Math.min(1, factor));

        // Interpoliere Position
        Position startPos = start.getPosition();
        Position endPos = end.getPosition();
        Position newPos = new Position(
            startPos.getX() + (endPos.getX() - startPos.getX()) * factor,
            startPos.getY() + (endPos.getY() - startPos.getY()) * factor,
            startPos.getZ() + (endPos.getZ() - startPos.getZ()) * factor
        );

        // Interpoliere Rotation
        Rotation startRot = start.getRotation();
        Rotation endRot = end.getRotation();
        Rotation newRot = new Rotation(
            startRot.getPitch() + (endRot.getPitch() - startRot.getPitch()) * factor,
            startRot.getYaw() + (endRot.getYaw() - startRot.getYaw()) * factor,
            startRot.getRoll() + (endRot.getRoll() - startRot.getRoll()) * factor
        );

        // Erstelle neuen Bewegungszustand
        return new MotionState(
            baseState.getCharacterId(),
            newPos,
            newRot,
            baseState.getSpeed()
        );
    }

    /**
     * Repräsentiert einen Keyframe in der Animation.
     */
    public static class AnimationKeyframe {
        private final float time;
        private final Position position;
        private final Rotation rotation;

        public AnimationKeyframe(float time, Position position, Rotation rotation) {
            this.time = time;
            this.position = position;
            this.rotation = rotation;
        }

        public float getTime() {
            return time;
        }

        public Position getPosition() {
            return position;
        }

        public Rotation getRotation() {
            return rotation;
        }
    }
}
