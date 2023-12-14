package net.xolt.freecam.forge;

import net.xolt.freecam.Freecam;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod("freecam")
public class FreecamNeoForge {

    public FreecamNeoForge() {
    }

    @SubscribeEvent
    public void onInit(FMLCommonSetupEvent event) {
        Freecam.LOGGER.info("Hello from Forge!");
    }

}
