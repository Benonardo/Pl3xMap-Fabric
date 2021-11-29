package net.pl3x.map.fabric.mixin;

import net.minecraft.block.MapColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapColor.class)
public class MapColorAccessor {

    @Accessor("COLORS")
    public static MapColor[] getColors() {
        throw new AssertionError();
    }

}
