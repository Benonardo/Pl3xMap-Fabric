package net.pl3x.map.fabric.pl3xmap.data;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.MapRenderer;
import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import net.pl3x.map.fabric.pl3xmap.duck.MapTexture;

import java.util.HashSet;
import java.util.Set;

public class Region {
    private final Pl3xMap pl3xmap;
    private final int regionX;
    private final int regionZ;
    private final Set<Integer> maps = new HashSet<>();

    private Image image;

    public Region(Pl3xMap pl3xmap, int regionX, int regionZ) {
        this.pl3xmap = pl3xmap;
        this.regionX = regionX;
        this.regionZ = regionZ;
    }

    public Image getImage(Map map) {
        if (this.image == null) {
            this.pl3xmap.getDownloadManager().queue(map, regionX, regionZ);
        }
        return this.image;
    }

    public void putImage(Image image) {
        this.image = image;
    }

    public void update(int id) {
        Int2ObjectMap<MapRenderer.MapTexture> textures = MinecraftClient.getInstance().gameRenderer.getMapRenderer().mapTextures;
        this.maps.add(id);
        this.maps.forEach((i) -> ((MapTexture) textures.get(i.intValue())).getMap().update());
    }
}
