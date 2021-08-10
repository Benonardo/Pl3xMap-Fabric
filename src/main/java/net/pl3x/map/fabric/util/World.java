package net.pl3x.map.fabric.util;

import java.util.Objects;
import java.util.UUID;

public final class World {
    private final UUID uuid;
    private final String name;
    private final int zoom;

    public World(UUID uuid, String name, int zoom) {
        this.uuid = uuid;
        this.name = name;
        this.zoom = zoom;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getZoom() {
        return zoom;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (World) obj;
        return Objects.equals(this.uuid, that.uuid) &&
                Objects.equals(this.name, that.name) &&
                this.zoom == that.zoom;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, zoom);
    }

    @Override
    public String toString() {
        return "World[" +
                "uuid=" + uuid + ", " +
                "name=" + name + ", " +
                "zoom=" + zoom + ']';
    }
}
