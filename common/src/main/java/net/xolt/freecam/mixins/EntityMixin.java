package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static net.xolt.freecam.Freecam.MC;

@Mixin(Entity.class)
public class EntityMixin {

    /// Overridden by [LocalPlayerMixin].
    @WrapMethod(method = "getViewXRot")
    protected float onGetViewXRot(float partialTick, Operation<Float> original) {
        return original.call(partialTick); // No-op
    }

    // Makes mouse input rotate the FreeCamera.
    @WrapMethod(method = "turn")
    private void onChangeLookDirection(double rotation, double pitch, Operation<Void> original) {
        if (Freecam.isEnabled() && freecam$this() == MC.player && !Freecam.isPlayerControlEnabled()) {
            Freecam.getFreeCamera().turn(rotation, pitch);
            return;
        }
        original.call(rotation, pitch);
    }

    // Prevents FreeCamera from pushing/getting pushed by entities.
    @WrapMethod(method = "push(Lnet/minecraft/world/entity/Entity;)V")
    private void onPushAwayFrom(Entity entity, Operation<Void> original) {
        if (Freecam.isEnabled() && (entity == Freecam.getFreeCamera() || freecam$this() == Freecam.getFreeCamera())) {
            return;
        }
        original.call(entity);
    }

    // Freezes the player's position if freezePlayer is enabled.
    @WrapMethod(method = "setDeltaMovement(DDD)V")
    private void onSetVelocity(double x, double y, double z, Operation<Void> original) {
        if (freecam$shouldFreeze()) return;
        original.call(x, y, z);
    }

    // Freezes the player's position if freezePlayer is enabled.
    @WrapMethod(method = "moveRelative")
    private void onUpdateVelocity(float speed, Vec3 input, Operation<Void> original) {
        if (freecam$shouldFreeze()) return;
        original.call(speed, input);
    }

    // Freezes the player's position if freezePlayer is enabled.
    @WrapMethod(method = "setPos(DDD)V")
    private void onSetPosition(double x, double y, double z, Operation<Void> original) {
        if (freecam$shouldFreeze()) return;
        original.call(x, y, z);
    }

    // Freezes the player's position if freezePlayer is enabled.
    @WrapMethod(method = "setPosRaw")
    private void onSetPos(double x, double y, double z, Operation<Void> original) {
        if (freecam$shouldFreeze()) return;
        original.call(x, y, z);
    }

    @Unique
    private Entity freecam$this() {
        return (Entity) (Object) this;
    }

    @Unique
    private boolean freecam$shouldFreeze() {
        return Freecam.isEnabled() && freecam$this() == MC.player && freecam$allowFreeze();
    }

    @Unique
    private boolean freecam$allowFreeze() {
        return ModConfig.get().shouldFreezePlayer() && !Freecam.isPlayerControlEnabled();
    }
}
