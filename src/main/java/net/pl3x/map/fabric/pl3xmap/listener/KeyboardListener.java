package net.pl3x.map.fabric.pl3xmap.listener;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import net.pl3x.map.fabric.pl3xmap.data.Key;
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
                new Key("key.pl3xmap.enabled", "category.pl3xmap.keybinds", GLFW.GLFW_KEY_N, this.pl3xmap::toggleOnOff)
                //new Key("key.pl3xmap.togglehud", "category.pl3xmap.keybinds", GLFW.GLFW_KEY_M, this.pl3xmap::toggleMiniMap)
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> this.keys.forEach(Key::check));
    }
}
