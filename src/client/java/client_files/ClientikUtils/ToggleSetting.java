package client_files.ClientikUtils;

import client_files.Module;

public class ToggleSetting extends BooleanSetting {

    private final Module target;

    public ToggleSetting(String name, Module target) {
        super(name, target.isEnabled());
        this.target = target;
    }

    public void sync() {
        setValue(target.isEnabled());
    }

    @Override
    public boolean toggle() {
        target.toggle();
        setValue(target.isEnabled());
        return getValue();
    }
}