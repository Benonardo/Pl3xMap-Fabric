package net.pl3x.map.fabric.pl3xmap.util;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import net.pl3x.map.fabric.pl3xmap.data.Map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DownloadManager {
    private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);


    private final Table<Integer, Integer, CompletableFuture<Void>> queue = HashBasedTable.create();
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
            this.queue.put(regionX, regionZ, new Queue(map, regionX, regionZ).future());
        }
    }

    public void clear() {
        synchronized (this.lock) {
            Set<Table.Cell<Integer, Integer, CompletableFuture<Void>>> set = this.queue.cellSet();
            if (set != null) {
                for (Table.Cell<Integer, Integer, CompletableFuture<Void>> cell : new HashSet<>(set)) {
                    CompletableFuture<Void> future = cell.getValue();
                    if (future != null) {
                        future.cancel(true);
                    }
                }
            }
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

        private CompletableFuture<Void> future() {
            return CompletableFuture.runAsync(this, executor)
                    .whenComplete((result, throwable) -> {
                        synchronized (lock) {
                            queue.remove(this.regionX, this.regionZ);
                        }
                        pl3xmap.updateAllMapTextures();
                    });
        }

        @Override
        public void run() {
            try {
                BufferedImage buffered = ImageIO.read(new URL(this.map.getUrl(this.regionX, this.regionZ)));
                Map.Image image = new Map.Image(512);
                for (int x = 0; x < 512; x++) {
                    for (int z = 0; z < 512; z++) {
                        image.setPixel(x, z, Color.rgb2bgr(buffered.getRGB(x, z)));
                    }
                }
                pl3xmap.getTileManager().put(this.regionX, this.regionZ, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
