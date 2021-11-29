package net.pl3x.map.fabric.mixin;

import net.minecraft.client.toast.Toast;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Toast.class)
public interface ToastAccessor {

    @Accessor("TEXTURE")
    static Identifier accessTEXTURE() {
        throw new AssertionError();
    }

}
