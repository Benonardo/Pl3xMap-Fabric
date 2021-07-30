package net.pl3x.map.fabric.pl3xmap;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.MapRenderer;
import net.pl3x.map.fabric.pl3xmap.configuration.Lang;
import net.pl3x.map.fabric.pl3xmap.listener.KeyboardListener;
import net.pl3x.map.fabric.pl3xmap.listener.ServerListener;
import net.pl3x.map.fabric.pl3xmap.manager.DownloadManager;
import net.pl3x.map.fabric.pl3xmap.manager.NetworkManager;
import net.pl3x.map.fabric.pl3xmap.manager.RegionManager;

public class Pl3xMap implements ModInitializer {
    private static Pl3xMap instance;

    public static Pl3xMap instance() {
        return instance;
    }

    private final NetworkManager networkManager;
    private final DownloadManager downloadManager;
    private final RegionManager regionManager;

    private final KeyboardListener keyboardListener;
    private final ServerListener serverListener;

    public boolean enabled = true;
    public boolean isOnServer = false;
    private String mapUrl;
    public boolean minimap = true;

    public Pl3xMap() {
        instance = this;

        this.networkManager = new NetworkManager(this);
        this.downloadManager = new DownloadManager(this);
        this.regionManager = new RegionManager(this);

        this.keyboardListener = new KeyboardListener(this);
        this.serverListener = new ServerListener(this);
    }

    @Override
    public void onInitialize() {
        this.networkManager.initialize();

        this.keyboardListener.initialize();
        this.serverListener.initialize();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isOnServer() {
        return this.isOnServer;
    }

    public boolean canRenderMap() {
        return this.enabled && this.isOnServer && this.mapUrl != null;
    }

    public void updateAllMapTextures() {
        MinecraftClient.getInstance().gameRenderer.getMapRenderer().mapTextures
                .values().forEach(MapRenderer.MapTexture::setNeedsUpdate);
    }

    public void toggleOnOff() {
        updateAllMapTextures();
        this.enabled = !this.enabled;
        Lang.send("Pl3xMap toggled " + Lang.onOff(this.enabled), true);
    }

    public void toggleMiniMap() {
        this.minimap = !this.minimap;
        Lang.send("Pl3xMap minimap toggled " + Lang.onOff(this.minimap), true);
    }

    public void clearAllData() {
        this.downloadManager.clear();
        this.regionManager.clear();
        updateAllMapTextures();
    }

    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }

    public DownloadManager getDownloadManager() {
        return this.downloadManager;
    }

    public RegionManager getRegionManager() {
        return this.regionManager;
    }

    public String getMapUrl() {
        return this.mapUrl;
    }

    public void setMapUrl(String url) {
        this.mapUrl = url;
    }
}
