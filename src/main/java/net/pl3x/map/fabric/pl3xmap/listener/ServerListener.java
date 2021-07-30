package net.pl3x.map.fabric.pl3xmap.listener;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import net.pl3x.map.fabric.pl3xmap.hud.MiniMap;

public class ServerListener {
    private final Pl3xMap pl3xmap;
    private final MiniMap minimap;

    public ServerListener(Pl3xMap pl3xmap) {
        this.pl3xmap = pl3xmap;
        this.minimap = new MiniMap();
    }

    public void initialize() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (!client.isInSingleplayer()) {
                this.pl3xmap.isOnServer = true;
                this.minimap.initialize();
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (!client.isInSingleplayer()) {
                this.minimap.disable();
                this.pl3xmap.isOnServer = false;
                this.pl3xmap.clearAllData();
                this.pl3xmap.setMapUrl(null);
            }
        });
    }
}
