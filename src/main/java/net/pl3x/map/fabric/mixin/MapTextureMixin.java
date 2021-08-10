package net.pl3x.map.fabric.mixin;

import net.minecraft.block.MapColor;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.map.MapState;
import net.pl3x.map.fabric.Pl3xMap;
import net.pl3x.map.fabric.duck.MapTexture;
import net.pl3x.map.fabric.tiles.Tile;
import net.pl3x.map.fabric.util.Image;
import net.pl3x.map.fabric.util.World;
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
    @Shadow
    private boolean needsUpdate;

    private final Pl3xMap pl3xmap = Pl3xMap.instance();
    private final Image image = new Image(128);

    private int id;
    private byte scale;
    private int centerX;
    private int centerZ;
    private World world;
    private boolean ready;
    private boolean skip;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injected(MapRenderer outer, int id, MapState state, CallbackInfo ci) {
        this.id = id;
    }

    @Inject(method = "updateTexture()V", at = @At("HEAD"), cancellable = true)
    private void injected(CallbackInfo ci) {
        if (this.pl3xmap.rendererEnabled() && this.pl3xmap.getServerManager().isOnServer() && this.pl3xmap.getServerManager().getUrl() != null && !this.skip) {
            if (!this.ready) {
                this.pl3xmap.getNetworkManager().requestMapData(id);
                this.skip = true;
                return;
            }
            updateMapTexture();
            ci.cancel();
        }
    }

    @Override
    public void skip() {
        this.ready = true;
        this.skip = true;
    }

    @Override
    public void setData(byte scale, int centerX, int centerZ, World world) {
        this.scale = scale;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.world = world;
        this.ready = true;
        this.skip = false;
        updateImage();
    }

    @Override
    public void updateImage() {
        if (!this.ready) {
            return;
        }
        int mod = 1 << this.scale;
        int startX = (this.centerX / mod - 64) * mod;
        int startZ = (this.centerZ / mod - 64) * mod;
        for (int x = 0; x < 128; x++) {
            for (int z = 0; z < 128; z++) {
                int blockX = startX + (x * mod) + this.scale;
                int blockZ = startZ + (z * mod) + this.scale;
                Tile tile = this.pl3xmap.getTileManager().get(this.world, blockX >> 9, blockZ >> 9);
                this.image.setPixel(x, z, tile == null ? 0 : tile.getImage().getPixel(blockX & 511, blockZ & 511));
            }
        }
        this.needsUpdate = true;
    }

    private void updateMapTexture() {
        if (!this.pl3xmap.getConfig().getRenderer().getFogOfWar()) {
            for (int x = 0; x < 128; x++) {
                for (int z = 0; z < 128; z++) {
                    setPixelColor(x, z, this.image.getPixel(x, z));
                }
            }
        } else {
            int color;
            int pl3xColor;
            for (int x = 0; x < 128; x++) {
                for (int z = 0; z < 128; z++) {
                    color = this.state.colors[x + z * 128] & 255;
                    if (color / 4 == 0) {
                        setPixelColor(x, z, 0);
                    } else {
                        pl3xColor = this.image.getPixel(x, z);
                        if (pl3xColor == 0) {
                            setPixelColor(x, z, MapColor.COLORS[color / 4].getRenderColor(color & 3));
                        } else {
                            setPixelColor(x, z, pl3xColor);
                        }
                    }
                }
            }
        }
        this.texture.upload();
    }

    @SuppressWarnings("ConstantConditions")
    private void setPixelColor(int x, int z, int color) {
        this.texture.getImage().setPixelColor(x, z, color);
    }
}
