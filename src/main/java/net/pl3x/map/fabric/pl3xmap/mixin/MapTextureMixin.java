package net.pl3x.map.fabric.pl3xmap.mixin;

import net.minecraft.block.MapColor;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.map.MapState;
import net.pl3x.map.fabric.pl3xmap.Pl3xMap;
import net.pl3x.map.fabric.pl3xmap.data.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.MapTexture.class)
public abstract class MapTextureMixin {
    @Final
    @Shadow
    private NativeImageBackedTexture texture;
    @Shadow
    private MapState state;

    private int id;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(MapRenderer outer, int id, MapState state, CallbackInfo ci) {
        this.id = id;
        if (Pl3xMap.instance().canRenderMap()) {
            Pl3xMap.instance().getNetworkManager().requestMapUrl();
        }
    }

    @Inject(method = "updateTexture()V", at = @At("HEAD"), cancellable = true)
    private void injected(CallbackInfo ci) {
        if (Pl3xMap.instance().canRenderMap()) {
            Map map = Map.MAPS.get(this.id);
            if (map == null) {
                Pl3xMap.instance().getNetworkManager().requestMapData(this.id);
                return;
            }
            update(map);
            ci.cancel();
        }
    }

    private void update(Map map) {
        Map.Image image = map.getImage();
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
