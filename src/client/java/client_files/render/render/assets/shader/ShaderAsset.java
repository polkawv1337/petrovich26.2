package client_files.render.render.assets.shader;

import client_files.render.render.assets.AssetLocation;

import java.util.Objects;

public final class ShaderAsset {
    private final AssetLocation id;
    private final String source;
    private final ShaderSourceType type;

    public ShaderAsset(AssetLocation id, String source, ShaderSourceType type) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.source = Objects.requireNonNull(source, "source cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
    }

    public AssetLocation getId() { return id; }
    public String getSource() { return source; }
    public ShaderSourceType getType() { return type; }
}
