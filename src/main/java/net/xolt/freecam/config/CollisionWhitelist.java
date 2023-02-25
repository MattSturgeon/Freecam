package net.xolt.freecam.config;

import net.minecraft.block.*;

import java.util.List;

public class CollisionWhitelist {

    private static final List<Class<? extends Block>> transparentWhitelist = List.of(
            AbstractGlassBlock.class,
            PaneBlock.class,
            BarrierBlock.class
    );

    private static final List<Class<? extends Block>> openableWhitelist = List.of(
            FenceGateBlock.class,
            DoorBlock.class,
            TrapdoorBlock.class
    );

    public static boolean isTransparent(Block block) {
        return isMatch(block, transparentWhitelist);
    }

    public static boolean isOpenable(Block block) {
        return isMatch(block, openableWhitelist);
    }

    private static boolean isMatch(Block block, List<Class<? extends Block>> whitelist) {
        return whitelist.stream().anyMatch(blockClass -> blockClass.isInstance(block));
    }
}
