package net.xolt.freecam.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;

import static me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;

@Config(name = "freecam")
public class ModConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    public static ModConfig INSTANCE;

    public static void init() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        GuiRegistry registry = AutoConfig.getGuiRegistry(ModConfig.class);
        registry.registerAnnotationProvider(ListenableConfigEntry.provider, ListenableConfigEntry.class);
        registry.registerAnnotationTransformer(ListenableConfigEntry.transformer, ListenableConfigEntry.class);
    }

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
    public FlightMode flightMode = FlightMode.DEFAULT;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
    public Perspective perspective = Perspective.INSIDE;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
    public InteractionMode interactionMode = InteractionMode.CAMERA;

    @ConfigEntry.Gui.Tooltip
    public double horizontalSpeed = 1.0;

    @ConfigEntry.Gui.Tooltip
    public double verticalSpeed = 1.0;

    @ConfigEntry.Gui.Tooltip
    public boolean ignoreTransparentCollision = true;

    @ConfigEntry.Gui.Tooltip
    public boolean ignoreOpenableCollision = true;

    @ConfigEntry.Gui.Tooltip
    @ListenableConfigEntry
    public boolean ignoreAllCollision = true;

    @ConfigEntry.Gui.Tooltip
    public boolean checkCollision = false;

    @ConfigEntry.Gui.Tooltip
    public boolean disableOnDamage = true;

    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean allowInteract = false;

    @ConfigEntry.Gui.Tooltip(count = 2)
    public boolean freezePlayer = false;

    @ConfigEntry.Gui.Tooltip
    public boolean showPlayer = true;

    @ConfigEntry.Gui.Tooltip
    public boolean showHand = false;

    @ConfigEntry.Gui.Tooltip
    public boolean showSubmersion = false;

    @ConfigEntry.Gui.Tooltip
    public boolean notifyFreecam = true;

    @ConfigEntry.Gui.Tooltip
    public boolean notifyTripod = true;

    public enum FlightMode implements SelectionListEntry.Translatable {
        CREATIVE("text.autoconfig.freecam.option.flightMode.creative"),
        DEFAULT("text.autoconfig.freecam.option.flightMode.default");

        private final String name;

        FlightMode(String name) {
            this.name = name;
        }

        public String getKey() {
            return name;
        }
    }

    public enum InteractionMode implements SelectionListEntry.Translatable {
        CAMERA("text.autoconfig.freecam.option.interactionMode.camera"),
        PLAYER("text.autoconfig.freecam.option.interactionMode.player");

        private final String name;

        InteractionMode(String name) {
            this.name = name;
        }

        public String getKey() {
            return name;
        }
    }

    public enum Perspective implements SelectionListEntry.Translatable {
        FIRST_PERSON("text.autoconfig.freecam.option.perspective.firstPerson"),
        THIRD_PERSON("text.autoconfig.freecam.option.perspective.thirdPerson"),
        THIRD_PERSON_MIRROR("text.autoconfig.freecam.option.perspective.thirdPersonMirror"),
        INSIDE("text.autoconfig.freecam.option.perspective.inside");

        private final String name;

        Perspective(String name) {
            this.name = name;
        }

        public String getKey() {
            return name;
        }
    }
}
