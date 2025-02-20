package com.example.motion.sys.model;

import lombok.Getter;

@Getter
public class CollisionData {
    private final Position point;
    private final Vector3D normal;
    private final float depth;

    public CollisionData(Position point, Vector3D normal, float depth) {
        this.point = point;
        this.normal = normal;
        this.depth = depth;
    }
}
