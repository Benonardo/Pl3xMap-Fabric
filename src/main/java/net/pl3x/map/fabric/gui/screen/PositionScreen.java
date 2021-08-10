package net.pl3x.map.fabric.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.gui.MiniMap;
import net.pl3x.map.fabric.gui.screen.widget.MiniMapWidget;
import org.lwjgl.glfw.GLFW;

public class PositionScreen extends AbstractScreen {
    private final MiniMap minimap;
    private final String strSize;
    private final String strCenter;
    private final String strHelp1;
    private final String strHelp2;
    private final String strHelp3;
    private final String strHelp4;

    public PositionScreen(Pl3xMap pl3xmap, Screen parent) {
        super(pl3xmap, parent);
        this.minimap = pl3xmap.getMiniMap();

        // add some key listeners
        this.keyHandler.listen(GLFW.GLFW_KEY_PAGE_UP, () -> this.minimap.addZoomLevel(1));
        this.keyHandler.listen(GLFW.GLFW_KEY_PAGE_DOWN, () -> this.minimap.addZoomLevel(-1));
        this.keyHandler.listen(GLFW.GLFW_KEY_KP_ADD, () -> this.minimap.addSize(16));
        this.keyHandler.listen(GLFW.GLFW_KEY_KP_SUBTRACT, () -> this.minimap.addSize(-16));

        // hide minimap so we're not drawing it twice
        this.minimap.setVisible(false);

        this.strSize = I18n.translate("pl3xmap.screen.position.size", "%s", "%s");
        this.strCenter = I18n.translate("pl3xmap.screen.position.center", "%s", "%s");
        this.strHelp1 = I18n.translate("pl3xmap.screen.position.help1");
        this.strHelp2 = I18n.translate("pl3xmap.screen.position.help2");
        this.strHelp3 = I18n.translate("pl3xmap.screen.position.help3");
        this.strHelp4 = I18n.translate("pl3xmap.screen.position.help4");
    }

    @Override
    protected void init() {
        addDrawableChild(new MiniMapWidget(this.minimap));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        int centerX = (int) (this.width / 2F);
        int centerY = (int) (this.height / 2F);

        drawText(matrixStack, String.format(this.strSize, this.minimap.getSize(), this.minimap.getZoom()), centerX, 30);
        drawText(matrixStack, String.format(this.strCenter, this.minimap.getCenterX(), this.minimap.getCenterZ()), centerX, 40);

        drawText(matrixStack, this.strHelp1, centerX, centerY - 30);
        drawText(matrixStack, this.strHelp2, centerX, centerY - 10);
        drawText(matrixStack, this.strHelp3, centerX, centerY + 10);
        drawText(matrixStack, this.strHelp4, centerX, centerY + 30);
    }

    @Override
    public void onClose() {
        this.minimap.setVisible(true);
        super.onClose();
    }
}
