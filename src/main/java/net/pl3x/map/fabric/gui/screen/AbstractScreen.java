package net.pl3x.map.fabric.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.keyboard.Key;
import net.pl3x.map.fabric.scheduler.Task;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractScreen extends Screen {
    final Pl3xMap pl3xmap;
    final Screen parent;
    final KeyHandler keyHandler;

    protected AbstractScreen(Pl3xMap pl3xmap, Screen parent) {
        super(new TranslatableText("pl3xmap.screen.options.title"));
        this.pl3xmap = pl3xmap;
        this.parent = parent;
        this.keyHandler = new KeyHandler();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int vOffset) {
        if (this.client != null && this.client.world != null) {
            this.fillGradient(matrixStack, 0, 0, this.width, this.height, 0xD00F4863, 0xC0370038);
        } else {
            this.renderBackgroundTexture(vOffset);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.keyHandler.isListening(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    protected void openScreen(Screen screen) {
        if (this.client != null) {
            this.client.setScreen(screen);
        }
    }

    @Override
    public void onClose() {
        this.pl3xmap.getConfigManager().save();
        this.keyHandler.cancel();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    protected void drawText(MatrixStack matrixStack, String text, int x, int y) {
        drawText(matrixStack, Text.of(text), x, y);
    }

    protected void drawText(MatrixStack matrixStack, Text text, int x, int y) {
        x = x - (int) (this.textRenderer.getWidth(text) / 2F);
        this.textRenderer.drawWithShadow(matrixStack, text, x, y, 0xFFFFFFFF);
    }

    public class KeyHandler extends Task {
        private final Map<Integer, Key> keys = new HashMap<>();

        public KeyHandler() {
            super(0, true);
            Pl3xMap.instance().getScheduler().addTask(this);
        }

        public void listen(int code, Key.Action action) {
            this.keys.put(code, new Key(action));
        }

        public boolean isListening(int code) {
            return this.keys.containsKey(code);
        }

        public boolean isPressed(int code) {
            return client != null && InputUtil.isKeyPressed(client.getWindow().getHandle(), code);
        }

        @Override
        public void run() {
            this.keys.forEach((code, key) -> {
                if (isPressed(code)) {
                    key.press();
                } else if (key.pressed()) {
                    key.release();
                }
            });
        }
    }
}
