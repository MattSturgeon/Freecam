package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.GameRenderer;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;

//? if >=26.3 {
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
//? }
//? if >=1.21.11 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
//? } else {
/*import net.minecraft.client.Camera;
*///? }
//? if <1.20.6 {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///? }
//? if <26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.injection.At;
import static net.xolt.freecam.Freecam.MC;
*///? }

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Hide hand in freecam if showHand is disabled
    @WrapMethod(method = "renderItemInHand")
    private void onRenderItemInHand(
        //? if >=26.3 {
        CameraRenderState cameraState,
        PlayerRenderState playerState,
        GpuTextureView depthTextureView,
        //? } else if >=1.20.6 {
        /*//~ if >=1.21.11 Camera -> CameraRenderState
        CameraRenderState cameraState,
        float deltaPartialTick,
        //~ if >=26.1 Matrix4f -> Matrix4fc
        org.joml.Matrix4fc modelViewMatrix,
        *///? } else {
        /*PoseStack poseStack,
        Camera camera,
        float deltaPartialTick,
        *///? }
        Operation<Void> original)
    {
        if (Freecam.isEnabled() && ModConfig.get().shouldHideHand()) return;
        //? if >=26.3 {
        original.call(cameraState, playerState, depthTextureView);
        //? } else if >=1.20.6 {
        /*original.call(cameraState, deltaPartialTick, modelViewMatrix);
        *///? } else {
        /*original.call(poseStack, camera, deltaPartialTick);
        *///? }
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
