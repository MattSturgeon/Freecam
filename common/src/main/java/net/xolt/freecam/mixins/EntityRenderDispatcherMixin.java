package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.util.FreeCamera;
import org.spongepowered.asm.mixin.Mixin;

import static net.xolt.freecam.Freecam.MC;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    // Prevents shadow being cast when Iris is enabled.
    @WrapMethod(method = "shouldRender")
    private boolean onShouldRender(
        Entity entity,
        Frustum culler,
        double camX,
        double camY,
        double camZ,
        //? if >=26.3
        float partialTicks,
        Operation<Boolean> original)
    {
        if (entity instanceof FreeCamera) {
            return false;
        } else if (entity == MC.player && Freecam.isEnabled() && ModConfig.get().shouldHidePlayer()) {
            return false;
        }

        return original.call(entity, culler, camX, camY, camZ /*? if >=26.3 >>')' */, partialTicks);
    }
}
