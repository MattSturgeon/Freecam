package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;

//~ if >=26.0 LightTexture -> Lightmap
import net.minecraft.client.renderer.Lightmap;
//? if >=26.1 {
import net.minecraft.client.renderer.state.LightmapRenderState;
//? } else if >=1.19 {
/*import net.minecraft.world.level.dimension.DimensionType;
*///? } else {
/*import net.minecraft.world.level.Level;
*///? }
//? if >=1.21.11 {
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.At;
//? } else {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
*///? }

//~ if >=26.0 LightTexture -> Lightmap
@Mixin(Lightmap.class)
public class FullBrightMixin {

    //? if >=26.1 {
    @WrapOperation(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/LightmapRenderState;brightness:F"))
    private float getBrightness(LightmapRenderState instance, Operation<Float> original) {
        if (Freecam.isEnabled() && ModConfig.get().isFullBrightEnabled()) {
            return 16.0f;
        }
        return original.call(instance);
    }
    //? } else if >=1.21.11 {
    /*@WrapOperation(method = "updateLightTexture", at = @At(value="INVOKE", target="Lnet/minecraft/world/level/dimension/DimensionType;ambientLight()F"))
    private float getBrightness(DimensionType instance, Operation<Float> original) {
        if (Freecam.isEnabled() && ModConfig.get().isFullBrightEnabled()) {
            return 1.0f;
        }
        return original.call(instance);
    }
    *///? } else {
    /*@WrapMethod(method = "getBrightness")
    //~ if >1.18.2 'private void' -> 'private static void'
    //~ if >1.18.2 'Level level' -> 'DimensionType dimensionType'
    private static float onGetBrightness(DimensionType dimensionType, int lightLevel, Operation<Float> original) {
        if (Freecam.isEnabled() && ModConfig.get().isFullBrightEnabled()) {
            return 1.0f;
        }
        //~ if >1.18.2 level -> dimensionType
        return original.call(dimensionType, lightLevel);
    }
    *///? }
}
