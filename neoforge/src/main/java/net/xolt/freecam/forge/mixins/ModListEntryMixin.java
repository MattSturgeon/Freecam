package net.xolt.freecam.forge.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.client.gui.widget.ModListWidget;
import net.neoforged.neoforgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.xolt.freecam.Freecam.MOD_ID;

/**
 * Enable translations in forge's mod list
 */
@Mixin(ModListWidget.ModEntry.class)
public class ModListEntryMixin {

    @Shadow @Final private IModInfo modInfo;

    @Unique
    private static MutableComponent freecam$displayName() {
        return Component.translatable("freecam.name");
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    MutableComponent setNameToRender(String arg) {
        if (MOD_ID.equals(modInfo.getModId())) {
            return freecam$displayName();
        }
        return Component.literal(arg);
    }

    @Inject(method = "getNarration", at = @At(value = "HEAD"), cancellable = true)
    void getNarration(CallbackInfoReturnable<Component> cir) {
        if (MOD_ID.equals(modInfo.getModId())) {
            cir.setReturnValue(Component.translatable("narrator.select", freecam$displayName()));
        }
    }
}
