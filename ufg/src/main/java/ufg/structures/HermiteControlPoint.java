package ufg.structures;

import org.joml.Vector3f;

public class HermiteControlPoint {
    public Vector3f position;
    public Vector3f length;

    public HermiteControlPoint(Vector3f position) {
        this.position = position;
        this.length = new Vector3f(20.0f, 0.0f, 0.0f);
    }

    public HermiteControlPoint(Vector3f position, Vector3f tangent) {
        this.position = position;
        this.length = tangent;
    }
}
