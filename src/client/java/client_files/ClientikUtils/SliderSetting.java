package client_files.ClientikUtils;

public class SliderSetting extends Setting {
    private float value;
    private final float min;
    private final float max;
    private final float step;

    public SliderSetting(String name, float value, float min, float max, float step) {
        super(name);
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = Math.round(Math.max(min, Math.min(max, value)) / step) * step;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getStep() {
        return step;
    }
}
