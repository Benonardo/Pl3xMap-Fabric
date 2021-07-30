package net.pl3x.map.fabric.pl3xmap.manager;

import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import net.pl3x.map.fabric.pl3xmap.data.Region;
import net.pl3x.map.fabric.pl3xmap.data.Table;

public class RegionManager {
    private final Table<Region> regions = new Table<>();
    private final Pl3xMap pl3xmap;

    public RegionManager(Pl3xMap pl3xmap) {
        this.pl3xmap = pl3xmap;
    }

    public Region get(int regionX, int regionZ) {
        Region region = regions.get(regionX, regionZ);
        if (region == null) {
            region = new Region(this.pl3xmap, regionX, regionZ);
            regions.put(regionX, regionZ, region);
        }
        return region;
    }

    public void clear() {
        regions.clear();
    }
}
