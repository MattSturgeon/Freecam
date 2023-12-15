package net.xolt.freecam.forge;

import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;

@Mod(Freecam.MOD_ID)
public class FreecamNeoForge {
    public FreecamNeoForge() {
        // Register our config screen with Forge
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) ->
                AutoConfig.getConfigScreen(ModConfig.class, parent).get()
        ));

        // Call our init
        Freecam.init();
    }
}
