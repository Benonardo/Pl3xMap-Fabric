package net.pl3x.map.fabric.configuration.options;

import net.minecraft.text.Text;
import net.pl3x.map.fabric.gui.screen.widget.Button;

public class IntegerOption implements Option<Integer> {
    private final Text name;
    private final Text tooltip;
    private final Getter getter;
    private final Setter setter;
    private final int min;
    private final int max;

    public IntegerOption(Text name, Text tooltip, int min, int max, Getter getter, Setter setter) {
        this.name = name;
        this.tooltip = tooltip;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Text getName() {
        return this.name;
    }

    @Override
    public Text tooltip() {
        return this.tooltip;
    }

    @Override
    public Integer getValue() {
        return this.getter.get();
    }

    @Override
    public void setValue(Integer value) {
        this.setter.set(value);
    }

    @Override
    public Button.PressAction onPress() {
        return null;
    }

    public int getMax() {
        return this.max;
    }

    public int getMin() {
        return this.min;
    }

    @FunctionalInterface
    public interface Getter {
        int get();
    }

    @FunctionalInterface
    public interface Setter {
        void set(int value);
    }
}
