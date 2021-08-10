package net.pl3x.map.fabric.gui.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.pl3x.map.fabric.configuration.options.IntegerOption;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class Slider extends ClickableWidget implements Tickable {
    private final Screen screen;
    private final IntegerOption option;
    private final double step;
    private double value;
    private int tooltipDelay;

    public Slider(Screen screen, int x, int y, int width, int height, IntegerOption option) {
        super(x, y, width, height, option.getName());
        this.screen = screen;
        this.option = option;
        this.step = option.getMin() / (double) option.getMax();
        this.value = getRatio(option.getValue());

        updateMessage();
    }

    @Override
    protected int getYImage(boolean hovered) {
        return 0;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        if (this.isHovered() && this.tooltipDelay > 10) {
            this.renderTooltip(matrices, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBackground(MatrixStack matrices, MinecraftClient client, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.isHovered() ? 2 : 1) * 20;
        this.drawTexture(matrices, this.x + (int) (this.value * (double) (this.width - 8)), this.y, 0, 46 + i, 4, 20);
        this.drawTexture(matrices, this.x + (int) (this.value * (double) (this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        List<OrderedText> tooltip = MinecraftClient.getInstance().textRenderer.wrapLines(this.option.tooltip(), 200);
        this.screen.renderOrderedTooltip(matrices, tooltip, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean bl = keyCode == GLFW.GLFW_KEY_LEFT;
        if (bl || keyCode == GLFW.GLFW_KEY_RIGHT) {
            float f = bl ? -1.0F : 1.0F;
            this.setValue(this.value + (double) (f / (float) (this.width - 8)));
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.setValueFromMouse(mouseX);
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
    }

    private void setValueFromMouse(double mouseX) {
        this.setValue((mouseX - (double) (this.x + 4)) / (double) (this.width - 8));
    }

    private void setValue(double value) {
        double d = this.value;
        this.value = MathHelper.clamp(value, 0.0D, 1.0D);
        int intVal = getValue(this.value);
        if (d != this.value) {
            this.option.setValue(intVal);
        }
        this.value = getRatio(intVal);

        updateMessage();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.playDownSound(MinecraftClient.getInstance().getSoundManager());
    }

    private double getRatio(double value) {
        return MathHelper.clamp((this.adjust(value) - this.option.getMin()) / (this.option.getMax() - this.option.getMin()), 0.0D, 1.0D);
    }

    private int getValue(double ratio) {
        return (int) Math.round(this.adjust(MathHelper.lerp(MathHelper.clamp(ratio, 0.0D, 1.0D), this.option.getMin(), this.option.getMax())));
    }

    private double adjust(double value) {
        if (this.step > 0.0F) {
            value = this.step * (float) Math.round(value / this.step);
        }
        return MathHelper.clamp(value, this.option.getMin(), this.option.getMax());
    }

    private void updateMessage() {
        setMessage(Text.of(this.option.getName().getString() + ": " + this.option.getValue()));
    }

    @Override
    public void tick() {
        if (this.isHovered()) {
            this.tooltipDelay++;
        } else if (this.tooltipDelay > 0) {
            this.tooltipDelay = 0;
        }
    }
}
