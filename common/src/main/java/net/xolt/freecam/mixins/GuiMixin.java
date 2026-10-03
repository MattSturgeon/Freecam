package net.xolt.freecam.mixins;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

//? if <26.2 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.xolt.freecam.Freecam;

//? if >=1.20.6 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? } else if > 1.18.2 {
/^import com.mojang.blaze3d.vertex.PoseStack;
^///? }

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
    /*@WrapMethod(method = "extractTextureOverlay")
    private void wrapRenderTextureOverlay(
        //? if >=1.20.6 {
        GuiGraphicsExtractor graphics,
        //? } else if > 1.18.2
        //PoseStack poseStack,
        Identifier texture,
        float alpha,
        Operation<Void> original)
    {
        if (Freecam.isEnabled()) return;

        original.call(
            //? if >=1.20.6 {
            graphics,
            //? } else if > 1.18.2
            //poseStack,
            texture,
            alpha);
    }
    *///? }
}
