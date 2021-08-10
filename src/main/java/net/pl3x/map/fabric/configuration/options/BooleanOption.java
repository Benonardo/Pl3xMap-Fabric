package net.pl3x.map.fabric.configuration.options;

import net.minecraft.text.Text;
import net.pl3x.map.fabric.gui.screen.widget.Button;

public class BooleanOption implements Option<Boolean> {
    private final Text name;
    private final Text tooltip;
    private final Getter getter;
    private final Setter setter;

    public BooleanOption(Text name, Text tooltip, Getter getter, Setter setter) {
        this.name = name;
        this.tooltip = tooltip;
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
    public Boolean getValue() {
        return this.getter.get();
    }

    @Override
    public void setValue(Boolean value) {
        this.setter.set(value);
    }

    @Override
    public Button.PressAction onPress() {
        return (button) -> {
            setValue(!getValue());
            button.updateMessage();
        };
    }

    @FunctionalInterface
    public interface Getter {
        boolean get();
    }

    @FunctionalInterface
    public interface Setter {
        void set(boolean value);
    }
}
