package net.pl3x.map.fabric.manager;

import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.util.World;
import net.pl3x.map.fabric.util.Constants;
import net.pl3x.map.fabric.tiles.Tile;
import net.pl3x.map.fabric.tiles.TileDownloader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TileManager {
    private final TileDownloader tileDownloader;
    private final java.util.Map<String, Tile> tiles = new HashMap<>();

    private Timer timer;

    public TileManager(Pl3xMap pl3xmap) {
        this.tileDownloader = new TileDownloader(pl3xmap);
    }

    public void enable() {
        disable();
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new UpdateTask(), Constants.TILE_UPDATE_INTERVAL, Constants.TILE_UPDATE_INTERVAL);
    }

    public void disable() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        this.tileDownloader.clear();
        this.tiles.clear();
    }

    public Tile get(World world, int x, int z) {
        String key = world.getUUID().toString() + x + "_" + z;
        Tile tile = tiles.get(key);
        if (tile == null) {
            tile = loadTile(world, x, z);
            this.tiles.put(key, tile);
        }
        return tile;
    }

    public Tile loadTile(World world, int x, int z) {
        Tile tile = new Tile(world, x, z);
        File file = tile.getFile();
        if (!file.exists()) {
            this.tileDownloader.queue(tile);
            return tile;
        }
        try {
            BufferedImage buffer = ImageIO.read(file);
            if (buffer != null) {
                tile.setImage(buffer);
                return tile;
            }
        } catch (IOException ignore) {
        }
        this.tileDownloader.queue(tile);
        return tile;
    }

    public class UpdateTask extends TimerTask {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            for (Tile tile : tiles.values()) {
                if (now - tile.lastUsed() > Constants.TILE_UPDATE_INTERVAL) {
                    tileDownloader.queue(tile);
                } else {
                    tile.updateImage();
                }
            }
        }
    }
}
