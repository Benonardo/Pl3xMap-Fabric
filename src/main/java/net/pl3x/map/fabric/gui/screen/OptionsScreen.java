package net.pl3x.map.fabric.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.configuration.options.BooleanOption;
import net.pl3x.map.fabric.configuration.options.IntegerOption;
import net.pl3x.map.fabric.gui.screen.widget.Button;
import net.pl3x.map.fabric.gui.screen.widget.Slider;
import net.pl3x.map.fabric.gui.screen.widget.Tickable;

import java.util.List;

public class OptionsScreen extends AbstractScreen {
    private List<ClickableWidget> options;
    private final String strRenderer;
    private final String strMinimap;
    private final String strOn;
    private final String strOff;
    private final String strYes;
    private final String strNo;

    public OptionsScreen(Screen parent) {
        this(Pl3xMap.instance(), parent);
    }

    public OptionsScreen(Pl3xMap pl3xmap, Screen parent) {
        super(pl3xmap, parent);

        this.strRenderer = I18n.translate("pl3xmap.screen.options.renderer.title");
        this.strMinimap = I18n.translate("pl3xmap.screen.options.minimap.title");
        this.strOn = I18n.translate("pl3xmap.screen.options.on");
        this.strOff = I18n.translate("pl3xmap.screen.options.off");
        this.strYes = I18n.translate("pl3xmap.screen.options.yes");
        this.strNo = I18n.translate("pl3xmap.screen.options.no");
    }

