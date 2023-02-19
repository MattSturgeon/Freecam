package net.xolt.freecam.util;

import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.BarrierBlock;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.List;

public class CollisionWhitelist {

    private static List<Class<? extends Block>> transparentWhitelist = new ArrayList<>();

    static {
        transparentWhitelist.add(AbstractGlassBlock.class);
        transparentWhitelist.add(BarrierBlock.class);
    }

    public static boolean isTransparent(Block block) {
        return transparentWhitelist.stream().anyMatch(blockClass -> blockClass.isInstance(block));
    }
}
