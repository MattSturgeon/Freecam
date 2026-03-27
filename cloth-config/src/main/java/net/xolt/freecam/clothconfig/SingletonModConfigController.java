package net.xolt.freecam.clothconfig;

import net.xolt.freecam.Freecam;
import net.xolt.freecam.clothconfig.model.JanksonConfigLoader;
import net.xolt.freecam.clothconfig.model.ModConfigModel;
import net.xolt.freecam.config.model.ConfigController;
import net.xolt.freecam.config.model.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class SingletonModConfigController implements ConfigController<ModConfigModel> {

    public static final SingletonModConfigController INSTANCE = new SingletonModConfigController();

    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonModConfigController.class);

    private final ConfigLoader<ModConfigModel> serializer = new JanksonConfigLoader<>(ModConfigModel.class, Freecam.MOD_ID);
    private final List<Consumer<ModConfigModel>> subscribers = new ArrayList<>();
    private final ModConfigModel defaults = new ModConfigModel();
    private ModConfigModel config;

    private SingletonModConfigController() {
        // TODO: split behavior from model
        onSave(ModConfigModel::onConfigChange);
    }

    @Override
    public ModConfigModel getConfig() {
        // Use defaults if config has not been loaded
        return config == null ? defaults : config;
    }

    @Override
    public ModConfigModel getDefaults() {
        return defaults;
    }

    @Override
    public void save() {
        ModConfigModel config = getConfig();
        try {
            serializer.write(config);
        } catch (Exception e) {
            LOGGER.error("Failed to save config", e);
            // TODO: Consider propagating an error to the GUI
            return;
        }
        subscribers.forEach(subscriber -> subscriber.accept(config));
    }

    @Override
    public void load() {
        try {
            config = serializer.read();
        } catch (Exception e) {
            LOGGER.error("Failed to load config, using defaults", e);
            config = new ModConfigModel();
        }
    }

    @Override
    public void onSave(Consumer<ModConfigModel> subscriber) {
        subscribers.add(subscriber);
    }
}
