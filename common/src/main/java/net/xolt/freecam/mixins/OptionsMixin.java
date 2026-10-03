package net.xolt.freecam.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import net.xolt.freecam.Freecam;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Options.class)
public class OptionsMixin {

    // Prevents switching to third person in freecam.
    @WrapMethod(method = "setCameraType")
    private void onSetPerspective(CameraType cameraType, Operation<Void> original) {
        if (Freecam.isEnabled()) return;
        original.call(cameraType);
    }
}
