package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.Module;

public class ClickGui extends Module {

    public ClickGui() {
        super("ClickGui", "Графическое меню модулей (Правый Shift)", Category.RENDER);
        setKey(org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}