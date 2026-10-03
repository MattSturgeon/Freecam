package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.player.LocalPlayer;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static net.xolt.freecam.Freecam.MC;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends EntityMixin {

    // Needed for Baritone compatibility.
    @WrapMethod(method = "isControlledCamera")
    private boolean onIsCamera(Operation<Boolean> original) {
        return Freecam.isEnabled() && freecam$this() == MC.player || original.call();
    }

    // Makes rotation depend upon FreeCamera rather than the player.
    @Override
    protected float onGetViewXRot(float partialTick, Operation<Float> original) {
        if (freecam$useFreecamRotation()) {
            return Freecam.getFreeCamera().getViewXRot(partialTick);
        }
        return super.onGetViewXRot(partialTick, original);
    }

    // Makes rotation depend upon FreeCamera rather than the player.
    @WrapMethod(method = "getViewYRot")
    private float onGetViewYRot(float partialTick, Operation<Float> original) {
        if (freecam$useFreecamRotation()) {
            return Freecam.getFreeCamera().getViewYRot(partialTick);
        }
        return original.call(partialTick);
    }

    @Unique
    private boolean freecam$useFreecamRotation() {
        return Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && !ModConfig.get().allowInteractionsFromPlayer();
    }

    @Unique
    private LocalPlayer freecam$this() {
        return (LocalPlayer) (Object) this;
    }
}
