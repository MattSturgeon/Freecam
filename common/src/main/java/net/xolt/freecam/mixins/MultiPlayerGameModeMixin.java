package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.freecam.Freecam.MC;

//? if <=1.18.2 {
/*import net.minecraft.client.multiplayer.ClientLevel;
*///? }

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    // Prevents interacting with blocks when allowInteract is disabled.
    @WrapMethod(method = "useItemOn")
    private InteractionResult onInteractBlock(
        LocalPlayer player,
        //? if <= 1.18.2
        //ClientLevel level,
        InteractionHand hand,
        BlockHitResult hitResult,
        Operation<InteractionResult> original)
    {
        if (freecam$disableInteract()) return InteractionResult.PASS;

        return original.call(
            player,
            //? if <= 1.18.2
            //level,
            hand,
            hitResult
        );
    }

    // Prevents interacting with entities when allowInteract is disabled, and prevents interacting with self.
    @WrapMethod(method = "interact")
    private InteractionResult onInteractEntity(
        Player player,
        Entity entity,
        //? if >=26.1
        EntityHitResult hitResult,
        InteractionHand hand,
        Operation<InteractionResult> original
    ) {
        if (entity == MC.player || freecam$disableInteract()) {
            return InteractionResult.PASS;
        }
        return original.call(
            player,
            entity,
            //? if >=26.1
            hitResult,
            hand
        );
    }

    // Prevents interacting with entities when allowInteract is disabled, and prevents interacting with self.
    //? if <26.1 {
    /*@WrapMethod(method = "interactAt")
    private InteractionResult onInteractEntityAtLocation(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, Operation<InteractionResult> original) {
        return entity == MC.player || freecam$disableInteract()
            ? InteractionResult.PASS
            : original.call(player, entity, hitResult, hand);
    }
    *///? }

    // Prevents attacking self.
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(Player player, Entity target, CallbackInfo ci) {
        if (target == MC.player) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean freecam$disableInteract() {
        return Freecam.isEnabled() && !Freecam.isPlayerControlEnabled() && ModConfig.get().shouldPreventInteractions();
    }
}
