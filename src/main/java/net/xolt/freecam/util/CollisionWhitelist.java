package net.xolt.freecam.util;

import net.minecraft.block.*;

import java.util.ArrayList;
import java.util.List;

public class CollisionWhitelist {

    private static List<Class<? extends Block>> transparentWhitelist = new ArrayList<>();
    private static List<Class<? extends Block>> openableWhitelist = new ArrayList<>();

    static {
        transparentWhitelist.add(AbstractGlassBlock.class);
        transparentWhitelist.add(PaneBlock.class);
        transparentWhitelist.add(BarrierBlock.class);

        openableWhitelist.add(FenceGateBlock.class);
        openableWhitelist.add(DoorBlock.class);
        openableWhitelist.add(TrapdoorBlock.class);
    }

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
