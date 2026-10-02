package client_files.ClientikUtils;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    private final String[] modes;
    private int index;

    public ModeSetting(String name, String... modes) {
        super(name);
        this.modes = modes;
        this.index = 0;
    }

    public String getValue() {
        return modes[index];
    }

    public void setValue(String value) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(value)) {
                index = i;
                return;
            }
        }
    }

    public void cycle() {
        index = (index + 1) % modes.length;
    }

    public List<String> getModes() {
        return Arrays.asList(modes);
    }
}
