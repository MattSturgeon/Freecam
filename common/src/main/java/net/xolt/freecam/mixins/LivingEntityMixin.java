package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.LivingEntity;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.freecam.Freecam.MC;
import static net.xolt.freecam.config.model.FlightMode.CREATIVE;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow public abstract float getHealth();

    // Allows for the horizontal speed of creative flight to be configured separately from vertical speed.
    @WrapMethod(method = "getFrictionInfluencedSpeed")
    private float onGetMovementSpeed(float friction, Operation<Float> original) {
        if (Freecam.isEnabled() && ModConfig.get().getFlightMode().equals(CREATIVE) && freecam$this() == Freecam.getFreeCamera()) {
            return (float) (ModConfig.get().getHorizontalSpeed() / 10) * (Freecam.getFreeCamera().isSprinting() ? 2 : 1);
        }
        return original.call(friction);
    }

    // Disables freecam upon receiving damage if disableOnDamage is enabled.
    @Inject(method = "setHealth", at = @At("HEAD"))
    private void onSetHealth(float health, CallbackInfo ci) {
        if (Freecam.isEnabled() && ModConfig.get().shouldDisableOnDamage() && freecam$this() == MC.player) {
            if (!MC.player.isCreative() && getHealth() > health) {
                Freecam.disableNextTick();
            }
        }
    }

    @Unique
    private LivingEntity freecam$this() {
        return (LivingEntity) (Object) this;
    }
}
