package net.pl3x.map.fabric;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.pl3x.map.fabric.configuration.Config;
import net.pl3x.map.fabric.gui.MiniMap;
import net.pl3x.map.fabric.manager.ConfigManager;
import net.pl3x.map.fabric.manager.ServerManager;
import net.pl3x.map.fabric.duck.MapTexture;
import net.pl3x.map.fabric.keyboard.Keyboard;
import net.pl3x.map.fabric.manager.NetworkManager;
import net.pl3x.map.fabric.manager.TileManager;
import net.pl3x.map.fabric.scheduler.Scheduler;
import net.pl3x.map.fabric.util.World;

public class Pl3xMap implements ModInitializer {
    private static Pl3xMap instance;

    public static Pl3xMap instance() {
        return instance;
    }

    private final ConfigManager configManager;

    private final NetworkManager networkManager;
    private final ServerManager serverManager;
    private final TileManager tileManager;

    private final Scheduler scheduler;
    private final Keyboard keyboard;

    private final MiniMap minimap;

    private boolean rendererEnabled;
    private World world;

    public Pl3xMap() {
        instance = this;

        this.configManager = new ConfigManager();

        this.networkManager = new NetworkManager(this);
        this.serverManager = new ServerManager(this);
        this.tileManager = new TileManager(this);

        this.scheduler = new Scheduler();
        this.keyboard = new Keyboard(this);

        this.minimap = new MiniMap(this);
    }

    @Override
    public void onInitialize() {
        if (this.configManager.getConfig() == null) {
            try {
                throw new IllegalStateException("Could not load Pl3xMap configuration");
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return;
            }
        }

        this.networkManager.initialize();
        this.serverManager.initialize();

        this.scheduler.initialize();
        this.keyboard.initialize();

        this.minimap.initialize();
    }

    public void enable() {
        this.rendererEnabled = configManager.getConfig().getRenderer().getEnabled();

        this.serverManager.enable();
        this.tileManager.enable();
    }

    public void disable() {
        this.rendererEnabled = false;

        this.scheduler.cancelAll();

        this.serverManager.disable();
        this.tileManager.disable();

        this.minimap.disable();

        clearAllData();
    }

    public boolean rendererEnabled() {
        return rendererEnabled;
    }

    public void setRendererEnabled(boolean value) {
        this.rendererEnabled = value;
    }

    public void updateAllMapTextures() {
        MinecraftClient.getInstance().gameRenderer.getMapRenderer().mapTextures
                .values().forEach(texture -> ((MapTexture) texture).updateImage());
    }

    public void clearAllData() {
        this.serverManager.disable();
        this.tileManager.disable();

        updateAllMapTextures();
    }

    public Config getConfig() {
        return this.configManager.getConfig();
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public ServerManager getServerManager() {
        return this.serverManager;
    }

    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }

    public TileManager getTileManager() {
        return this.tileManager;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public MiniMap getMiniMap() {
        return this.minimap;
    }

    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
