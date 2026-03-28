package net.xolt.freecam.config;

import net.minecraft.client.gui.screens.Screen;
import net.xolt.freecam.util.OptionalService;
import net.xolt.freecam.util.OptionalServiceLoader;

import java.util.Optional;

import static net.xolt.freecam.Freecam.MC;

public interface ConfigScreenProvider extends OptionalService {

    Screen getConfigScreen(Screen parent);

    default void openConfigScreen() {
        openConfigScreen(MC.screen);
    }

    default void openConfigScreen(Screen parent) {
        MC.setScreen(getConfigScreen(parent));
    }

    static Optional<ConfigScreenProvider> load() {
        return OptionalServiceLoader.get(ConfigScreenProvider.class);
    }
}
