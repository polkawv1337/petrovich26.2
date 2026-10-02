package client_files.render.render.animation.threed;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Skeleton {
    private final Bone root;
    private final Map<String, Bone> byName = new HashMap<>();

    public Skeleton(Bone root) {
        this.root = Objects.requireNonNull(root, "root bone cannot be null");
        indexBones(root);
    }

    private void indexBones(Bone current) {
        if (current.getName() != null) {
            byName.put(current.getName(), current);
        }
        for (Bone child : current.getChildren()) {
            indexBones(child);
        }
    }

    public Bone getRoot() {
        return root;
    }

    public Bone findBone(String name) {
        return byName.get(name);
    }

    public Map<String, Bone> getBones() {
        return Collections.unmodifiableMap(byName);
    }
}
