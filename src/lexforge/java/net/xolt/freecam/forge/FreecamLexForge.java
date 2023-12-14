package net.xolt.freecam.forge;

import net.xolt.freecam.Freecam;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod("freecam")
public class FreecamLexForge {

    public FreecamLexForge() {
    }

    @SubscribeEvent
    public void onInit(FMLCommonSetupEvent event) {
        Freecam.LOGGER.info("Hello from Forge!");
    }

}
