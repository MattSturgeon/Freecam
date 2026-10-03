package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;

//? if >= 1.21.11 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
//? } else {
/*import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
*///? }
//? if <1.21.11
//import static net.xolt.freecam.Freecam.MC;

//~ if >=1.21.11 EntityRenderer -> AvatarRenderer
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    //? if >= 1.21.11 {
    // Prevent rendering of nametag in inventory screen
    //~ if >= 26.0 'state/CameraRenderState' -> 'state/level/CameraRenderState'
    //~ if >= 26.0 'submitNameTag' -> 'submitNameDisplay'
    @WrapMethod(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V")
    private void onSubmitNameDisplay(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Operation<Void> original) {
        if (Freecam.isEnabled() && state.shadowPieces.isEmpty()) return;
        original.call(state, poseStack, submitNodeCollector, camera);
    }
    //? } else {
    /*@WrapMethod(method = "renderNameTag")
    private void onRenderLabel(
            Entity renderState,
            Component component,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int packedLightCoords,
            //? if >=1.20.6
            float partialTick,
            Operation<Void> original)
   {
        if (Freecam.isEnabled() && !MC.getEntityRenderDispatcher().shouldRenderShadow) return;
        original.call(
            renderState,
            component,
            poseStack,
            multiBufferSource,
            packedLightCoords
            //? if >=1.20.6
            , partialTick
        );
    }
    *///? }
}
