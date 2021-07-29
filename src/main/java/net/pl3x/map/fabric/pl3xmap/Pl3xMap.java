package net.pl3x.map.fabric.pl3xmap;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.MapRenderer;
import net.pl3x.map.fabric.pl3xmap.configuration.Lang;
import net.pl3x.map.fabric.pl3xmap.data.TileManager;
import net.pl3x.map.fabric.pl3xmap.listener.KeyboardListener;
import net.pl3x.map.fabric.pl3xmap.listener.ServerListener;
import net.pl3x.map.fabric.pl3xmap.network.NetworkManager;
import net.pl3x.map.fabric.pl3xmap.util.DownloadManager;

public class Pl3xMap implements ModInitializer {
    private static Pl3xMap instance;

    public static Pl3xMap instance() {
        return instance;
    }

    private final NetworkManager networkManager;
    private final DownloadManager downloadManager;
    private final TileManager tileManager;

    private final KeyboardListener keyboardListener;
    private final ServerListener serverListener;

    public Pl3xMap() {
        instance = this;

        this.networkManager = new NetworkManager(this);
        this.downloadManager = new DownloadManager(this);
        this.tileManager = new TileManager(this);

        this.keyboardListener = new KeyboardListener(this);
        this.serverListener = new ServerListener(this);
    }

    public boolean enabled = true;
    public boolean minimap = true;
    public boolean isOnServer = false;

    @Override
    public void onInitialize() {
        this.networkManager.initialize();

        this.keyboardListener.initialize();
        this.serverListener.initialize();
    }

    public boolean canRenderMap() {
        return this.enabled && this.isOnServer;
    }

    public void updateAllMapTextures() {
        MinecraftClient.getInstance().gameRenderer.getMapRenderer().mapTextures
                .values().forEach(MapRenderer.MapTexture::setNeedsUpdate);
    }

    public void clearAllData() {
        this.downloadManager.clear();
        this.tileManager.clear();
    }

    public void toggleOnOff() {
        clearAllData();
        this.enabled = !this.enabled;
        Lang.send("Pl3xMap toggled " + Lang.onOff(this.enabled), true);
    }

    public void toggleMiniMap() {
        this.minimap = !this.minimap;
        Lang.send("Pl3xMap minimap toggled " + Lang.onOff(this.minimap), true);
    }

    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }

    public DownloadManager getDownloadManager() {
        return this.downloadManager;
    }

    public TileManager getTileManager() {
        return this.tileManager;
    }
}
