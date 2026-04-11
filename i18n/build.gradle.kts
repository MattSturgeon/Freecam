plugins {
    id("freecam.i18n")
}

// Test
i18n {
    transform("${meta.id}.mod.description") {
        rename("modmenu.descriptionTranslation.${meta.id}")
        rename("fml.menu.mods.info.description.${meta.id}")
    }
}
