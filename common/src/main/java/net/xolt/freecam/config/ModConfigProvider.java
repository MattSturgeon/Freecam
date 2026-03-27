package net.xolt.freecam.config;

import net.xolt.freecam.util.SingleInstanceServiceLoader;

public interface ModConfigProvider {

    MCAwareModConfig getConfig();
    void saveConfig();
    void loadConfig();

    static ModConfigProvider instance() {
        return Holder.INSTANCE;
    }

    class Holder {
        private Holder() {}

        private static final ModConfigProvider INSTANCE = SingleInstanceServiceLoader.get(ModConfigProvider.class);
    }
}
