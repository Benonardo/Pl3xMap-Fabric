package net.pl3x.map.fabric.pl3xmap.mixin;

import net.minecraft.block.MapColor;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.map.MapState;
import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import net.pl3x.map.fabric.pl3xmap.data.Image;
import net.pl3x.map.fabric.pl3xmap.data.Map;
import net.pl3x.map.fabric.pl3xmap.duck.MapTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.MapTexture.class)
public abstract class MapTextureMixin implements MapTexture {
    @Final
    @Shadow
    private NativeImageBackedTexture texture;
    @Shadow
    private MapState state;

    private final Pl3xMap pl3xmap = Pl3xMap.instance();
    private int id;
    private Map map;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(MapRenderer outer, int id, MapState state, CallbackInfo ci) {
        this.id = id;
        if (pl3xmap.isEnabled() && pl3xmap.isOnServer()) {
            this.pl3xmap.getNetworkManager().requestMapUrl();
        }
    }

    @Inject(method = "updateTexture()V", at = @At("HEAD"), cancellable = true)
    private void injected(CallbackInfo ci) {
        if (this.pl3xmap.canRenderMap()) {
            if (this.map == null) {
                this.pl3xmap.getNetworkManager().requestMapData(this.id);
            } else {
                update();
                ci.cancel();
            }
        }
    }

    @Override
    public Map getMap() {
        return this.map;
    }

    @Override
    public void setMap(Map map) {
        this.map = map;
    }

    private void update() {
        System.out.println("update " + id);
        Image image = this.map.getImage();
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                int drawColor;
                int vanillaColor = this.state.colors[x + z * 128] & 255;
                if (vanillaColor / 4 != 0) {
                    int pl3xColor = image.getPixel(x, z);
                    if (pl3xColor == 0) {
                        drawColor = MapColor.COLORS[vanillaColor / 4].getRenderColor(vanillaColor & 3);
                    } else {
                        drawColor = pl3xColor;
                    }
                } else {
                    drawColor = 0;
                }
                setPixelColor(x, z, drawColor);
            }
        }
        this.texture.upload();
    }

    @SuppressWarnings("ConstantConditions")
    private void setPixelColor(int x, int z, int color) {
        this.texture.getImage().setPixelColor(x, z, color);
    }
}
