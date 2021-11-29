package net.pl3x.map.fabric.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.pl3x.map.fabric.mixin.ToastAccessor;

import java.text.NumberFormat;
import java.util.Locale;

public class Coordinates extends DrawableHelper implements Drawable, Element, Selectable {
    private static final NumberFormat NUMBER = NumberFormat.getInstance(Locale.US);
    private final FullMapWidget fullmap;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Coordinates(FullMapWidget fullmap, int x, int y, int width, int height) {
        this.fullmap = fullmap;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Text getCoordinates(int mouseX, int mouseY) {
        String x = NUMBER.format(this.fullmap.getPosX(mouseX));
        String y = NUMBER.format(this.fullmap.getPosY(mouseY));
        return new TranslatableText("%s %s", x, y);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ToastAccessor.accessTEXTURE());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        Text coordinates = getCoordinates(mouseX, mouseY);
        int w2 = (int) ((textRenderer.getWidth(coordinates) + 20) / 2F);
        int h2 = (int) (this.height / 2F);
        drawTexture(matrixStack, x, y, w2, h2, 0, 0, w2, h2, 256, 256);
        drawTexture(matrixStack, x, y + h2, w2, h2, 0, 32 - h2, w2, h2, 256, 256);
        drawTexture(matrixStack, x + w2, y, w2, h2, 160 - w2, 0, w2, h2, 256, 256);
        drawTexture(matrixStack, x + w2, y + h2, w2, h2, 160 - w2, 32 - h2, w2, h2, 256, 256);
        drawCenteredText(matrixStack, textRenderer, coordinates, this.x + w2, this.y + (this.height - 8) / 2, 0xffcccccc);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
    }
}
