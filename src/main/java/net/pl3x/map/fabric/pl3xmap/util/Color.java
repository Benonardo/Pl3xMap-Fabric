package net.pl3x.map.fabric.pl3xmap.util;

public class Color {
    public static int rgb2bgr(int color) {
        // Minecraft flips red and blue for some reason
        // lets flip them back and remove the alpha channel
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        return (0xFF << 24) | (b << 16) | (g << 8) | r;
    }
}
