package net.pl3x.map.fabric.tiles;

import net.pl3x.map.fabric.Pl3xMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TileDownloader {
    private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
    private final java.util.Map<Tile, CompletableFuture<Void>> queue = new HashMap<>();
    private final Pl3xMap pl3xmap;

    public TileDownloader(Pl3xMap pl3xmap) {
        this.pl3xmap = pl3xmap;
    }

    public void queue(Tile tile) {
        if (this.queue.containsKey(tile)) {
            return; // already downloading
        }
        this.queue.put(tile, CompletableFuture.runAsync(new TileQueue(this.pl3xmap, tile), this.executor)
                .whenComplete((result, throwable) -> this.queue.remove(tile))
        );
    }

    public void clear() {
        Iterator<java.util.Map.Entry<Tile, CompletableFuture<Void>>> iter = this.queue.entrySet().iterator();
        while (iter.hasNext()) {
            iter.next().getValue().cancel(true);
            iter.remove();
        }
    }
}
