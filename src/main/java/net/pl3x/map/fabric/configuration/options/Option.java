package net.pl3x.map.fabric.configuration.options;

import net.minecraft.text.Text;
import net.pl3x.map.fabric.gui.screen.widget.Button;

public interface Option<T> {
    T getValue();

    void setValue(T value);

    Button.PressAction onPress();

    Text getName();

    Text tooltip();
}
