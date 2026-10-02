package client_files.Petrovich.Movement;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import client_files.ModuleManager;

public class Timer extends Module {

    private final SliderSetting speed = addSetting(new SliderSetting("Множитель", 1.5f, 0.1f, 5.0f, 0.05f));

    public Timer() {
        super("Timer", "Ускоряет темп игры (множитель тиков)", Category.MOVEMENT);
    }

    public static float getMultiplier() {
        Timer timer = ModuleManager.getInstance().get(Timer.class);
        if (timer != null && timer.isEnabled()) {
            return timer.speed.getValue();
        }
        return 1.0f;
    }
}