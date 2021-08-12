package net.pl3x.map.fabric.util;

import java.util.Objects;
import java.util.UUID;

public final class World {
    private final UUID uuid;
    private final String name;
    private final int zoomMax;
    private final int zoomDefault;
    private final int zoomExtra;

    public World(UUID uuid, String name, int zoomMax, int zoomDefault, int zoomExtra) {
        this.uuid = uuid;
        this.name = name;
        this.zoomMax = zoomMax;
        this.zoomDefault = zoomDefault;
        this.zoomExtra = zoomExtra;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getZoomMax() {
        return zoomMax;
    }

    public int getZoomDefault() {
        return this.zoomDefault;
    }

    public int getZoomExtra() {
        return this.zoomExtra;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (World) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.name, that.name) &&
                this.zoomMax == that.zoomMax &&
                this.zoomDefault == that.zoomDefault &&
                this.zoomExtra == that.zoomExtra;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, zoomMax, zoomDefault, zoomExtra);
    }

    @Override
    public String toString() {
        return "World[" +
                "uuid=" + uuid + ", " +
                "name=" + name + ", " +
                "zoomMax=" + zoomMax +
                "zoomDefault=" + zoomDefault +
                "zoomExtra=" + zoomExtra + ']';
    }
}
