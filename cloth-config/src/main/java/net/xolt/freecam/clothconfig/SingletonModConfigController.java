package net.xolt.freecam.clothconfig;

import net.xolt.freecam.Freecam;
import net.xolt.freecam.clothconfig.model.JanksonConfigLoader;
import net.xolt.freecam.clothconfig.model.ModConfigAdapter;
import net.xolt.freecam.clothconfig.model.ModConfigModel;
import net.xolt.freecam.config.model.ConfigController;
import net.xolt.freecam.config.model.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SingletonModConfigController implements ConfigController<ModConfigAdapter> {

    public static final SingletonModConfigController INSTANCE = new SingletonModConfigController();

    private static final Logger LOGGER = LoggerFactory.getLogger(SingletonModConfigController.class);

    private final ConfigLoader<ModConfigModel> serializer = new JanksonConfigLoader<>(ModConfigModel.class, Freecam.MOD_ID);
    private final ModConfigAdapter defaults = new ModConfigAdapter(new ModConfigModel());
    private ModConfigAdapter config = new ModConfigAdapter(new ModConfigModel());

    private SingletonModConfigController() {}

    @Override
    public ModConfigAdapter getConfig() {
        return config;
    }

    @Override
    public ModConfigAdapter getDefaults() {
        return defaults;
    }

    @Override
    public void save() {
        ModConfigModel modified = getConfig().getModel();
        try {
            serializer.write(modified);
        } catch (Exception e) {
            LOGGER.error("Failed to save config", e);
            // TODO: Consider propagating an error to the GUI
            return;
        }
        config = new ModConfigAdapter(modified);
    }

    @Override
    public void load() {
        ModConfigModel model;
        try {
            model = serializer.read();
        } catch (Exception e) {
            LOGGER.error("Failed to load config, using defaults", e);
            model = new ModConfigModel();
        }
        config = new ModConfigAdapter(model);
    }
}
