package net.pl3x.map.fabric.pl3xmap.configuration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.commons.lang3.BooleanUtils;

public class Lang {
    public static void send(String message) {
        send(message, false);
    }

    public static void send(Text message) {
        send(message, false);
    }

    public static void send(String message, boolean actionbar) {
        send(Text.of(message), actionbar);
    }

    public static void send(Text message, boolean actionbar) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(message, actionbar);
        }
    }

    public static String onOff(boolean bool) {
        return BooleanUtils.toStringOnOff(bool);
    }
}
