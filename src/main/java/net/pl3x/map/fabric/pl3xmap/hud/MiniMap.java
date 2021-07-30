package net.pl3x.map.fabric.pl3xmap.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class MiniMap {
    private final MinecraftClient client;
    private final Identifier identifier = new Identifier("pl3xmap", "minimap");

    private NativeImageBackedTexture texture;

    public MiniMap() {
        this.client = MinecraftClient.getInstance();

        //HudRenderCallback.EVENT.register(this::render);
    }

    public void initialize() {
        System.out.println("Init minimap");
        this.texture = new NativeImageBackedTexture(256, 256, true);
        this.client.getTextureManager().registerTexture(this.identifier, this.texture);
    }

    public void disable() {
        System.out.println("Disable minimap");
        this.client.getTextureManager().destroyTexture(this.identifier);
        this.texture = null;
    }

    private void render(MatrixStack matrixStack, float delta) {
        if (this.client.player == null) {
            return;
        }
        if (this.client.options.debugEnabled) {
            return;
        }

        matrixStack.push();

        //this.client.textRenderer.draw(matrixStack, "Text", 5, 5, -1);

        updateTexture(this.client.player.getBlockPos());

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.identifier);
        DrawableHelper.drawTexture(matrixStack, 10, 10, 0, 0, 0, 128, 128, 128, 128);

        matrixStack.pop();
    }

    public void updateTexture(BlockPos pos) {
        if (pos == null || this.texture.getImage() == null) {
            return;
        }
        for (int x = 0; x < 256; x++) {
            for (int z = 0; z < 256; z++) {
                this.texture.getImage().setPixelColor(x, z, 0xFF6699CC); // TODO
            }
        }
        this.texture.upload();
    }
}
