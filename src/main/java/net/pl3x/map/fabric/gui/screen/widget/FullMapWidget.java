package net.pl3x.map.fabric.gui.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.tiles.Tile;
import net.pl3x.map.fabric.util.World;

import java.util.LinkedHashSet;
import java.util.Set;

public class FullMapWidget implements Drawable, Element, Selectable {
    private static final boolean DEBUG = false;

    private final Set<Tile> tiles = new LinkedHashSet<>();

    private final Pl3xMap pl3xmap;
    private final MinecraftClient client;
    private final int width;
    private final int height;
    private final World world;

    private double panX;
    private double panY;

    private double offsetX;
    private double offsetY;

    private int zoom;

    private long lastClick;

    public FullMapWidget(Pl3xMap pl3xmap, MinecraftClient client, int width, int height) {
        this.pl3xmap = pl3xmap;
        this.client = client;
        this.width = width;
        this.height = height;

        this.world = this.pl3xmap.getWorld();
        this.zoom = this.world.getZoomDefault();

        if (this.client.player != null) {
            this.offsetX = screenToWorld(-this.width / 2F, 0) + this.client.player.getBlockX();
            this.offsetY = screenToWorld(-this.height / 2F, 0) + this.client.player.getBlockZ();
        }

        updateTiles();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.panX = mouseX;
        this.panY = mouseY;

        long now = System.currentTimeMillis();
        if (now - this.lastClick < 250) {
            zoom(mouseX, mouseY, 1);
        }
        this.lastClick = now;

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.panX = 0;
        this.panY = 0;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.offsetX -= (mouseX - this.panX) / getScale();
        this.offsetY -= (mouseY - this.panY) / getScale();

        this.panX = mouseX;
        this.panY = mouseY;

        updateTiles();

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        zoom(mouseX, mouseY, amount);

        return true;
    }

    public void zoom(double amount) {
        zoom(this.width / 2F, this.height / 2F, amount);
    }

    private void zoom(double mouseX, double mouseY, double amount) {
        int newZoom = Math.min(Math.max(this.zoom + (amount > 0 ? 1 : -1), 0), this.world.getZoomMax() + this.world.getZoomExtra());
        if (newZoom == this.zoom) {
            return;
        }

        double beforeX = screenToWorld(mouseX, this.offsetX);
        double beforeY = screenToWorld(mouseY, this.offsetY);

        this.zoom = newZoom;

        double afterX = screenToWorld(mouseX, this.offsetX);
        double afterY = screenToWorld(mouseY, this.offsetY);

        this.offsetX += (beforeX - afterX);
        this.offsetY += (beforeY - afterY);

        updateTiles();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return true;
    }

    public void onClose() {
        this.tiles.clear();
    }

    public double worldToScreen(double world, double offset) {
        return (world - offset) * getScale();
    }

    public double screenToWorld(double screen, double offset) {
        return (screen / getScale()) + offset;
    }

    public double getScale() {
        return (1.0D / Math.pow(2, this.world.getZoomMax() - this.zoom)) / this.client.getWindow().getScaleFactor();
    }

    public int getPosX(double mouseX) {
        return (int) screenToWorld(mouseX, this.offsetX);
    }

    public int getPosY(double mouseY) {
        return (int) screenToWorld(mouseY, this.offsetY);
    }

    public Text getUrl() {
        return new LiteralText(String.format("%s/?world=%s&zoom=%s&x=%s&z=%s",
                this.pl3xmap.getServerManager().getUrl(),
                this.world.getName(),
                this.zoom,
                Math.round(getPosX(this.width / 2F)),
                Math.round(getPosY(this.height / 2F))
        ));
    }

    public void updateTiles() {
        this.tiles.clear();

        if (this.world == null) {
            return;
        }

        double extra = Math.pow(2, this.zoom > this.world.getZoomMax() ? this.zoom - this.world.getZoomMax() : 0);
        double pow = Math.pow(2, this.world.getZoomMax() - this.zoom) * extra;

        double x0 = Math.round(((int) screenToWorld(0, this.offsetX) >> 9) / pow - 1);
        double y0 = Math.round(((int) screenToWorld(0, this.offsetY) >> 9) / pow - 1);
        double x1 = Math.round(((int) screenToWorld(this.width, this.offsetX) >> 9) / pow);
        double y1 = Math.round(((int) screenToWorld(this.height, this.offsetY) >> 9) / pow);

        for (double x = x0; x <= x1; x++) {
            for (double y = y0; y <= y1; y++) {
                this.tiles.add(this.pl3xmap.getTileManager().get(this.world, (int) x, (int) y, this.zoom));
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        double extra = Math.pow(2, this.zoom > this.world.getZoomMax() ? this.zoom - this.world.getZoomMax() : 0);
        double pow = Math.pow(2, this.world.getZoomMax() - this.zoom) * extra;
        double size = Tile.SIZE / this.client.getWindow().getScaleFactor() * extra;

        matrixStack.push();
        for (Tile tile : this.tiles) {
            float x0 = (float) Math.round(worldToScreen((tile.getX() << 9) * pow, this.offsetX));
            float y0 = (float) Math.round(worldToScreen((tile.getZ() << 9) * pow, this.offsetY));
            float x1 = (float) (x0 + size);
            float y1 = (float) (y0 + size);
            tile.render(matrixStack, x0, y0, x1, y1, 0F, 0F, 1F, 1F);

            if (DEBUG) {
                DrawableHelper.fill(matrixStack, (int) x0, (int) y0, (int) x0 + 1, (int) y1, 0x88888888);
                DrawableHelper.fill(matrixStack, (int) x0, (int) y0, (int) x1, (int) y0 + 1, 0x88888888);
            }
        }
        matrixStack.pop();

        if (DEBUG) {
            int i = -1;
            debug(matrixStack, "pow: " + pow, ++i);
            debug(matrixStack, "offset: " + this.offsetX + " " + this.offsetY, ++i);
            debug(matrixStack, "mouse: " + mouseX + " " + mouseY, ++i);
            debug(matrixStack, "scale: " + getScale(), ++i);
            debug(matrixStack, "zoom: " + this.zoom, ++i);
            debug(matrixStack, "size: " + size, ++i);
            debug(matrixStack, "window: " + width + " " + height, ++i);
            debug(matrixStack, "tile count: " + this.tiles.size(), ++i);
            debug(matrixStack, "loaded tile count: " + this.pl3xmap.getTileManager().count(), ++i);
        }
    }

    private void debug(MatrixStack matrixStack, String str, int y) {
        this.client.textRenderer.drawWithShadow(matrixStack, Text.of(str), 10, 50 + 10 * y, 0xFFFFFFFF);
    }

    @Override
    public boolean changeFocus(boolean lookForwards) {
        return true;
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }
}
