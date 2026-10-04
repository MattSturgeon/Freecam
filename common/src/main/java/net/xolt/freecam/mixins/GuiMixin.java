package net.xolt.freecam.mixins;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

//? if <26.2 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.freecam.Freecam.MC;
*///? }

@Mixin(Gui.class)
public class GuiMixin {
    // Makes HUD correspond to the player rather than the FreeCamera.
    // NOTE: moved to HudMixin in 26.2
    //? if <26.2 {
    /*@WrapMethod(method = "getCameraPlayer")
    private Player wrapGetCameraPlayer(Operation<Player> original) {
        return Freecam.isEnabled() ? MC.player : original.call();
    }
    *///? }

    // Don't render equipped-item overlays while Freecam is active
    // NOTE: moved to HudMixin in 26.2
    //? if <26.2 {
    /*@Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderTextureOverlay(CallbackInfo ci) {
        if (Freecam.isEnabled()) ci.cancel();
    }
    *///? }
}
