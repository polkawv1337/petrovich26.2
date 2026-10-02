package client_files.render.render.assets.font;

import client_files.render.render.assets.AssetLocation;

import java.util.Objects;

public final class FontAsset {
    private final AssetLocation id;
    private final byte[] rawBytes;
    private final FontAssetType type;
    private final String family;

    public FontAsset(AssetLocation id, byte[] rawBytes, FontAssetType type, String family) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.rawBytes = rawBytes != null ? rawBytes.clone() : new byte[0];
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.family = family != null ? family : "CustomFont";
    }

    public AssetLocation getId() { return id; }
    public byte[] getRawBytes() { return rawBytes.clone(); }
    public FontAssetType getType() { return type; }
    public String getFamily() { return family; }
}
