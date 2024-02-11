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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ModPackSource extends BuiltInPackSource {
    private static final PackMetadataSection VERSION_METADATA_SECTION = new PackMetadataSection(Component.literal("Mod Resources"), SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES), Optional.empty());
    private static final BuiltInMetadata METADATA = BuiltInMetadata.of(PackMetadataSection.TYPE, VERSION_METADATA_SECTION);
    private final String namespace;
    private final @Nullable Path externalAssetDir;

    public ModPackSource(String namespace, Path path, DirectoryValidator directoryValidator) {
        super(PackType.CLIENT_RESOURCES, ModPackSource.createVanillaPackSource(namespace, path), new ResourceLocation(namespace, "resourcepacks"), directoryValidator);
        this.namespace = namespace;
        this.externalAssetDir = this.findExplodedAssetPacks(path);
    }

    private @Nullable Path findExplodedAssetPacks(Path assetsDir) {
        Path path;
        if (SharedConstants.IS_RUNNING_IN_IDE && assetsDir.getFileSystem() == FileSystems.getDefault() && Files.isDirectory(path = assetsDir.getParent().resolve("resourcepacks"))) {
            return path;
        }
        return null;
    }

    private static VanillaPackResources createVanillaPackSource(String namespace, Path assetsDir) {
        VanillaPackResourcesBuilder vanillaPackResourcesBuilder = new VanillaPackResourcesBuilder().setMetadata(METADATA).exposeNamespace(namespace);
        return vanillaPackResourcesBuilder.applyDevelopmentConfig().pushJarResources().pushAssetPath(PackType.CLIENT_RESOURCES, assetsDir).build();
    }

    @Override
    protected @NotNull Component getPackTitle(String id) {
        return Component.literal(id);
    }

    @Override
    protected @Nullable Pack createVanillaPack(PackResources resources) {
        return Pack.readMetaAndCreate(namespace, VERSION_METADATA_SECTION.description(), true, ModPackSource.fixedResources(resources), PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.BUILT_IN);
    }

    @Override
    protected @Nullable Pack createBuiltinPack(String id, Pack.ResourcesSupplier resources, Component title) {
        return Pack.readMetaAndCreate(id, title, false, resources, PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.BUILT_IN);
    }

    @Override
    protected void populatePackList(BiConsumer<String, Function<String, Pack>> populator) {
        super.populatePackList(populator);
        if (this.externalAssetDir != null) {
            this.discoverPacksInPath(this.externalAssetDir, populator);
        }
    }
}

