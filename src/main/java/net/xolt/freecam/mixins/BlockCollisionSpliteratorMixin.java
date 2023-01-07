package net.xolt.freecam.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.mixins.accessors.AbstractBlockAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockCollisionSpliterator.class)
public class BlockCollisionSpliteratorMixin {

    private Entity entity;

    // Store the provided entity, so we can later check if it is freecam
    @Inject(method = "<init>(Lnet/minecraft/world/CollisionView;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Z)V", at = @At("RETURN"))
    private void onConstructed(CollisionView world, Entity entity, Box box, boolean forEntity, CallbackInfo ci) {
        this.entity = entity;
    }

    // Apply custom block collision rules based on collision mode setting
    @Redirect(method = "computeNext()Lnet/minecraft/util/shape/VoxelShape;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape onGetCollisionShape(BlockState blockState, BlockView world, BlockPos blockPos, ShapeContext context) {
        if (Freecam.isEnabled() && entity != null && entity.equals(Freecam.getFreeCamera())) {
            switch (ModConfig.INSTANCE.collisionMode) {
                case OPAQUE -> {
                    // Don't collide if transparent
                    if(!((AbstractBlockAccessor) blockState.getBlock()).getMaterial().blocksLight()) {
                        return VoxelShapes.empty();
                    }
                }
                case NONE -> {
                    // Don't collide with anything
                    return VoxelShapes.empty();
                }
            }
        }

        return blockState.getCollisionShape(world, blockPos, context);
    }
}
