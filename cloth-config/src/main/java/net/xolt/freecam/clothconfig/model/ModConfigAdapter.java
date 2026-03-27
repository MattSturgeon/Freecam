package net.xolt.freecam.clothconfig.model;

import net.minecraft.world.level.block.Block;
import net.xolt.freecam.config.MCAwareModConfig;
import net.xolt.freecam.config.model.ConfigController;
import net.xolt.freecam.config.model.FlightMode;
import net.xolt.freecam.config.model.Perspective;

public class ModConfigAdapter implements MCAwareModConfig {

    private final ModConfigModel config;
    private final CollisionBehavior collisionBehavior;

    public ModConfigAdapter(ModConfigModel config) {
        this(config, new CollisionBehavior(config.collision));
    }

    ModConfigAdapter(ModConfigModel config, CollisionBehavior collisionBehavior) {
        this.config = config;
        this.collisionBehavior = collisionBehavior;
    }

    public ModConfigModel getModel() {
        return config;
    }

    @Override
    public FlightMode getFlightMode() {
        return config.movement.flightMode;
    }

    @Override
    public double getHorizontalSpeed() {
        return config.movement.horizontalSpeed;
    }

    @Override
    public double getVerticalSpeed() {
        return config.movement.verticalSpeed;
    }

    @Override
    public boolean ignoreAllCollision() {
        return config.collision.ignoreAll;
    }

    @Override
    public boolean shouldCheckInitialCollision() {
        return config.collision.alwaysCheck || !config.collision.ignoreAll;
    }

    @Override
    public boolean ignoreCollisionWith(Block block) {
        return config.collision.ignoreAll || collisionBehavior.isIgnored(block);
    }

    @Override
    public Perspective getInitialPerspective() {
        return config.visual.perspective;
    }

    @Override
    public boolean shouldShowPlayer() {
        return config.visual.showPlayer;
    }

    @Override
    public boolean shouldShowHand() {
        return config.visual.showHand;
    }

    @Override
    public boolean isFullBrightEnabled() {
        return config.visual.fullBright;
    }

    @Override
    public boolean shouldShowSubmersionFog() {
        return config.visual.showSubmersion;
    }

    @Override
    public boolean shouldDisableOnDamage() {
        return config.utility.disableOnDamage;
    }

    @Override
    public boolean shouldFreezePlayer() {
        return config.utility.freezePlayer;
    }

    @Override
    public boolean shouldPreventInteractions() {
        return !config.utility.allowInteract;
    }

    public boolean allowInteractionsFrom(ModConfigModel.InteractionMode mode) {
        return config.utility.allowInteract && config.utility.interactionMode == mode;
    }

    @Override
    public boolean allowInteractionsFromCamera() {
        return allowInteractionsFrom(ModConfigModel.InteractionMode.CAMERA);
    }

    @Override
    public boolean allowInteractionsFromPlayer() {
        return allowInteractionsFrom(ModConfigModel.InteractionMode.PLAYER);
    }

    @Override
    public boolean isRestrictedOnServer(String serverIp) {
        return switch (config.servers.mode) {
            case NONE -> false;
            case WHITELIST -> {
                String ip = serverIp.trim().toLowerCase();
                yield config.servers.whitelist.stream()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .noneMatch(ip::equals);
            }
            case BLACKLIST -> {
                String ip = serverIp.trim().toLowerCase();
                yield config.servers.blacklist.stream()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .anyMatch(ip::equals);
            }
        };
    }

    @Override
    public boolean shouldNotifyFreecam() {
        return config.notification.notifyFreecam;
    }

    @Override
    public boolean shouldNotifyTripod() {
        return config.notification.notifyTripod;
    }
}