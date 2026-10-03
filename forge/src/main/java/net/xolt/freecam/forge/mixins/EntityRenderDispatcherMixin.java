package net.xolt.freecam.forge.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;

import static net.xolt.freecam.Freecam.MC;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    /**
    Stops the player from rendering in Freecam if showPlayer is disabled.
    Forge includes an additional condition inside LevelRenderer's extractVisibleEntities method,
    causing non-camera LocalPlayers to render
     **/
    @WrapMethod(method = "shouldRender")
    private <E extends Entity> boolean onShouldRender(E entity, Frustum frustum, double camX, double camY, double camZ, Operation<Boolean> original) {
        if (entity == MC.player && Freecam.isEnabled() && ModConfig.get().shouldHidePlayer()) {
            return false;
        }
        return original.call(entity, frustum, camX, camY, camZ);
    }
}
