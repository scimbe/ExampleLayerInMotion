package com.example.motion.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Repräsentiert die Daten einer Animation mit Keyframes und Metadaten.
 */
public class AnimationData {
    private final String id;
    private final String name;
    private final float duration;
    private final List<KeyFrame> keyFrames;
    private final AnimationType type;
    private final boolean looping;

    public AnimationData(String id, 
                        String name, 
                        float duration, 
                        List<KeyFrame> keyFrames, 
                        AnimationType type, 
                        boolean looping) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.keyFrames = new ArrayList<>(keyFrames);
        this.type = type;
        this.looping = looping;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public float getDuration() { return duration; }
    public List<KeyFrame> getKeyFrames() { return new ArrayList<>(keyFrames); }
    public AnimationType getType() { return type; }
    public boolean isLooping() { return looping; }

    /**
     * Berechnet den interpolierten Zustand zu einem bestimmten Zeitpunkt.
     */
    public MotionState interpolateAtTime(float time, MotionState currentState) {
        if (keyFrames.isEmpty()) {
            return currentState;
        }

        // Handle looping animations
        if (looping) {
            time = time % duration;
        }

        // Find surrounding keyframes
        KeyFrame prevFrame = keyFrames.get(0);
        KeyFrame nextFrame = keyFrames.get(keyFrames.size() - 1);

        for (int i = 0; i < keyFrames.size() - 1; i++) {
            if (keyFrames.get(i).getTime() <= time && keyFrames.get(i + 1).getTime() > time) {
                prevFrame = keyFrames.get(i);
                nextFrame = keyFrames.get(i + 1);
                break;
            }
        }

        // Calculate interpolation factor
        float frameDuration = nextFrame.getTime() - prevFrame.getTime();
        float factor = frameDuration > 0 ? 
            (time - prevFrame.getTime()) / frameDuration : 0;

        // Interpolate between frames
        return new MotionState(
            currentState.getCharacterId(),
            interpolatePosition(prevFrame.getPosition(), nextFrame.getPosition(), factor),
            interpolateRotation(prevFrame.getRotation(), nextFrame.getRotation(), factor),
            currentState.getSpeed()
        );
    }

    private Position interpolatePosition(Position start, Position end, float factor) {
        return new Position(
            start.getX() + (end.getX() - start.getX()) * factor,
            start.getY() + (end.getY() - start.getY()) * factor,
            start.getZ() + (end.getZ() - start.getZ()) * factor
        );
    }

    private Rotation interpolateRotation(Rotation start, Rotation end, float factor) {
        return new Rotation(
            start.getPitch() + (end.getPitch() - start.getPitch()) * factor,
            start.getYaw() + (end.getYaw() - start.getYaw()) * factor,
            start.getRoll() + (end.getRoll() - start.getRoll()) * factor
        );
    }

    /**
     * Repräsentiert einen einzelnen Keyframe in der Animation.
     */
    public static class KeyFrame {
        private final float time;
        private final Position position;
        private final Rotation rotation;

        public KeyFrame(float time, Position position, Rotation rotation) {
            this.time = time;
            this.position = position;
            this.rotation = rotation;
        }

        public float getTime() { return time; }
        public Position getPosition() { return position; }
        public Rotation getRotation() { return rotation; }
    }

    /**
     * Definiert die verschiedenen Animationstypen.
     */
    public enum AnimationType {
        IDLE,
        WALK,
        RUN,
        JUMP,
        ATTACK,
        CUSTOM
    }

    @Override
    public String toString() {
        return String.format("Animation(%s, %s, duration=%.2f, frames=%d, type=%s, looping=%b)",
            id, name, duration, keyFrames.size(), type, looping);
    }
}