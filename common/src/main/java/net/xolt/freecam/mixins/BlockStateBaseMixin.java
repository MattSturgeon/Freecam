package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.util.FreeCamera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow public abstract Block getBlock();

    @WrapMethod(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;")
    private VoxelShape onGetCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, Operation<VoxelShape> original) {
        if (context instanceof EntityCollisionContext entityShapeContext
            && entityShapeContext.getEntity()/*? if <1.18 >>*//*.orElse(null)*/ instanceof FreeCamera
            && Freecam.isEnabled()
            && ModConfig.get().ignoreCollisionWith(getBlock())) {
            return Shapes.empty();
        }
        return original.call(level, pos, context);
    }
}
