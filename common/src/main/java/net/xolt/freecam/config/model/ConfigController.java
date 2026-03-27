package net.xolt.freecam.config.model;

import net.xolt.freecam.config.ModConfig;

public interface ConfigController<T extends ModConfig> {
    T getConfig();
    T getDefaults();
    void save();
    void load();
}
