package net.xolt.freecam.docgen;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public class ModPackSource implements RepositorySource {
    private static final PackMetadataSection VERSION_METADATA_SECTION = new PackMetadataSection(
            Component.literal("Mod Resources"),
            SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES),
            Optional.empty());
    private static final BuiltInMetadata METADATA = BuiltInMetadata.of(PackMetadataSection.TYPE, VERSION_METADATA_SECTION);
    private final String namespace;
    private final PackResources resources;

    public ModPackSource(String namespace, Path assetsDir) {
        this.namespace = namespace;
        this.resources = new VanillaPackResourcesBuilder()
                .setMetadata(METADATA)
                .exposeNamespace(namespace)
                .applyDevelopmentConfig()
                .pushAssetPath(PackType.CLIENT_RESOURCES, assetsDir)
                .pushJarResources()
                .build();
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        Optional.ofNullable(createPack()).ifPresent(consumer);
    }

    private @Nullable Pack createPack() {
        return Pack.readMetaAndCreate(
                namespace,
                VERSION_METADATA_SECTION.description(),
                true,
                fixedResourcesSupplier(resources),
                PackType.CLIENT_RESOURCES,
                Pack.Position.BOTTOM,
                PackSource.BUILT_IN);
    }

    private static Pack.ResourcesSupplier fixedResourcesSupplier(final PackResources resources) {
        return new Pack.ResourcesSupplier() {
            public @NotNull PackResources openPrimary(String id) {
                return resources;
            }

            public @NotNull PackResources openFull(String id, Pack.Info info) {
                return resources;
            }
        };
    }
}

