package net.pl3x.map.fabric.gui.screen;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.gui.screen.widget.Button;
import net.pl3x.map.fabric.gui.screen.widget.Coordinates;
import net.pl3x.map.fabric.gui.screen.widget.FullMapWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FullMapScreen extends AbstractScreen {
    private static final Text OPTIONS = new TranslatableText("pl3xmap.screen.full-map.options");
    private static final Text OPTIONS_TOOLTIP = new TranslatableText("pl3xmap.screen.full-map.options.tooltip");

    private static final Text ZOOM_IN = Text.of("+");
    private static final Text ZOOM_OUT = Text.of("-");
    private static final Text LINK = Text.of("?");

    private static final Text BLANK = Text.of("");

    private static final Text LINK_CONFIRMATION = new TranslatableText("chat.link.confirmTrusted");

    private static final Text OPEN = new TranslatableText("chat.link.open");
    private static final Text COPY = new TranslatableText("chat.copy");
    private static final Text CANCEL = ScreenTexts.CANCEL;

    private final List<Element> reverse = new ArrayList<>();
    private final List<ButtonWidget> confirmLink = new ArrayList<>();

    private FullMapWidget fullmap;

    private Identifier background;
    private Text openURL;

    public FullMapScreen(Pl3xMap pl3xmap, Screen parent) {
        super(pl3xmap, parent);

        // hide minimap so we're not drawing it needlessly
        this.pl3xmap.getMiniMap().setVisible(false);
    }

    @Override
    protected void init() {
        updateBackground();

        // skip straight to options screen if no pl3x world set
        if (this.pl3xmap.getWorld() == null) {
            openScreen(new OptionsScreen(this.pl3xmap, null));
            return;
        }

        // fullmap is a clickable widget for click and drag conveniences
        addDrawableChild(this.fullmap = new FullMapWidget(this.pl3xmap, this.client, this.width, this.height));

        List.of(
                new Button(this, 5, 5, 20, 20, ZOOM_IN, BLANK, (button) -> this.fullmap.zoom(1)),
                new Button(this, 5, 25, 20, 20, ZOOM_OUT, BLANK, (button) -> this.fullmap.zoom(-1)),
                new Button(this, 5, this.height - 25, 20, 20, LINK, BLANK, (button) -> this.openURL = this.fullmap.getUrl()),
                new Coordinates(this.fullmap, 30, this.height - 25, 50, 20),
                // TODO sidebar for world and player select
                new Button(this, this.width - 87, this.height - 25, 80, 20, OPTIONS, OPTIONS_TOOLTIP, (button) -> openScreen(new OptionsScreen(this.pl3xmap, this)))
        ).forEach(this::addDrawableChild);

        this.confirmLink.clear();
        this.confirmLink.addAll(List.of(
                new ButtonWidget(this.width / 2 - 50 - 105, this.height / 6 + 96, 100, 20, OPEN, (button) -> openLink()),
                new ButtonWidget(this.width / 2 - 50, this.height / 6 + 96, 100, 20, COPY, (button) -> copyLink()),
                new ButtonWidget(this.width / 2 - 50 + 105, this.height / 6 + 96, 100, 20, CANCEL, (button) -> cancel())
        ));

        // reverse the elements to draw bottom up and click top down
        this.reverse.addAll(this.children());
        Collections.reverse(this.reverse);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        if (this.background == null) {
            renderBackground(matrixStack);
        } else {
            this.pl3xmap.getTextureManager().drawTexture(matrixStack, this.background, 0, 0, this.width, this.height, 0, 0, this.width / 512F, this.height / 512F);
        }

        super.render(matrixStack, mouseX, mouseY, delta);

        drawText(matrixStack, this.title, (int) (this.width / 2F), 15);

        if (openURL != null) {
            this.fillGradient(matrixStack, 0, 0, this.width, this.height, 0xD00F4863, 0xC0370038);
            drawCenteredText(matrixStack, this.textRenderer, LINK_CONFIRMATION, this.width / 2, 70, 16777215);
            drawCenteredText(matrixStack, this.textRenderer, this.openURL, this.width / 2, 90, 16777215);
            for (ButtonWidget buttonWidget : this.confirmLink) {
                buttonWidget.render(matrixStack, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int vOffset) {
        // don't waste cpu on super's background
    }

    private void updateBackground() {
        if (this.client == null || this.client.world == null) {
            this.background = null;
        } else {
            this.background = this.pl3xmap.getTextureManager().getTexture(this.client.world);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // reverse so we click buttons before clicking map
        Iterator<? extends Element> iter = (this.openURL != null ? this.confirmLink : this.reverse).iterator();
        Element element;
        do {
            if (!iter.hasNext()) {
                return false;
            }
            element = iter.next();
        } while (!element.mouseClicked(mouseX, mouseY, button));
        this.setFocused(element);
        if (button == 0) {
            this.setDragging(true);
        }
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (this.openURL != null) {
            cancel();
            return false;
        }
        return true;
    }

    @Override
    public void onClose() {
        this.fullmap.onClose();
        this.pl3xmap.getMiniMap().setVisible(true);
        super.onClose();
    }

    private void openLink() {
        Util.getOperatingSystem().open(this.openURL.asString());
        this.openURL = null;
    }

    private void copyLink() {
        if (this.client != null) {
            this.client.keyboard.setClipboard(this.openURL.asString());
        }
        this.openURL = null;
    }

    private void cancel() {
        this.openURL = null;
    }
}
