package net.xolt.freecam.docgen;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.IndexedAssetSource;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.world.level.validation.DirectoryValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class Application {

    private final String assetIndex;
    private final Path assetsDir;
    private final Path modAssetsDir;
    private final String modId;
    private final Path buildDir;
    private final String lang;
    private LanguageManager languageManager;

    private Application(String assetIndex, Path assetsDir, Path modAssetsDir, String modId, Path buildDir, String lang) {
        this.assetIndex = assetIndex;
        this.assetsDir = assetsDir;
        this.modAssetsDir = modAssetsDir;
        this.modId = modId;
        this.buildDir = buildDir;
        this.lang = lang;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void run() throws IOException {
        // Validate the output dir
        validateOutputDir(buildDir);

        System.out.println("Bootstrapping");
        bootstrap();
        System.out.println("Finished bootstrapping");
        System.out.println("Language: " + languageManager.getSelected());

        System.out.println("Getting Language instance");
        Language language = Language.getInstance();

        System.out.println("%s: %s".formatted("gui.done", language.getOrDefault("gui.done", "<failed>")));
        System.out.println("%s: %s".formatted("key.freecam.toggle", language.getOrDefault("key.freecam.toggle", "<failed>")));


        Files.createDirectories(buildDir);
    }

    private void bootstrap() {
        System.out.println("Bootstrapping Minecraft");
        bootstrapMinecraft();

        System.out.println("Creating LanguageManager");
        languageManager = new LanguageManager(lang);

        System.out.println("Creating resource pack repository");
        Path realAssetsDir = IndexedAssetSource.createIndexFs(assetsDir, assetIndex);
        PackRepository repository = new PackRepository(
                new ClientPackSource(realAssetsDir, new DirectoryValidator(path -> true)),
                new ModPackSource(modId, modAssetsDir)
        );

        System.out.println("Loading resource packs");
        repository.reload();

        System.out.println("Loading resources");
        try (ReloadableResourceManager resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES)) {
            resourceManager.registerReloadListener(languageManager);

            resourceManager.createReload(
                    Util.backgroundExecutor(),
                    runnable -> {
                        System.out.println("Running resources load callback");
                        runnable.run();
                    },
                    CompletableFuture.completedFuture(Unit.INSTANCE),
                    repository.openAllSelected());
        }
        System.out.println("Finished loading resources");
    }

    private static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static void validateOutputDir(Path outputDir) throws IOException {
        if (Files.exists(outputDir)) {
            if (!Files.isDirectory(outputDir)) {
                System.err.println("Not a directory: %s".formatted(outputDir.toAbsolutePath()));
                System.exit(1);
            }
            try (var list = Files.list(outputDir)) {
                if (list.findAny().isPresent()) {
                    System.err.println("Warning: output directory is not empty: %s".formatted(outputDir.toAbsolutePath()));
                }
            } catch (IOException e) {
                System.err.println("Error reading output directory: %s".formatted(outputDir.toAbsolutePath()));
                throw e;
            }
        }
    }

    public static class Builder {
        private String lang;
        private String assetIndex;
        private Path assetsDir;
        private Path modAssetsDir;
        private String modId;
        private Path buildDir;

        private Builder() {}

        public Application build() {
            return new Application(assetIndex, assetsDir, modAssetsDir, modId, buildDir, lang);
        }

        public Builder withLang(String lang) {
            this.lang = lang;
            return this;
        }

        public Builder withAssetIndex(String assetIndex) {
            this.assetIndex = assetIndex;
            return this;
        }

        public Builder withAssetsDir(Path assetsDir) {
            this.assetsDir = assetsDir;
            return this;
        }

        public Builder withModAssetsDir(Path modAssetsDir) {
            this.modAssetsDir = modAssetsDir;
            return this;
        }

        public Builder withModId(String modId) {
            this.modId = modId;
            return this;
        }

        public Builder withBuildDir(Path buildDir) {
            this.buildDir = buildDir;
            return this;
        }
    }
}
