package net.xolt.freecam.mixins;

//? if >=26.2 {

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;

import static net.xolt.freecam.Freecam.MC;


@Mixin(Hud.class)
public class HudMixin {
    // Makes HUD correspond to the player rather than the FreeCamera.
    // NOTE: Was in GuiMixin before 26.2
    @WrapMethod(method = "getCameraPlayer")
    private Player wrapGetCameraPlayer(Operation<Player> original) {
        return Freecam.isEnabled() ? MC.player : original.call();
    }

    // Don't render equipped-item overlays while Freecam is active
    // NOTE: Was in GuiMixin before 26.2
    @WrapMethod(method = "extractTextureOverlay")
    private void wrapRenderTextureOverlay(
        GuiGraphicsExtractor graphics,
        Identifier texture,
        float alpha,
        Operation<Void> original)
    {
        if (!Freecam.isEnabled()) original.call(graphics, texture, alpha);
    }
}
//? }
