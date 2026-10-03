package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.util.FreeCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if <1.21.11 {
/*import net.minecraft.world.level.BlockGetter;
*///? } else if <26.1
//import net.minecraft.world.level.Level;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private Entity entity;
    @Shadow private float eyeHeightOld;
    @Shadow private float eyeHeight;

    // When toggling freecam, update the camera's eye height instantly without any transition.
    //? if >=26.1 {
    @Inject(method = "setEntity", at = @At("HEAD"))
    private void onSetEntity(Entity entity, CallbackInfo ci) {
        if (entity == null || this.entity == null) {
            return;
        }

        if (entity instanceof FreeCamera || this.entity instanceof FreeCamera) {
            this.eyeHeightOld = this.eyeHeight = entity.getEyeHeight();
        }
    }
    //? } else {
    /*@Inject(method = "setup", at = @At("HEAD"))
    //~ if >=1.21.11 'BlockGetter area' -> 'Level level'
    public void onUpdate(Level level, Entity entity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (entity == null || this.entity == null || entity.equals(this.entity)) {
            return;
        }

        if (entity instanceof FreeCamera || this.entity instanceof FreeCamera) {
            this.eyeHeightOld = this.eyeHeight = entity.getEyeHeight();
        }
    }
    *///? }

    // Removes the submersion overlay when underwater, in lava, or powdered snow.
    @WrapMethod(method = "getFluidInCamera")
    public FogType onGetSubmersionType(Operation<FogType> original) {
        if (Freecam.isEnabled() && ModConfig.get().shouldHideSubmersionFog()) {
            return FogType.NONE;
        }
        return original.call();
    }
}
