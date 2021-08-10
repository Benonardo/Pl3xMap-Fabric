package net.pl3x.map.fabric.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.configuration.MiniMapConfig;
import net.pl3x.map.fabric.scheduler.Task;
import net.pl3x.map.fabric.tiles.Tile;
import net.pl3x.map.fabric.util.Constants;
import net.pl3x.map.fabric.util.Image;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class MiniMap {
    private final static int MAP_SIZE = 512;

    private final static Identifier FRAME_CIRCLE = new Identifier(Constants.MODID, "minimap_frame_circle_texture");
    private final static Identifier FRAME_SQUARE = new Identifier(Constants.MODID, "minimap_frame_square_texture");
    private final static Identifier MASK_CIRCLE = new Identifier(Constants.MODID, "minimap_mask_circle_texture");
    private final static Identifier MASK_SQUARE = new Identifier(Constants.MODID, "minimap_mask_square_texture");
    private final static Identifier MAP = new Identifier(Constants.MODID, "minimap_map_texture");
    private final static Identifier SELF = new Identifier(Constants.MODID, "minimap_self_texture");
    private final static Identifier OVERWORLD_SKY = new Identifier(Constants.MODID, "minimap_overworld_sky_texture");
    private final static Identifier NETHER_SKY = new Identifier(Constants.MODID, "minimap_nether_sky_texture");
    private final static Identifier END_SKY = new Identifier(Constants.MODID, "minimap_end_sky_texture");

    private final Pl3xMap pl3xmap;
    private final MinecraftClient client;

    private boolean enabled;
    private boolean visible;

    private ClientPlayerEntity player;

    private int windowWidth;
    private int windowHeight;

    private NativeImageBackedTexture mapTexture;
    private final Image image = new Image(MAP_SIZE);

    private int textX;
    private int textZ;
    private final float textScale = 0.85F;

    private boolean northLocked;
    private boolean circular;
    private boolean showFrame;
    private boolean showCoordinates;
    private boolean showDirections;

    private Anchor anchorX;
    private Anchor anchorZ;
    private int anchorOffsetX;
    private int anchorOffsetZ;

    private int zoom;
    private float deltaZoom;
    private int zoomLevel;

    private int size;
    private int halfSize;
    private int halfSizeSq;
    private int doubleSize;

    private float percentX;
    private float percentZ;

    private int centerX;
    private int centerZ;
    private int left;
    private int top;

    private Tile tile;
    private final Object lock = new Object();
    private boolean updating;

    private UpdateMiniMap updateTask;

    public MiniMap(Pl3xMap pl3xmap) {
        this.pl3xmap = pl3xmap;
        this.client = MinecraftClient.getInstance();
    }

    public void initialize() {
        HudRenderCallback.EVENT.register((matrixStack, delta) -> {
            if (!this.enabled || !this.visible) {
                return;
            }
            if (this.client.options.debugEnabled) {
                return;
            }
            this.render(matrixStack, delta);
        });
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void enable() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::enable);
            return;
        }

        this.enabled = true;
        this.visible = true;

        this.player = this.client.player;

        this.windowWidth = this.client.getWindow().getScaledWidth();
        this.windowHeight = this.client.getWindow().getScaledHeight();

        if (this.mapTexture == null) {
            this.mapTexture = new NativeImageBackedTexture(MAP_SIZE, MAP_SIZE, true);
            this.client.getTextureManager().registerTexture(MAP, this.mapTexture);
            loadTexture(FRAME_CIRCLE, "/assets/pl3xmap/gui/frame_circle.png");
            loadTexture(FRAME_SQUARE, "/assets/pl3xmap/gui/frame_square.png");
            loadTexture(MASK_CIRCLE, "/assets/pl3xmap/gui/mask_circle.png");
            loadTexture(MASK_SQUARE, "/assets/pl3xmap/gui/mask_square.png");
            loadTexture(SELF, "/assets/pl3xmap/gui/player.png");
            loadTexture(OVERWORLD_SKY, "/assets/pl3xmap/gui/overworld_sky.png");
            loadTexture(NETHER_SKY, "/assets/pl3xmap/gui/nether_sky.png");
            loadTexture(END_SKY, "/assets/pl3xmap/gui/end_sky.png");
        }

        MiniMapConfig config = this.pl3xmap.getConfig().getMinimap();

        this.northLocked = config.getNorthLock();
        this.circular = config.getCircular();
        this.showFrame = config.getDrawFrame();
        this.showCoordinates = config.getCoordinates();
        this.showDirections = config.getDirections();

        setZoomLevel(config.getZoom());

        setSize(config.getSize());

        this.anchorX = Anchor.get(config.getAnchorX());
        this.anchorZ = Anchor.get(config.getAnchorZ());
        this.anchorOffsetX = config.getAnchorOffsetX();
        this.anchorOffsetZ = config.getAnchorOffsetZ();
        this.percentX = this.anchorX.getPercent(this.windowWidth, this.anchorOffsetX);
        this.percentZ = this.anchorZ.getPercent(this.windowHeight, this.anchorOffsetZ);

        updateCenter();

        this.updateTask = new UpdateMiniMap(config.getUpdateInterval());
        this.pl3xmap.getScheduler().addTask(this.updateTask);

        this.deltaZoom = 0.0F;

        updateMapTexture();
    }

    public void disable() {
        this.enabled = false;

        if (this.updateTask != null) {
            this.updateTask.cancel();
        }
        this.updateTask = null;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getTop() {
        return this.top;
    }

    public int getLeft() {
        return this.left;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public int getCenterZ() {
        return this.centerZ;
    }

    public void setCenter(int centerX, int centerZ, boolean updateAnchors) {
        this.percentX = centerX / (float) this.windowWidth;
        this.percentZ = centerZ / (float) this.windowHeight;
        if (updateAnchors) {
            this.anchorX = Anchor.get(this.percentX * 100F);
            this.anchorZ = Anchor.get(this.percentZ * 100F);
            this.anchorOffsetX = this.anchorX.getOffset(this.windowWidth, centerX);
            this.anchorOffsetZ = this.anchorZ.getOffset(this.windowHeight, centerZ);
        }
        updateCenter();
        this.pl3xmap.getConfig().getMinimap().setAnchorX(this.anchorX.value());
        this.pl3xmap.getConfig().getMinimap().setAnchorZ(this.anchorZ.value());
        this.pl3xmap.getConfig().getMinimap().setAnchorOffsetX(this.anchorOffsetX);
        this.pl3xmap.getConfig().getMinimap().setAnchorOffsetZ(this.anchorOffsetZ);
    }

    public void updateCenter() {
        this.centerX = (int) (this.anchorX.percent() * this.windowWidth) + this.anchorOffsetX;
        this.centerZ = (int) (this.anchorZ.percent() * this.windowHeight) + this.anchorOffsetZ;
        this.left = this.centerX - this.halfSize;
        this.top = this.centerZ - this.halfSize;
        this.textX = (int) (this.centerX / this.textScale);
        this.textZ = (int) ((this.centerZ + this.halfSize) / this.textScale) + 5; // add 5 for padding
    }

    public int getSize() {
        return this.size;
    }

    public int getHalfSize() {
        return this.halfSize;
    }

    public void addSize(int add) {
        setSize(this.size + add);
        updateCenter();
    }

    private void setSize(int size) {
        if (size < 64) size = 64;
        if (size > 512) size = 512;
        this.size = size;
        this.halfSize = (int) (size / 2F);
        this.halfSizeSq = this.halfSize * this.halfSize;
        this.doubleSize = this.size * 2;
        this.pl3xmap.getConfig().getMinimap().setSize(this.size);
    }

    public int getZoom() {
        return this.zoom;
    }

    public void addZoomLevel(int add) {
        setZoomLevel(this.zoomLevel + add);
    }

    public void setZoomLevel(int level) {
        this.zoomLevel = Math.min(Math.max(level, 0), 6);
        int zoom = this.zoomLevel <= 0 ? 64 : (int) (7 * Math.pow(2, this.zoomLevel)) + 64;
        this.zoom = Math.min(Math.max(zoom, 64), 512);
        this.pl3xmap.getConfig().getMinimap().setZoom(this.zoomLevel);
    }

    public float deltaZoom(float delta) {
        if (Math.abs(this.zoom - this.deltaZoom) > 0.01F) {
            this.deltaZoom = this.deltaZoom + delta / 5F * (this.zoom - this.deltaZoom);
        }
        return this.deltaZoom;
    }

    public void setUpdateInterval(int value) {
        this.updateTask.updateInterval = value;
    }

    public void setShowCoordinates(boolean value) {
        this.showCoordinates = value;
    }

    public void setShowDirections(boolean value) {
        this.showDirections = value;
    }

    public void setCircular(boolean value) {
        this.circular = value;
    }

    public void setShowFrame(boolean value) {
        this.showFrame = value;
    }

    public void setNorthLocked(boolean value) {
        this.northLocked = value;
    }

    public boolean contains(double centerX, double centerZ) {
        centerX -= this.centerX;
        centerZ -= this.centerZ;
        return (centerX * centerX + centerZ * centerZ) < this.halfSizeSq;
    }

    public void checkWindowResize() {
        int width = this.client.getWindow().getScaledWidth();
        int height = this.client.getWindow().getScaledHeight();
        boolean update = false;
        int x = this.centerX;
        int z = this.centerZ;
        if (this.windowWidth != width) {
            switch (this.anchorX) {
                case MID -> x = width / 2 + this.left - this.windowWidth / 2 + this.halfSize;
                case HIGH -> x = width + this.left - this.windowWidth + this.halfSize;
            }
            this.windowWidth = width;
            update = true;
        }
        if (this.windowHeight != height) {
            switch (this.anchorZ) {
                case MID -> z = height / 2 + this.top - this.windowHeight / 2 + this.halfSize;
                case HIGH -> z = height + this.top - this.windowHeight + this.halfSize;
            }
            this.windowHeight = height;
            update = true;
        }
        if (update) {
            setCenter(x, z, false);
        }
    }

    private void loadTexture(Identifier identifier, String resource) {
        InputStream stream = Pl3xMap.class.getResourceAsStream(resource);
        if (stream == null) {
            return;
        }
        try {
            NativeImageBackedTexture texture = new NativeImageBackedTexture(512, 512, true);
            texture.setImage(NativeImage.read(stream));
            texture.upload();
            this.client.getTextureManager().registerTexture(identifier, texture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateMapTexture() {
        if (!this.enabled || !this.visible) {
            return;
        }
        if (this.updating) {
            return;
        }
        if (this.pl3xmap.getWorld() == null) {
            return;
        }
        if (this.mapTexture.getImage() == null) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            int blockX, blockZ, x, z;
            int startX = (int) Math.floor(this.player.getX()) - (int) (MAP_SIZE / 2F);
            int startZ = (int) Math.floor(this.player.getZ()) - (int) (MAP_SIZE / 2F) + 1;
            synchronized (this.lock) {
                this.updating = true;
                for (x = 0; x < MAP_SIZE; x++) {
                    for (z = 0; z < MAP_SIZE; z++) {
                        blockX = startX + x;
                        blockZ = startZ + z;
                        this.tile = this.pl3xmap.getTileManager().get(this.pl3xmap.getWorld(), blockX >> 9, blockZ >> 9);
                        this.image.setPixel(x, z, this.tile == null ? 0x00000000 : this.tile.getImage().getPixel(blockX & 511, blockZ & 511));
                    }
                }
            }
        }).whenComplete((consumer, throwable) -> {
            int x, z;
            synchronized (this.lock) {
                for (x = 0; x < MAP_SIZE; x++) {
                    for (z = 0; z < MAP_SIZE; z++) {
                        this.mapTexture.getImage().setPixelColor(x, z, this.image.getPixel(x, z));
                    }
                }
                this.mapTexture.upload();
                this.updating = false;
            }
        });
    }

    public void render(MatrixStack matrixStack, float delta) {
        // get fresh reference to player since vanilla destroys this object on dimension change
        this.player = this.client.player;

        // setup opengl stuff - this ensures we draw how we expect in case another mod left without cleaning up
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        // setup texture size stuff - most of this is used multiple times so these act like a "cache" for this frame
        float zoomDelta = this.deltaZoom(this.client.getLastFrameDuration());
        float scale = this.size / zoomDelta;
        float halfScale = scale / 2F;
        float markerOffset = scale / (scale * 2);
        float x0 = this.left + scale;
        float x1 = x0 + this.size - scale * 2;
        float y0 = this.top + scale;
        float y1 = y0 + this.size - scale * 2;
        float u = (MAP_SIZE / 2F - zoomDelta / 2F) / MAP_SIZE; // resizes with zoom _and_ size
        float u2 = (MAP_SIZE / 2F - this.size / 2F) / MAP_SIZE; // doesn't resize with size or zoom
        float v = 1.0F - u;
        float v2 = 1.0F - u2;

        // we only allow rotating if _not_ north locked and _not_ circular
        float angle = (this.player.getYaw(delta) - 180.0F) % 360.0F;

        // draw mask - uses special blend which writes to the alpha channel where black pixels exist.
        // the mask is twice as large as the map texture and the black pixels are the map size.
        // this ensures pixels outside the map area won't draw to the screen. this fixes rough edges
        // on circle maps and prevents corners from extending beyond the frame on rotating square maps.
        RenderSystem.blendFuncSeparate(GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_SRC_COLOR, GL11.GL_ZERO);
        {
            RenderSystem.setShaderTexture(0, this.circular ? MASK_CIRCLE : MASK_SQUARE);
            DrawableHelper.drawTexture(matrixStack, this.left - this.halfSize, this.top - this.halfSize, 0, 0, 0, this.doubleSize, this.doubleSize, this.doubleSize, this.doubleSize);
        }

        // draw sky background - uses special blend which only writes where high alpha values exist from above
        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
        {
            matrixStack.push();
            if (this.player.world.getRegistryKey() == World.NETHER) {
                RenderSystem.setShaderTexture(0, NETHER_SKY);
            } else if (this.player.world.getRegistryKey() == World.END) {
                RenderSystem.setShaderTexture(0, END_SKY);
            } else {
                RenderSystem.setShaderTexture(0, OVERWORLD_SKY);
            }
            drawLayer(matrixStack, x0 - halfScale, y0 + halfScale, x1 - halfScale, y1 + halfScale, u2, v2);
            matrixStack.pop();
        }

        // draw map - still using special blend from above
        {
            matrixStack.push();
            if (!this.northLocked) {
                if (!this.circular) {
                    // scale square map to hide missing pixels in corners
                    matrixStack.translate(this.centerX, this.centerZ, 0);
                    matrixStack.scale(1.41421356237F, 1.41421356237F, 1.41421356237F);
                    matrixStack.translate(-this.centerX, -this.centerZ, 0);
                }
                rotateScene(matrixStack, this.centerX, this.centerZ, -angle);
            }
            RenderSystem.setShaderTexture(0, MAP);
            drawLayer(matrixStack, x0 - halfScale, y0 + halfScale, x1 - halfScale, y1 + halfScale, u, v);
            matrixStack.pop();
        }

        // use a special blend that supports translucent pixels for the remaining textures
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // draw self marker
        {
            matrixStack.push();
            if (this.northLocked) {
                // only allow rotating if map is not rotating or if northlocked
                rotateScene(matrixStack, this.centerX, this.centerZ, angle);
            }
            RenderSystem.setShaderTexture(0, SELF);
            drawLayer(matrixStack, x0 + markerOffset, y0, x1 + markerOffset, y1, u2, v2);
            matrixStack.pop();
        }

        // draw the frame
        if (this.showFrame) {
            RenderSystem.setShaderTexture(0, this.circular ? FRAME_CIRCLE : FRAME_SQUARE);
            DrawableHelper.drawTexture(matrixStack, this.left, this.top, 0, 0, 0, this.size, this.size, this.size, this.size);
        }

        // draw cardinal directions
        if (this.showDirections) {
            int dirX = (int) (this.centerX / this.textScale) + 1;
            int dirY = (int) (this.centerZ / this.textScale) - 3;
            float angle2 = this.northLocked ? 0.0F : angle;
            float distance = (this.halfSize / this.textScale) + 4;
            if (!this.circular) {
                if (!this.northLocked && angle2 != 0.0F) {
                    distance /= cos(45.0F - Math.abs(45.0F + (-Math.abs(angle2) % 90.0F)));
                }
            }
            matrixStack.push();
            matrixStack.scale(this.textScale, this.textScale, this.textScale);
            {
                matrixStack.push();
                matrixStack.translate(distance * sin(angle2 - 180), distance * cos(angle2 - 180), 0);
                text(matrixStack, "N", dirX, dirY);
                matrixStack.pop();
                matrixStack.push();
                matrixStack.translate(distance * sin(angle2 + 90), distance * cos(angle2 + 90), 0);
                text(matrixStack, "E", dirX, dirY);
                matrixStack.pop();
                matrixStack.push();
                matrixStack.translate(distance * sin(angle2), distance * cos(angle2), 0);
                text(matrixStack, "S", dirX, dirY);
                matrixStack.pop();
                matrixStack.push();
                matrixStack.translate(distance * sin(angle2 - 90), distance * cos(angle2 - 90), 0);
                text(matrixStack, "W", dirX, dirY);
                matrixStack.pop();
            }
            matrixStack.pop();
        }

        // draw coordinates
        if (this.showCoordinates) {
            matrixStack.push();
            matrixStack.scale(this.textScale, this.textScale, this.textScale);
            text(matrixStack, String.format("%s, %s, %s", (int) Math.floor(this.player.getX()), (int) Math.floor(this.player.getY()), (int) Math.floor(this.player.getZ())), this.textX, this.textZ + 8);
            matrixStack.pop();
        }

        // done
        RenderSystem.disableBlend();
    }

    private void drawLayer(MatrixStack matrixStack, float x0, float y0, float x1, float y1, float u, float v) {
        Matrix4f model = matrixStack.peek().getModel();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(model, x0, y1, 0F).texture(u, v).next();
        bufferBuilder.vertex(model, x1, y1, 0F).texture(v, v).next();
        bufferBuilder.vertex(model, x1, y0, 0F).texture(v, u).next();
        bufferBuilder.vertex(model, x0, y0, 0F).texture(u, u).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    private void text(MatrixStack matrixStack, String text, int x, int y) {
        x -= (int) (this.client.textRenderer.getWidth(text) / 2F);
        this.client.textRenderer.draw(matrixStack, text, x + 1, y + 1, 0xFF3F3F3F);
        this.client.textRenderer.draw(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    private void rotateScene(MatrixStack matrixStack, int x, int y, float degrees) {
        matrixStack.translate(x, y, 0);
        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(degrees));
        matrixStack.translate(-x, -y, 0);
    }

    private float cos(float degree) {
        return (float) Math.cos(Math.toRadians(degree));
    }

    private float sin(float degree) {
        return (float) Math.sin(Math.toRadians(degree));
    }

    private enum Anchor {
        LOW(0), MID(50), HIGH(100);

        private final static double ONE = 100 * (1.0F / 3.0F);
        private final static double TWO = 100 * (2.0F / 3.0F);

        private final int value;

        Anchor(int value) {
            this.value = value;
        }

        private int value() {
            return value;
        }

        public float percent() {
            return value / 100F;
        }

        public int getOffset(int windowSize, int center) {
            return center - (int) (windowSize * percent());
        }

        public float getPercent(int windowSize, int offset) {
            return (windowSize * percent() + offset) / windowSize;
        }

        private static Anchor get(double value) {
            return value < ONE ? LOW : value < TWO ? MID : HIGH;
        }
    }

    private class UpdateMiniMap extends Task {
        private int updateInterval;
        private int tick;

        private UpdateMiniMap(int updateInterval) {
            super(0, true);
            this.updateInterval = updateInterval;
        }

        @Override
        public void run() {
            if (this.tick++ >= this.updateInterval) {
                updateMapTexture();
                this.tick = 0;
            }
            checkWindowResize();
        }
    }
}
