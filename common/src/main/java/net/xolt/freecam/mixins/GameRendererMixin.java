package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.GameRenderer;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import static net.xolt.freecam.Freecam.MC;
*///? }

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Hide hand in freecam if showHand is disabled
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void onRenderItemInHand(CallbackInfo ci) {
        if (Freecam.isEnabled() && ModConfig.get().shouldHideHand()) {
            ci.cancel();
        }
    }

    // Disables block outlines when allowInteract is disabled.
    @WrapMethod(method = "shouldRenderBlockOutline")
    private boolean onShouldRenderBlockOutline(Operation<Boolean> original) {
        if (Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && ModConfig.get().shouldPreventInteractions()) {
            return false;
        }
        return original.call();
    }

    // Makes mouse clicks come from the player rather than the freecam entity when player control is enabled or if interaction mode is set to player.
    // Moved to Minecraft#pick in 26.1
    //? if <26.1 {
    /*@WrapOperation(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()Lnet/minecraft/world/entity/Entity;"))
    private Entity onGetCameraEntity(Minecraft instance, Operation<Entity> original) {
        if (Freecam.isEnabled() && (Freecam.isPlayerControlEnabled() || ModConfig.get().allowInteractionsFromPlayer())) {
            return MC.player;
        }
        return original.call(instance);
    }
    *///? }
}
