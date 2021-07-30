package net.pl3x.map.fabric.pl3xmap.manager;

import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import net.pl3x.map.fabric.pl3xmap.data.Image;
import net.pl3x.map.fabric.pl3xmap.data.Map;
import net.pl3x.map.fabric.pl3xmap.data.Region;
import net.pl3x.map.fabric.pl3xmap.data.Table;
import net.pl3x.map.fabric.pl3xmap.util.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DownloadManager {
    private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);

    private final Table<CompletableFuture<Void>> queue = new Table<>();
    private final Object lock = new Object();

    private final Pl3xMap pl3xmap;

    public DownloadManager(Pl3xMap pl3xmap) {
        this.pl3xmap = pl3xmap;
    }

    public void queue(Map map, int regionX, int regionZ) {
        synchronized (this.lock) {
            if (this.queue.contains(regionX, regionZ)) {
                return; // already downloading
            }
            this.queue.put(regionX, regionZ, CompletableFuture.runAsync(new Queue(map, regionX, regionZ), executor)
                    .whenComplete((result, throwable) -> {
                        synchronized (lock) {
                            queue.remove(regionX, regionZ);
                        }
                    })
            );
        }
    }

    public void clear() {
        synchronized (this.lock) {
            this.queue.values().forEach(future -> future.cancel(true));
            this.queue.clear();
        }
    }

    private class Queue implements Runnable {
        private final Map map;
        private final int regionX;
        private final int regionZ;

        private Queue(Map map, int regionX, int regionZ) {
            this.map = map;
            this.regionX = regionX;
            this.regionZ = regionZ;
        }

        @Override
        public void run() {
            try {
                BufferedImage buffered = ImageIO.read(new URL(this.map.getUrl(this.regionX, this.regionZ)));
                Image image = new Image(512);
                for (int x = 0; x < 512; x++) {
                    for (int z = 0; z < 512; z++) {
                        image.setPixel(x, z, Color.rgb2bgr(buffered.getRGB(x, z)));
                    }
                }

                Region region = pl3xmap.getRegionManager().get(this.regionX, this.regionZ);
                region.putImage(image);
                region.update(map.getId());

                pl3xmap.updateAllMapTextures();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
