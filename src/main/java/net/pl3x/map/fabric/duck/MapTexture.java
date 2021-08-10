package net.pl3x.map.fabric.duck;

import net.pl3x.map.fabric.util.World;

public interface MapTexture {
    void skip();

    void setData(byte scale, int centerX, int centerZ, World world);

    void updateImage();
}
