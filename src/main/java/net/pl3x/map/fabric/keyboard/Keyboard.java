package net.pl3x.map.fabric.keyboard;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.gui.screen.OptionsScreen;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class Keyboard {
    private final List<Key> globalKeys = new ArrayList<>();
    private final Pl3xMap pl3xmap;

    public Keyboard(Pl3xMap pl3xmap) {
        this.pl3xmap = pl3xmap;
    }

    public void initialize() {
        this.globalKeys.addAll(List.of(
                new Key("pl3xmap.key.options.open", "pl3xmap.key.category", GLFW.GLFW_KEY_M, () -> MinecraftClient.getInstance().setScreen(new OptionsScreen(this.pl3xmap, null))),

                new Key("pl3xmap.key.minimap.zoom.increase", "pl3xmap.key.category", GLFW.GLFW_KEY_PAGE_UP, () -> this.pl3xmap.getMiniMap().addZoomLevel(1)),
                new Key("pl3xmap.key.minimap.zoom.decrease", "pl3xmap.key.category", GLFW.GLFW_KEY_PAGE_DOWN, () -> this.pl3xmap.getMiniMap().addZoomLevel(-1))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> this.globalKeys.forEach(Key::check));
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
