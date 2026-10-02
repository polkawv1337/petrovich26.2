package client_files.ClientikUtils;

import org.lwjgl.glfw.GLFW;

public class BindSetting extends Setting {
    public static final int MOUSE_BASE = 1000;
    public static final int SCROLL_UP = 2000;
    public static final int SCROLL_DOWN = 2001;

    private int value;

    public BindSetting(String name, int defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getKeyName() {
        if (value <= 0) {
            return "NONE";
        }
        if (value == SCROLL_UP) {
            return "WHEEL_UP";
        }
        if (value == SCROLL_DOWN) {
            return "WHEEL_DOWN";
        }
        if (value >= MOUSE_BASE && value < MOUSE_BASE + 32) {
            return mouseButtonName(value - MOUSE_BASE);
        }
        if (value >= 290 && value <= 301) {
            return "F" + (value - 289);
        }
        String name = GLFW.glfwGetKeyName(value, 0);
        if (name != null && !name.isEmpty()) {
            return name.toUpperCase();
        }
        return "B" + value;
    }

    private static String mouseButtonName(int button) {
        return switch (button) {
            case 0 -> "LMB";
            case 1 -> "RMB";
            case 2 -> "MMB";
            default -> "M" + (button + 1);
        };
    }
}