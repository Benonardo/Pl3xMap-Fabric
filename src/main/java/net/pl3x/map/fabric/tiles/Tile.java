package net.pl3x.map.fabric.tiles;

import net.pl3x.map.fabric.util.Image;
import net.pl3x.map.fabric.util.World;
import net.pl3x.map.fabric.util.Constants;

import java.awt.image.BufferedImage;
import java.io.File;

public class Tile {
    public static final int SIZE = 512;

    private final World world;
    private final int x;
    private final int z;
    private final Image image;
    private final Object lock = new Object();

    private long lastUsed;

    public Tile(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.image = new Image(SIZE);
        updateLastUsed();
    }

    public World getWorld() {
        return this.world;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public Image getImage() {
        updateLastUsed();
        synchronized (this.lock) {
            return this.image;
        }
    }

    public void updateImage() {
        this.lastUsed = Integer.MIN_VALUE;
    }

    public long lastUsed() {
        return this.lastUsed;
    }

    private void updateLastUsed() {
        if (this.lastUsed >= 0) {
            this.lastUsed = System.currentTimeMillis();
        }
    }

    public void setImage(BufferedImage bufferedImage) {
        synchronized (this.lock) {
            for (int x = 0; x < SIZE; x++) {
                for (int z = 0; z < SIZE; z++) {
                    this.image.setPixel(x, z, rgb2bgr(bufferedImage.getRGB(x, z)));
                }
            }
        }
        updateLastUsed();
    }

    private int rgb2bgr(int color) {
        // Minecraft flips red and blue for some reason
        // lets flip them back
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public File getFile() {
        File dir = new File(Constants.MODID, this.world.getUUID().toString());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IllegalStateException("Cannot create tiles directory for " + this.world);
            }
        }
        return new File(dir, this.x + "_" + this.z + ".png");
    }
}
