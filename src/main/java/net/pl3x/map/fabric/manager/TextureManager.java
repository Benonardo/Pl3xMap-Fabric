package net.pl3x.map.fabric.manager;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.world.World;
import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.util.Constants;

import java.io.IOException;
import java.io.InputStream;

public class TextureManager {
    public static final Identifier FRAME_CIRCLE = new Identifier(Constants.MODID, "minimap_frame_circle_texture");
    public static final Identifier FRAME_SQUARE = new Identifier(Constants.MODID, "minimap_frame_square_texture");
    public static final Identifier MASK_CIRCLE = new Identifier(Constants.MODID, "minimap_mask_circle_texture");
    public static final Identifier MASK_SQUARE = new Identifier(Constants.MODID, "minimap_mask_square_texture");
    public static final Identifier MAP = new Identifier(Constants.MODID, "minimap_map_texture");
    public static final Identifier SELF = new Identifier(Constants.MODID, "minimap_self_texture");
    public static final Identifier OVERWORLD_SKY = new Identifier(Constants.MODID, "minimap_overworld_sky_texture");
    public static final Identifier NETHER_SKY = new Identifier(Constants.MODID, "minimap_nether_sky_texture");
    public static final Identifier END_SKY = new Identifier(Constants.MODID, "minimap_end_sky_texture");

    public void initialize() {
        loadTexture(FRAME_CIRCLE, "/assets/pl3xmap/gui/frame_circle.png");
        loadTexture(FRAME_SQUARE, "/assets/pl3xmap/gui/frame_square.png");
        loadTexture(MASK_CIRCLE, "/assets/pl3xmap/gui/mask_circle.png");
        loadTexture(MASK_SQUARE, "/assets/pl3xmap/gui/mask_square.png");
        loadTexture(SELF, "/assets/pl3xmap/gui/player.png");
        loadTexture(OVERWORLD_SKY, "/assets/pl3xmap/gui/overworld_sky.png");
        loadTexture(NETHER_SKY, "/assets/pl3xmap/gui/nether_sky.png");
        loadTexture(END_SKY, "/assets/pl3xmap/gui/end_sky.png");
    }

    private void loadTexture(Identifier identifier, String resource) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::initialize);
            return;
        }
        InputStream stream = Pl3xMap.class.getResourceAsStream(resource);
        if (stream == null) {
            return;
        }
        try {
            NativeImageBackedTexture texture = new NativeImageBackedTexture(512, 512, true);
            texture.setImage(NativeImage.read(stream));
            texture.upload();
            MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, texture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void drawTexture(MatrixStack matrixStack, Identifier texture, float x0, float y0, float x1, float y1, float u, float v) {
        drawTexture(matrixStack, texture, x0, y0, x1, y1, u, u, v, v);
    }

    public void drawTexture(MatrixStack matrixStack, Identifier texture, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        Matrix4f model = matrixStack.peek().getModel();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(model, x0, y1, 0F).texture(u0, v1).next();
        bufferBuilder.vertex(model, x1, y1, 0F).texture(u1, v1).next();
        bufferBuilder.vertex(model, x1, y0, 0F).texture(u1, v0).next();
        bufferBuilder.vertex(model, x0, y0, 0F).texture(u0, v0).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    public Identifier getTexture(World world) {
        if (world.getRegistryKey() == World.NETHER) {
            return TextureManager.NETHER_SKY;
        } else if (world.getRegistryKey() == World.END) {
            return TextureManager.END_SKY;
        } else {
            return TextureManager.OVERWORLD_SKY;
        }
    }
}
