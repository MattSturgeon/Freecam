package net.xolt.freecam.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.xolt.freecam.Freecam;
import net.fabricmc.api.ModInitializer;

public class FreecamFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Freecam.LOGGER.info("Hello from Fabric!");
    }
}
