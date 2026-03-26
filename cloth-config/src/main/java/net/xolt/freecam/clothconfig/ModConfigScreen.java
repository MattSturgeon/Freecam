package net.xolt.freecam.clothconfig;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.*;
import me.shedaniel.clothconfig2.impl.builders.KeyCodeBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.xolt.freecam.config.model.FlightMode;
import net.xolt.freecam.config.ModBindings;
import net.xolt.freecam.clothconfig.model.ModConfigModel;
import net.xolt.freecam.config.model.Perspective;

import java.util.stream.Stream;

public class ModConfigScreen {

    // ClothConfig doesn't have built-in double sliders, so scale up long sliders
    // UI displays 2dp, so scale by 100x
    private static final double SLIDER_SCALE = 100.0;
    private static final double MIN_SPEED = 0.0;
    private static final double MAX_SPEED = 10.0;

    public static Screen getConfigScreen(Screen parent) {
        return builder().setParentScreen(parent).build();
    }

    /**
     * Construct a {@link ConfigBuilder} using our custom config & GUI screen.
     */
    public static ConfigBuilder builder() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Component.translatable("text.autoconfig.freecam.title"))
                // .setGlobalized(true) // Adds a sidebar menu
                .transparentBackground()
                .setSavingRunnable(ModConfigModel.INSTANCE::save);

        // Shared entry builder instance
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Create & populate the "default" top-level category
        // This mutates the builder by adding a new "category" to it
        ConfigCategory category = defaultCategory(builder, entryBuilder);

        return builder;
    }

    /**
     * Creates the default category.
     * <p>
     * Note: if multiple categories are registered, they'll show as tabs.
     * Currently we use sub-category list-entries instead, which show as collapsible menus.
     */
    private static ConfigCategory defaultCategory(ConfigBuilder configBuilder, ConfigEntryBuilder entryBuilder) {
        ConfigCategory category = configBuilder.getOrCreateCategory(Component.empty());

        // Add entries to the category
        // Currently this will be sub-categories
        Stream.of(
                controlsCategory(entryBuilder),
                movementCategory(entryBuilder),
                collisionCategory(entryBuilder),
                visualCategory(entryBuilder),
                utilityCategory(entryBuilder),
                serversCategory(entryBuilder),
                notificationCategory(entryBuilder)
        ).forEach(category::addEntry);

        return category;
    }

    /**
     * @param entryBuilder {@link ConfigEntryBuilder cloth-config entry builder}
     * @return the controls sub-category
     */
    private static SubCategoryListEntry controlsCategory(ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder builder = entryBuilder.startSubCategory(Component.translatable("text.autoconfig.freecam.option.controls"))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.controls.@Tooltip"));

        // Add a KeyCodeEntry for each binding in ModBindings
        ModBindings.stream()
                .map(bind -> entryBuilder.fillKeybindingField(Component.translatable(bind.getName()), bind))
                .map(KeyCodeBuilder::build)
                .forEach(builder::add);

        return builder.build();
    }

    /**
     * @param entryBuilder {@link ConfigEntryBuilder cloth-config entry builder}
     * @return the movement sub-category
     */
    private static SubCategoryListEntry movementCategory(ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder builder = entryBuilder.startSubCategory(Component.translatable("text.autoconfig.freecam.option.movement"))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.movement.@Tooltip"));

        EnumListEntry<FlightMode> flightMode = entryBuilder.startEnumSelector(
                        Component.translatable("text.autoconfig.freecam.option.movement.flightMode"),
                        FlightMode.class,
                        ModConfigModel.INSTANCE.movement.flightMode)
                .setEnumNameProvider(value -> Component.translatable("text.autoconfig.freecam.option.movement.flightMode." + value.name().toLowerCase()))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.movement.flightMode.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.movement.flightMode)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.movement.flightMode = value)
                .build();

        LongSliderEntry horizontalSpeed = entryBuilder.startLongSlider(
                        Component.translatable("text.autoconfig.freecam.option.movement.horizontalSpeed"),
                        toSlider(ModConfigModel.INSTANCE.movement.horizontalSpeed),
                        toSlider(MIN_SPEED), toSlider(MAX_SPEED))
                .setDefaultValue(() -> toSlider(ModConfigModel.DEFAULTS.movement.horizontalSpeed))
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.movement.horizontalSpeed = fromSlider(value))
                .setTextGetter(value -> Component.translatable("text.autoconfig.freecam.option.movement.speedValue", fromSlider(value)))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.movement.horizontalSpeed.@Tooltip"))
                .build();

        LongSliderEntry verticalSpeed = entryBuilder.startLongSlider(
                        Component.translatable("text.autoconfig.freecam.option.movement.verticalSpeed"),
                        toSlider(ModConfigModel.INSTANCE.movement.verticalSpeed),
                        toSlider(MIN_SPEED), toSlider(MAX_SPEED))
                .setDefaultValue(() -> toSlider(ModConfigModel.DEFAULTS.movement.verticalSpeed))
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.movement.verticalSpeed = fromSlider(value))
                .setTextGetter(value -> Component.translatable("text.autoconfig.freecam.option.movement.speedValue", fromSlider(value)))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.movement.verticalSpeed.@Tooltip"))
                .build();

        // Add entries to the sub-category
        Stream.of(
                flightMode,
                horizontalSpeed,
                verticalSpeed
        ).forEach(builder::add);

        return builder.build();
    }

    /**
     * @param entryBuilder {@link ConfigEntryBuilder cloth-config entry builder}
     * @return the collision sub-category
     */
    private static SubCategoryListEntry collisionCategory(ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder builder = entryBuilder.startSubCategory(Component.translatable("text.autoconfig.freecam.option.collision"))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.collision.@Tooltip"));

        BooleanListEntry ignoreAll = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.collision.ignoreAll"),
                        ModConfigModel.INSTANCE.collision.ignoreAll)
                .setTooltip(
                        Component.translatable("text.autoconfig.freecam.option.collision.ignoreAll.@Tooltip[0]"),
                        Component.translatable("text.autoconfig.freecam.option.collision.ignoreAll.@Tooltip[1]")
                )
                .setDefaultValue(ModConfigModel.DEFAULTS.collision.ignoreAll)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.collision.ignoreAll = value)
                .build();

        BooleanListEntry ignoreTransparent = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.collision.ignoreTransparent"),
                        ModConfigModel.INSTANCE.collision.ignoreTransparent)
                .setRequirement(() -> !ignoreAll.getValue())
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.collision.ignoreTransparent.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.collision.ignoreTransparent)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.collision.ignoreTransparent = value)
                .build();

        BooleanListEntry ignoreOpenable = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.collision.ignoreOpenable"),
                        ModConfigModel.INSTANCE.collision.ignoreOpenable)
                .setRequirement(() -> !ignoreAll.getValue())
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.collision.ignoreOpenable.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.collision.ignoreOpenable)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.collision.ignoreOpenable = value)
                .build();

        BooleanListEntry ignoreCustom = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.collision.ignoreCustom"),
                        ModConfigModel.INSTANCE.collision.ignoreCustom)
                .setRequirement(() -> !ignoreAll.getValue())
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.collision.ignoreCustom.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.collision.ignoreCustom)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.collision.ignoreCustom = value)
                .build();

        StringListListEntry idWhitelist = entryBuilder.startStrList(
                        Component.translatable("text.autoconfig.freecam.option.collision.whitelist.ids"),
                        ModConfigModel.INSTANCE.collision.whitelist.ids)
                .setTooltip(
                        Component.translatable("text.autoconfig.freecam.option.collision.whitelist.ids.@Tooltip[0]"),
                        Component.translatable("text.autoconfig.freecam.option.collision.whitelist.ids.@Tooltip[1]"))
                .setDisplayRequirement(ignoreCustom::getValue)
                .setDefaultValue(ModConfigModel.DEFAULTS.collision.whitelist.ids)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.collision.whitelist.ids = value)
                .build();

        StringListListEntry patternWhitelist = entryBuilder.startStrList(
                        Component.translatable("text.autoconfig.freecam.option.collision.whitelist.patterns"),
                        ModConfigModel.INSTANCE.collision.whitelist.patterns)
                .setTooltip(
                        Component.translatable("text.autoconfig.freecam.option.collision.whitelist.patterns.@Tooltip[0]"),
                        Component.translatable("text.autoconfig.freecam.option.collision.whitelist.patterns.@Tooltip[1]"))
                .setDisplayRequirement(ignoreCustom::getValue)
                .setDefaultValue(ModConfigModel.DEFAULTS.collision.whitelist.patterns)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.collision.whitelist.patterns = value)
                .build();

        BooleanListEntry alwaysCheck = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.collision.alwaysCheck"),
                        ModConfigModel.INSTANCE.collision.alwaysCheck)
                .setTooltip(
                        Component.translatable("text.autoconfig.freecam.option.collision.alwaysCheck.@Tooltip[0]"),
                        Component.translatable("text.autoconfig.freecam.option.collision.alwaysCheck.@Tooltip[1]"))
                .setDefaultValue(ModConfigModel.DEFAULTS.collision.alwaysCheck)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.collision.alwaysCheck = value)
                .build();

        // Add entries to the sub-category
        Stream.of(
                ignoreTransparent,
                ignoreOpenable,
                ignoreCustom,
                idWhitelist,
                patternWhitelist,
                ignoreAll,
                alwaysCheck
        ).forEach(builder::add);

        return builder.build();
    }

    /**
     * @param entryBuilder {@link ConfigEntryBuilder cloth-config entry builder}
     * @return the visual sub-category
     */
    private static SubCategoryListEntry visualCategory(ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder builder = entryBuilder.startSubCategory(Component.translatable("text.autoconfig.freecam.option.visual"))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.visual.@Tooltip"));

        EnumListEntry<Perspective> perspective = entryBuilder.startEnumSelector(
                        Component.translatable("text.autoconfig.freecam.option.visual.perspective"),
                        Perspective.class,
                        ModConfigModel.INSTANCE.visual.perspective)
                .setEnumNameProvider(value -> {
                    String key = switch ((Perspective) value) {
                        case FIRST_PERSON -> "firstPerson";
                        case THIRD_PERSON -> "thirdPerson";
                        case THIRD_PERSON_MIRROR -> "thirdPersonMirror";
                        case INSIDE -> "inside";
                    };
                    return Component.translatable("text.autoconfig.freecam.option.visual.perspective." + key);
                })
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.visual.perspective.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.visual.perspective)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.visual.perspective = value)
                .build();

        BooleanListEntry showPlayer = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.visual.showPlayer"),
                        ModConfigModel.INSTANCE.visual.showPlayer)
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.visual.showPlayer.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.visual.showPlayer)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.visual.showPlayer = value)
                .build();

        BooleanListEntry showHand = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.visual.showHand"),
                        ModConfigModel.INSTANCE.visual.showHand)
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.visual.showHand.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.visual.showHand)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.visual.showHand = value)
                .build();

        BooleanListEntry fullBright = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.visual.fullBright"),
                        ModConfigModel.INSTANCE.visual.fullBright)
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.visual.fullBright.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.visual.fullBright)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.visual.fullBright = value)
                .build();

        BooleanListEntry showSubmersion = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.visual.showSubmersion"),
                        ModConfigModel.INSTANCE.visual.showSubmersion)
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.visual.showSubmersion.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.visual.showSubmersion)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.visual.showSubmersion = value)
                .build();

        // Add entries to the sub-category
        Stream.of(
                perspective,
                showPlayer,
                showHand,
                fullBright,
                showSubmersion
        ).forEach(builder::add);

        return builder.build();
    }

    /**
     * @param entryBuilder {@link ConfigEntryBuilder cloth-config entry builder}
     * @return the utility sub-category
     */
    private static SubCategoryListEntry utilityCategory(ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder builder = entryBuilder.startSubCategory(Component.translatable("text.autoconfig.freecam.option.utility"))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.utility.@Tooltip"));

        BooleanListEntry disableOnDamage = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.utility.disableOnDamage"),
                        ModConfigModel.INSTANCE.utility.disableOnDamage)
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.utility.disableOnDamage.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.utility.disableOnDamage)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.utility.disableOnDamage = value)
                .build();

        BooleanListEntry freezePlayer = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.utility.freezePlayer"),
                        ModConfigModel.INSTANCE.utility.freezePlayer)
                .setTooltip(
                        Component.translatable("text.autoconfig.freecam.option.utility.freezePlayer.@Tooltip[0]"),
                        Component.translatable("text.autoconfig.freecam.option.utility.freezePlayer.@Tooltip[1]")
                )
                .setDefaultValue(ModConfigModel.DEFAULTS.utility.freezePlayer)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.utility.freezePlayer = value)
                .build();

        BooleanListEntry allowInteract = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.utility.allowInteract"),
                        ModConfigModel.INSTANCE.utility.allowInteract)
                .setTooltip(
                        Component.translatable("text.autoconfig.freecam.option.utility.allowInteract.@Tooltip[0]"),
                        Component.translatable("text.autoconfig.freecam.option.utility.allowInteract.@Tooltip[1]")
                )
                .setDefaultValue(ModConfigModel.DEFAULTS.utility.allowInteract)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.utility.allowInteract = value)
                .build();

        EnumListEntry<ModConfigModel.InteractionMode> interactionMode = entryBuilder.startEnumSelector(
                        Component.translatable("text.autoconfig.freecam.option.utility.interactionMode"),
                        ModConfigModel.InteractionMode.class,
                        ModConfigModel.INSTANCE.utility.interactionMode)
                .setEnumNameProvider(value -> Component.translatable("text.autoconfig.freecam.option.utility.interactionMode." + value.name().toLowerCase()))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.utility.interactionMode.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.utility.interactionMode)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.utility.interactionMode = value)
                .build();

        // Add entries to the sub-category
        Stream.of(
                disableOnDamage,
                freezePlayer,
                allowInteract,
                interactionMode
        ).forEach(builder::add);

        return builder.build();
    }

    /**
     * @param entryBuilder {@link ConfigEntryBuilder cloth-config entry builder}
     * @return the servers sub-category
     */
    private static SubCategoryListEntry serversCategory(ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder builder = entryBuilder.startSubCategory(Component.translatable("text.autoconfig.freecam.option.servers"))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.servers.@Tooltip"));

        EnumListEntry<ModConfigModel.ServerRestriction> mode = entryBuilder.startEnumSelector(
                        Component.translatable("text.autoconfig.freecam.option.servers.mode"),
                        ModConfigModel.ServerRestriction.class,
                        ModConfigModel.INSTANCE.servers.mode)
                .setEnumNameProvider(value -> Component.translatable("text.autoconfig.freecam.option.servers.mode." + value.name().toLowerCase()))
                .setTooltip(
                        Component.translatable("text.autoconfig.freecam.option.servers.mode.@Tooltip[0]"),
                        Component.translatable("text.autoconfig.freecam.option.servers.mode.@Tooltip[1]"))
                .setDefaultValue(ModConfigModel.DEFAULTS.servers.mode)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.servers.mode = value)
                .build();

        @SuppressWarnings("UnstableApiUsage")
        StringListListEntry whitelist = entryBuilder.startStrList(
                        Component.translatable("text.autoconfig.freecam.option.servers.whitelist"),
                        ModConfigModel.INSTANCE.servers.whitelist)
                .setDisplayRequirement(() -> mode.getValue() == ModConfigModel.ServerRestriction.WHITELIST)
                .setDefaultValue(ModConfigModel.DEFAULTS.servers.whitelist)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.servers.whitelist = value)
                .build();

        @SuppressWarnings("UnstableApiUsage")
        StringListListEntry blacklist = entryBuilder.startStrList(
                        Component.translatable("text.autoconfig.freecam.option.servers.blacklist"),
                        ModConfigModel.INSTANCE.servers.blacklist)
                .setDisplayRequirement(() -> mode.getValue() == ModConfigModel.ServerRestriction.BLACKLIST)
                .setDefaultValue(ModConfigModel.DEFAULTS.servers.blacklist)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.servers.blacklist = value)
                .build();

        // Add entries to the sub-category
        Stream.of(
                mode,
                whitelist,
                blacklist
        ).forEach(builder::add);

        return builder.build();
    }

    /**
     * @param entryBuilder {@link ConfigEntryBuilder cloth-config entry builder}
     * @return the notification sub-category
     */
    private static SubCategoryListEntry notificationCategory(ConfigEntryBuilder entryBuilder) {
        SubCategoryBuilder builder = entryBuilder.startSubCategory(Component.translatable("text.autoconfig.freecam.option.notification"))
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.notification.@Tooltip"));

        BooleanListEntry notifyFreecam = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.notification.notifyFreecam"),
                        ModConfigModel.INSTANCE.notification.notifyFreecam)
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.notification.notifyFreecam.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.notification.notifyFreecam)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.notification.notifyFreecam = value)
                .build();

        BooleanListEntry notifyTripod = entryBuilder.startBooleanToggle(
                        Component.translatable("text.autoconfig.freecam.option.notification.notifyTripod"),
                        ModConfigModel.INSTANCE.notification.notifyTripod)
                .setTooltip(Component.translatable("text.autoconfig.freecam.option.notification.notifyTripod.@Tooltip"))
                .setDefaultValue(ModConfigModel.DEFAULTS.notification.notifyTripod)
                .setSaveConsumer(value -> ModConfigModel.INSTANCE.notification.notifyTripod = value)
                .build();

        // Add entries to the sub-category
        Stream.of(
                notifyFreecam,
                notifyTripod
        ).forEach(builder::add);

        return builder.build();
    }

    private static long toSlider(double value) {
        return (long) (value * SLIDER_SCALE);
    }

    private static double fromSlider(long value) {
        return value / SLIDER_SCALE;
    }
}
