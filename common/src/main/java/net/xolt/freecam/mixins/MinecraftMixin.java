package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.freecam.config.ModBindings.KEY_TOGGLE;
import static net.xolt.freecam.config.ModBindings.KEY_TRIPOD_RESET;

//? if >=26.1 {
import net.minecraft.world.entity.Entity;
import static net.xolt.freecam.Freecam.MC;
//? }

@Mixin(Minecraft.class)
public class MinecraftMixin {

    // Prevents attacks when allowInteract is disabled.
    @WrapMethod(method = "startAttack")
    //? if >1.17.1 {
    private boolean onDoAttack(Operation<Boolean> original) {
        return !freecam$disableInteract() && original.call();
    }
    //? } else {
    /*private void onDoAttack(Operation<Void> original) {
        if (!freecam$disableInteract()) original.call();
    }
    *///? }

    // Prevents item pick when allowInteract is disabled.
    //~ if >=26.1 pickBlock -> pickBlockOrEntity
    @WrapMethod(method = "pickBlockOrEntity")
    private void onDoItemPick(Operation<Void> original) {
        if (freecam$disableInteract()) return;
        original.call();
    }


    // Makes mouse clicks come from the player rather than the freecam entity when player control is enabled or if interaction mode is set to player.
    // Was GameRenderer#pick before 26.1
    //? if >=26.1 {
    @WrapOperation(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity onGetCameraEntity(Minecraft instance, Operation<Entity> original) {
        if (Freecam.isEnabled() && (Freecam.isPlayerControlEnabled() || ModConfig.get().allowInteractionsFromPlayer())) {
            return MC.player;
        }
        return original.call(instance);
    }
    //? }

    // Prevents block breaking when allowInteract is disabled.
    @WrapMethod(method = "continueAttack")
    private void onHandleBlockBreaking(boolean down, Operation<Void> original) {
        if (freecam$disableInteract()) return;
        original.call(down);
    }

    // Prevents hotbar keys from changing selected slot when freecam key is held
    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 2))
    private boolean onHandleHotbarKeys(KeyMapping instance, Operation<Boolean> original) {
        if (KEY_TOGGLE.get().isDown() || KEY_TRIPOD_RESET.get().isDown()) return false;

        return original.call(instance);
    }

    // Disables freecam if the player disconnects.
    //~ if >=1.20.6 '"clearLevel()V"' -> '"disconnect*"'
    @Inject(method = "disconnect*", at = @At(value = "HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        Freecam.onDisconnect();
    }

    @Unique
    private static boolean freecam$disableInteract() {
        return Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && ModConfig.get().shouldPreventInteractions();
    }
}
