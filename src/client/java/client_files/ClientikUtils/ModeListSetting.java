package client_files.ClientikUtils;

import java.util.ArrayList;
import java.util.List;

public class ModeListSetting extends Setting {

    private final List<BooleanSetting> children = new ArrayList<>();
    private final List<String> enabled = new ArrayList<>();

    public ModeListSetting(String name, BooleanSetting... children) {
        super(name);
        if (children != null) {
            for (BooleanSetting child : children) {
                this.children.add(child);
                if (child.getValue()) {
                    enabled.add(child.getName());
                }
            }
        }
    }

    public List<BooleanSetting> getSettings() {
        return children;
    }

    public boolean isEnabled(String childName) {
        return enabled.contains(childName);
    }

    public void toggle(String childName) {
        if (enabled.contains(childName)) {
            enabled.remove(childName);
        } else {
            enabled.add(childName);
        }
    }

    public void set(String childName, boolean value) {
        if (value) {
            if (!enabled.contains(childName)) {
                enabled.add(childName);
            }
        } else {
            enabled.remove(childName);
        }
    }

    public List<String> getEnabledNames() {
        return enabled;
    }

    public void setEnabledNames(List<String> names) {
        enabled.clear();
        if (names != null) {
            enabled.addAll(names);
        }
    }
}