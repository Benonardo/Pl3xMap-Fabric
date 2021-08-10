package net.pl3x.map.fabric.configuration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class Lang {
    public static void actionbar(String key, Object... args) {
        send(new TranslatableText(key, args), true);
    }

    public static void chat(String key, Object... args) {
        send(new TranslatableText(key, args), false);
    }

    public static String parse(String key, Object... args) {
        if (I18n.hasTranslation(key)) {
            return I18n.translate(key, args);
        }
        return String.format(key, args);
    }

    public static void send(Text text, boolean actionbar) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(text, actionbar);
        }
    }
}
