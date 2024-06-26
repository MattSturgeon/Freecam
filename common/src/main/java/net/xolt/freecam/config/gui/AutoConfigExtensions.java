package net.xolt.freecam.config.gui;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.gui.DefaultGuiProviders;
import me.shedaniel.autoconfig.gui.DefaultGuiTransformers;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

/**
 * Extensions and modifications to AutoConfig.
 *
 * @see DefaultGuiProviders
 * @see DefaultGuiTransformers
 */
public class AutoConfigExtensions {
    static final Component RESET_TEXT = Component.translatable("text.cloth-config.reset_value");
    static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

    private AutoConfigExtensions() {}

    public static void apply(Class<? extends ConfigData> configClass) {
        GuiRegistry registry = AutoConfig.getGuiRegistry(configClass);
        ModBindingsConfigImpl.apply(registry);
        VariantTooltipImpl.apply(registry);
        BoundedContinuousImpl.apply(registry);
    }
}
