package client_files.ClientikUtils;

public abstract class Setting {
    protected final String name;
    protected boolean visible = true;
    private java.util.function.Supplier<Boolean> visibilitySupplier = null;

    protected Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return visibilitySupplier != null ? visibilitySupplier.get() : visible;
    }

    public Setting setVisible(boolean visible) {
        this.visibilitySupplier = null;
        this.visible = visible;
        return this;
    }

    public Setting setVisible(java.util.function.Supplier<Boolean> visibilitySupplier) {
        this.visibilitySupplier = visibilitySupplier;
        this.visible = true;
        return this;
    }
}
