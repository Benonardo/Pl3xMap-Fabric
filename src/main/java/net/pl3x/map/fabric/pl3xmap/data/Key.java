package net.pl3x.map.fabric.pl3xmap.data;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class Key {
    private final KeyBinding binding;
    private final Press press;

    public Key(String name, String category, int key, Press press) {
        this.binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, key, category));
        this.press = press;
    }

    public void check() {
        if (this.binding.wasPressed()) {
            this.press.execute();
        }
    }

    @FunctionalInterface
    public interface Press {
        void execute();
    }
}
