package net.pl3x.map.fabric.pl3xmap.data;

import net.pl3x.map.fabric.pl3xmap.Pl3xMap;

import java.util.HashMap;

public class Map {
    public static final java.util.Map<Integer, Map> MAPS = new HashMap<>();
    public static final String TILES = "/tiles/{world}/{zoom}/";
    public static String MAP_URL;

    private final Pl3xMap pl3xmap;
    private final byte scale;
    private final int centerX;
    private final int centerZ;
    private final int zoom;
    private final String world;

    private final Image image = new Image(128);

    public Map(Pl3xMap pl3xmap, byte scale, int centerX, int centerZ, int zoom, String world) {
        this.pl3xmap = pl3xmap;
        this.scale = scale;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.zoom = zoom;
        this.world = world;
    }

    public Image getImage() {
        update();
        return this.image;
    }

    public String getUrl(int regionX, int regionZ) {
        return MAP_URL + TILES
                .replace("{world}", this.world)
                .replace("{zoom}", String.valueOf(this.zoom))
                + (regionX + "_" + regionZ + ".png");
    }

    public void update() {
        int mod = 1 << this.scale;
        int startX = (this.centerX / mod - 64) * mod;
        int startZ = (this.centerZ / mod - 64) * mod;
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                int blockX = startX + (x * mod) + this.scale;
                int blockZ = startZ + (z * mod) + this.scale;
                Map.Image pl3xImage = this.pl3xmap.getTileManager().get(this, blockX >> 9, blockZ >> 9);
                this.image.setPixel(x, z, pl3xImage == null ? 0 : pl3xImage.getPixel(blockX & 511, blockZ & 511));
            }
        }
    }

    public static class Image {
        private final int[][] pixels;

        public Image(int size) {
            this.pixels = new int[size][size];
        }

        public void setPixel(int x, int z, int color) {
            this.pixels[x][z] = color;
        }

        public int getPixel(int x, int z) {
            return this.pixels[x][z];
        }
    }
}
