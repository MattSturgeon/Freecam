package net.xolt.freecam.clothconfig;

import net.minecraft.client.gui.screens.Screen;
import net.xolt.freecam.config.ConfigScreenProvider;
import net.xolt.freecam.config.MCAwareModConfig;
import net.xolt.freecam.config.ModConfigProvider;
import net.xolt.freecam.clothconfig.model.ModConfigModel;
import org.jetbrains.annotations.Nullable;

public class ClothConfigProvider implements ModConfigProvider, ConfigScreenProvider {

    @Override
    public MCAwareModConfig getConfig() {
        return ModConfigModel.INSTANCE;
    }

    @Override
    public void setupConfig() {
        ModConfigModel.load();
    }

    @Override
    public Screen getConfigScreen(@Nullable Screen parent) {
        return ModConfigScreen.getConfigScreen(parent);
    }

}
