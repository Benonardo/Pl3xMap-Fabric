package net.pl3x.map.fabric.pl3xmap.data;

import net.pl3x.map.fabric.pl3xmap.Pl3xMap;

public class Map {
    private final Pl3xMap pl3xmap;
    private final int id;
    private final byte scale;
    private final int centerX;
    private final int centerZ;
    private final int zoom;
    private final String world;

    private final Image image = new Image(128);
    private final Object lock = new Object();

    private long lastUpdate;

    public Map(Pl3xMap pl3xmap, int id, byte scale, int centerX, int centerZ, int zoom, String world) {
        this.pl3xmap = pl3xmap;
        this.id = id;
        this.scale = scale;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.zoom = zoom;
        this.world = world;
        update();
    }

    public int getId() {
        return this.id;
    }

    public Image getImage() {
        long now = System.currentTimeMillis();
        if (now - this.lastUpdate > 2000) {
            update();
        }
        this.lastUpdate = now;
        synchronized (this.lock) {
            return this.image;
        }
    }

    public String getUrl(int regionX, int regionZ) {
        return String.format("%s/tiles/%s/%s/%s_%s.png", pl3xmap.getMapUrl(), this.world, this.zoom, regionX, regionZ);
    }

    public void update() {
        synchronized (this.lock) {
            int mod = 1 << this.scale;
            int startX = (this.centerX / mod - 64) * mod;
            int startZ = (this.centerZ / mod - 64) * mod;
            for (int x = 0; x < 128; x++) {
                for (int z = 0; z < 128; z++) {
                    int blockX = startX + (x * mod) + this.scale;
                    int blockZ = startZ + (z * mod) + this.scale;
                    Image pl3xImage = this.pl3xmap.getRegionManager().get(blockX >> 9, blockZ >> 9).getImage(this);
                    this.image.setPixel(x, z, pl3xImage == null ? 0 : pl3xImage.getPixel(blockX & 511, blockZ & 511));
                }
            }
        }
    }
}
