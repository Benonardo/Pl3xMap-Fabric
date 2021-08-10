package net.pl3x.map.fabric.gui.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.pl3x.map.fabric.configuration.options.BooleanOption;

import java.util.List;

public class Button extends PressableWidget implements Tickable {
    private final Screen screen;
    private final Text tooltip;
    private final PressAction onPress;
    private final BooleanOption option;
    private int tooltipDelay;

    public Button(Screen screen, int x, int y, int width, int height, BooleanOption option) {
        this(screen, x, y, width, height, option.getName(), option.tooltip(), option.onPress(), option);
    }

    public Button(Screen screen, int x, int y, int width, int height, Text name, Text tooltip, PressAction onPress) {
        this(screen, x, y, width, height, name, tooltip, onPress, null);
    }

    public Button(Screen screen, int x, int y, int width, int height, Text name, Text tooltip, PressAction onPress, BooleanOption option) {
        super(x, y, width, height, name);
        this.screen = screen;
        this.tooltip = tooltip;
        this.option = option;
        this.onPress = onPress;

        updateMessage();
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderButton(matrices, mouseX, mouseY, delta);
        if (this.isHovered() && this.tooltipDelay > 10) {
            this.renderTooltip(matrices, mouseX, mouseY);
        }
    }

    @Override
    public void tick() {
        if (this.isHovered() && this.active) {
            this.tooltipDelay++;
        } else if (this.tooltipDelay > 0) {
            this.tooltipDelay = 0;
        }
    }

    @Override
    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        List<OrderedText> tooltip = MinecraftClient.getInstance().textRenderer.wrapLines(this.tooltip, 150);
        this.screen.renderOrderedTooltip(matrices, tooltip, mouseX, mouseY);
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    public void updateMessage() {
        if (this.option != null) {
            setMessage(Text.of(this.option.getName().getString() + ": " + getStringValue()));
        }
    }

    public BooleanOption getOption() {
        return this.option;
    }

    public String getStringValue() {
        return this.option.getValue().toString();
    }

    @FunctionalInterface
    public interface PressAction {
        void onPress(Button button);
    }
}
