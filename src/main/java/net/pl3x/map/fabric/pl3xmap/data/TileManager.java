package net.pl3x.map.fabric.pl3xmap.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.pl3x.map.fabric.pl3xmap.Pl3xMap;

public class TileManager {
    private final Table<Integer, Integer, Map.Image> images = HashBasedTable.create();
    private final Object lock = new Object();

    private final Pl3xMap pl3xmap;

    public TileManager(Pl3xMap pl3xmap) {
        this.pl3xmap = pl3xmap;
    }

    public Map.Image get(Map map, int rX, int rZ) {
        synchronized (this.lock) {
            Map.Image img = this.images.get(rX, rZ);
            if (img != null) {
                return img;
            }
        }
        this.pl3xmap.getDownloadManager().queue(map, rX, rZ);
        return null;
    }

    public void put(int rX, int rZ, Map.Image image) {
        synchronized (this.lock) {
            this.images.put(rX, rZ, image);
        }
    }

    public void clear() {
        synchronized (this.lock) {
            this.images.clear();
        }

        this.pl3xmap.updateAllMapTextures();
    }
}