    @Override
    public void init() {
        int center = (int) (this.width / 2F);

        this.options = List.of(
                new Button(this, center - 154, 65, 150, 20, new BooleanOption(
                        new TranslatableText("pl3xmap.screen.options.renderer.enabled"),
                        new TranslatableText("pl3xmap.screen.options.renderer.enabled.tooltip"),
                        () -> this.pl3xmap.getConfig().getRenderer().getEnabled(),
                        value -> {
                            this.pl3xmap.getConfig().getRenderer().setEnabled(value);
                            this.pl3xmap.setRendererEnabled(value);
                        }
                )) {
                    @Override
                    public String getStringValue() {
                        return getOption().getValue() ? strOn : strOff;
                    }
                },
                new Button(this, center + 4, 65, 150, 20, new BooleanOption(
                        new TranslatableText("pl3xmap.screen.options.renderer.fog-of-war"),
                        new TranslatableText("pl3xmap.screen.options.renderer.fog-of-war.tooltip"),
                        () -> this.pl3xmap.getConfig().getRenderer().getFogOfWar(),
                        value -> this.pl3xmap.getConfig().getRenderer().setFogOfWar(value)
                )) {
                    @Override
                    public String getStringValue() {
                        return getOption().getValue() ? strOn : strOff;
                    }
                },
                new Button(this, center - 154, 110, 150, 20, new BooleanOption(
                        new TranslatableText("pl3xmap.screen.options.minimap.enabled"),
                        new TranslatableText("pl3xmap.screen.options.minimap.enabled.tooltip"),
                        () -> this.pl3xmap.getConfig().getMinimap().getEnabled(),
                        value -> {
                            this.pl3xmap.getConfig().getMinimap().setEnabled(value);
                            if (value && this.pl3xmap.getServerManager().isOnServer() && this.pl3xmap.getServerManager().getUrl() != null) {
                                this.pl3xmap.getMiniMap().enable();
                            } else {
                                this.pl3xmap.getMiniMap().disable();
                            }
                        }
                )) {
                    @Override
                    public String getStringValue() {
                        return getOption().getValue() ? strYes : strNo;
                    }
                },
                new Button(this, center + 4, 110, 150, 20, new BooleanOption(
                        new TranslatableText("pl3xmap.screen.options.minimap.north-locked"),
                        new TranslatableText("pl3xmap.screen.options.minimap.north-locked.tooltip"),
                        () -> this.pl3xmap.getConfig().getMinimap().getNorthLock(),
                        value -> {
                            this.pl3xmap.getConfig().getMinimap().setNorthLock(value);
                            this.pl3xmap.getMiniMap().setNorthLocked(value);
                        }
                )) {
                    @Override
                    public String getStringValue() {
                        return getOption().getValue() ? strYes : strNo;
                    }
                },
                new Button(this, center - 154, 135, 150, 20, new BooleanOption(
                        new TranslatableText("pl3xmap.screen.options.minimap.frame"),
                        new TranslatableText("pl3xmap.screen.options.minimap.frame.tooltip"),
                        () -> this.pl3xmap.getConfig().getMinimap().getDrawFrame(),
                        value -> {
                            this.pl3xmap.getConfig().getMinimap().setDrawFrame(value);
                            this.pl3xmap.getMiniMap().setShowFrame(value);
                        }
                )) {
                    @Override
                    public String getStringValue() {
                        return getOption().getValue() ? strOn : strOff;
                    }
                },
                new Button(this, center + 4, 135, 150, 20, new BooleanOption(
                        new TranslatableText("pl3xmap.screen.options.minimap.circular"),
                        new TranslatableText("pl3xmap.screen.options.minimap.circular.tooltip"),
                        () -> this.pl3xmap.getConfig().getMinimap().getCircular(),
                        value -> {
                            this.pl3xmap.getConfig().getMinimap().setCircular(value);
                            this.pl3xmap.getMiniMap().setCircular(value);
                        }
                )) {
                    @Override
                    public String getStringValue() {
                        return getOption().getValue() ? strYes : strNo;
                    }
                },
                new Button(this, center - 154, 160, 150, 20, new BooleanOption(
                        new TranslatableText("pl3xmap.screen.options.minimap.directions"),
                        new TranslatableText("pl3xmap.screen.options.minimap.directions.tooltip"),
                        () -> this.pl3xmap.getConfig().getMinimap().getDirections(),
                        value -> {
                            this.pl3xmap.getConfig().getMinimap().setDirections(value);
                            this.pl3xmap.getMiniMap().setShowDirections(value);
                        }
                )) {
                    @Override
                    public String getStringValue() {
                        return getOption().getValue() ? strOn : strOff;
                    }
                },
                new Button(this, center + 4, 160, 150, 20, new BooleanOption(
                        new TranslatableText("pl3xmap.screen.options.minimap.coordinates"),
                        new TranslatableText("pl3xmap.screen.options.minimap.coordinates.tooltip"),
                        () -> this.pl3xmap.getConfig().getMinimap().getCoordinates(),
                        value -> {
                            this.pl3xmap.getConfig().getMinimap().setCoordinates(value);
                            this.pl3xmap.getMiniMap().setShowCoordinates(value);
                        }
                )) {
                    @Override
                    public String getStringValue() {
                        return getOption().getValue() ? strOn : strOff;
                    }
                },
                new Slider(this, center - 154, 185, 150, 20, new IntegerOption(
                        new TranslatableText("pl3xmap.screen.options.minimap.update-interval"),
                        new TranslatableText("pl3xmap.screen.options.minimap.update-interval.tooltip"),
                        0, 20,
                        () -> this.pl3xmap.getConfig().getMinimap().getUpdateInterval(),
                        value -> {
                            this.pl3xmap.getConfig().getMinimap().setUpdateInterval(value);
                            this.pl3xmap.getMiniMap().setUpdateInterval(value);
                        }
                )),
                new Button(this, center + 4, 185, 150, 20,
                        new TranslatableText("pl3xmap.screen.options.minimap.position-size-zoom"),
                        new TranslatableText("pl3xmap.screen.options.minimap.position-size-zoom.tooltip"),
                        (button) -> {
                            if (this.pl3xmap.getMiniMap().isEnabled() && this.pl3xmap.getServerManager().isOnServer() && this.pl3xmap.getServerManager().getUrl() != null) {
                                openScreen(new PositionScreen(this.pl3xmap, this));
                            } else {
                                button.active = false;
                                button.setMessage(new TranslatableText("pl3xmap.screen.options.minimap.position-size-zoom.error"));
                                this.pl3xmap.getScheduler().addTask(40, () -> {
                                    button.setMessage(new TranslatableText("pl3xmap.screen.options.minimap.position-size-zoom"));
                                    button.active = true;
                                });
                            }
                        }
                )
        );

        this.options.forEach(this::addDrawableChild);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        super.render(matrixStack, mouseX, mouseY, delta);

        int center = (int) (this.width / 2F);

        drawText(matrixStack, this.strRenderer, center, 50);
        drawText(matrixStack, this.strMinimap, center, 95);
    }

    @Override
    public void tick() {
        this.options.forEach(option -> {
            if (option instanceof Tickable tickable) {
                tickable.tick();
            }
        });
    }
}
