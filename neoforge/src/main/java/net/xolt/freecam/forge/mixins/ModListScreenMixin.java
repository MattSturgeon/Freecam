package net.xolt.freecam.forge.mixins;

import net.minecraft.locale.Language;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.xolt.freecam.Freecam.MOD_ID;

/**
 * Enable translations in forge's mod info panel
 */
@Mixin(ModListScreen.class)
public abstract class ModListScreenMixin {

    @Unique
    private String freecam$translate(String key, String fallback) {
        return Language.getInstance().getOrDefault(key, fallback);
    }

    @Redirect(method = "updateCache", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforgespi/language/IModInfo;getDisplayName()Ljava/lang/String;"))
    String getName(IModInfo instance) {
        if (MOD_ID.equals(instance.getModId())) {
            return freecam$translate("freecam.name", instance.getDisplayName());
        }
        return instance.getDisplayName();
    }

    @Redirect(method = "updateCache", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforgespi/language/IModInfo;getDescription()Ljava/lang/String;"))
    String getDescription(IModInfo instance) {
        if (MOD_ID.equals(instance.getModId())) {
            return freecam$translate("freecam.description", instance.getDescription());
        }
        return instance.getDescription();
    }
}
