package net.xolt.freecam.config;

import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiTransformer;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import net.minecraft.text.Text;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@interface ListenableConfigEntry {
    GuiTransformer transformer = (list, translationKey, field, o, o1, guiRegistryAccess) -> {
        System.err.println("Found match (transformer): " + field.getName());
        return list;
    };
    GuiProvider provider = (s, field, o, o1, guiRegistryAccess) -> {
        System.err.println("Found match (provider): " + field.getName());
        try {
            BooleanToggleBuilder builder = new BooleanToggleBuilder(Text.translatable("reset"), Text.translatable(s), field.getBoolean(o));
            BooleanListEntry entry = builder.build();

            return List.of(entry);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    };
}
