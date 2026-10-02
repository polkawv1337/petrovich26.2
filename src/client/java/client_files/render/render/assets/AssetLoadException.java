package client_files.render.render.assets;

public class AssetLoadException extends RuntimeException {
    private final AssetLocation location;

    public AssetLoadException(AssetLocation location, String message) {
        super(String.format("Failed to load asset '%s': %s", location, message));
        this.location = location;
    }

    public AssetLoadException(AssetLocation location, String message, Throwable cause) {
        super(String.format("Failed to load asset '%s': %s", location, message), cause);
        this.location = location;
    }

    public AssetLocation getLocation() {
        return location;
    }
}
