package net.pl3x.map.fabric.pl3xmap.listener;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class KeyboardListener {
    private final List<Key> keys = new ArrayList<>();
    private final Pl3xMap pl3xmap;

    public KeyboardListener(Pl3xMap pl3xmap) {
        this.pl3xmap = pl3xmap;
    }

    public void initialize() {
        this.keys.addAll(List.of(
                new Key("key.pl3xmap.togglemod", "category.pl3xmap.keybinds", GLFW.GLFW_KEY_N, this.pl3xmap::toggleOnOff),
                new Key("key.pl3xmap.toggleminimap", "category.pl3xmap.keybinds", GLFW.GLFW_KEY_M, this.pl3xmap::toggleMiniMap)
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> this.keys.forEach(Key::check));
    }

    private static class Key {
        private final KeyBinding binding;
        private final Press press;

        private Key(String name, String category, int key, Press press) {
            this.binding = KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, key, category));
            this.press = press;
        }

        private void check() {
            while (this.binding.wasPressed()) {
                this.press.execute();
            }
        }

        @FunctionalInterface
        private interface Press {
            void execute();
        }
    }
}
