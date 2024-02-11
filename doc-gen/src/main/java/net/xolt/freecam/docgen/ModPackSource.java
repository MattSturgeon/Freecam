/*
 * Decompiled with CFR 0.2.1 (FabricMC 53fa44c9).
 */
package net.xolt.freecam.docgen;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.jetbrains.annotations.Nullable;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ModPackSource
extends BuiltInPackSource {
    private static final PackMetadataSection VERSION_METADATA_SECTION = new PackMetadataSection(Component.translatable("resourcePack.vanilla.description"), SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES), Optional.empty());
    private static final BuiltInMetadata BUILT_IN_METADATA = BuiltInMetadata.of(PackMetadataSection.TYPE, VERSION_METADATA_SECTION);
    private static final Component VANILLA_NAME = Component.translatable("resourcePack.vanilla.name");
    private static final ResourceLocation PACKS_DIR = new ResourceLocation("freecam", "resourcepacks");
    @Nullable
    private final Path externalAssetDir;

    public ModPackSource(Path path, DirectoryValidator directoryValidator) {
        super(PackType.CLIENT_RESOURCES, ModPackSource.createVanillaPackSource(path), PACKS_DIR, directoryValidator);
        this.externalAssetDir = this.findExplodedAssetPacks(path);
    }

    @Nullable
    private Path findExplodedAssetPacks(Path assetIndex) {
        Path path;
        if (SharedConstants.IS_RUNNING_IN_IDE && assetIndex.getFileSystem() == FileSystems.getDefault() && Files.isDirectory(path = assetIndex.getParent().resolve("resourcepacks"))) {
            return path;
        }
        return null;
    }

    private static VanillaPackResources createVanillaPackSource(Path assetIndex) {
        VanillaPackResourcesBuilder vanillaPackResourcesBuilder = new VanillaPackResourcesBuilder().setMetadata(BUILT_IN_METADATA).exposeNamespace("freecam");
        return vanillaPackResourcesBuilder.applyDevelopmentConfig().pushJarResources().pushAssetPath(PackType.CLIENT_RESOURCES, assetIndex).build();
    }

    @Override
    protected Component getPackTitle(String id) {
        return Component.literal(id);
    }

    @Override
    @Nullable
    protected Pack createVanillaPack(PackResources resources) {
        return Pack.readMetaAndCreate("freecam", VANILLA_NAME, true, ModPackSource.fixedResources(resources), PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.BUILT_IN);
    }

    @Override
    @Nullable
    protected Pack createBuiltinPack(String id, Pack.ResourcesSupplier resources, Component title) {
        return Pack.readMetaAndCreate(id, title, false, resources, PackType.CLIENT_RESOURCES, Pack.Position.TOP, PackSource.BUILT_IN);
    }

    @Override
    protected void populatePackList(BiConsumer<String, Function<String, Pack>> populator) {
        super.populatePackList(populator);
        if (this.externalAssetDir != null) {
            this.discoverPacksInPath(this.externalAssetDir, populator);
        }
    }
}

