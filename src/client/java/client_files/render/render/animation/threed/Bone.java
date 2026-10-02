package client_files.render.render.animation.threed;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bone {
    private final String name;
    private final Vector3f localPosition = new Vector3f();
    private final Quaternionf localRotation = new Quaternionf();
    private final Vector3f localScale = new Vector3f(1f, 1f, 1f);

    private Bone parent;
    private final List<Bone> children = new ArrayList<>();

    public Bone(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public Vector3f getLocalPosition() { return localPosition; }
    public Quaternionf getLocalRotation() { return localRotation; }
    public Vector3f getLocalScale() { return localScale; }

    public Bone getParent() { return parent; }
    public void setParent(Bone parent) { this.parent = parent; }

    public List<Bone> getChildren() { return Collections.unmodifiableList(children); }

    public void addChild(Bone child) {
        if (child != null) {
            child.setParent(this);
            children.add(child);
        }
    }

    public Matrix4f worldMatrix() {
        Matrix4f local = new Matrix4f()
                .translation(localPosition)
                .rotate(localRotation)
                .scale(localScale);

        if (parent != null) {
            return new Matrix4f(parent.worldMatrix()).mul(local);
        }
        return local;
    }
}
