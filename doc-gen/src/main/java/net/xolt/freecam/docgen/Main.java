package net.xolt.freecam.docgen;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String... args) throws IOException {
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        NonOptionArgumentSpec<String> unrecognisedArgsSpec = optionParser.nonOptions();
        ArgumentAcceptingOptionSpec<String> langSpec = optionParser
                .accepts("lang")
                .withRequiredArg()
                .defaultsTo("en_us");
        ArgumentAcceptingOptionSpec<String> assetIndexSpec = optionParser
                .accepts("assetIndex")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec<File> assetsDirSpec = optionParser
                .accepts("assetsDir")
                .withRequiredArg()
                .ofType(File.class);
        ArgumentAcceptingOptionSpec<File> modAssetsDirSpec = optionParser
                .accepts("modAssetsDir")
                .withRequiredArg()
                .ofType(File.class);
        ArgumentAcceptingOptionSpec<String> modIdSpec = optionParser
                .accepts("modId")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec<File> buildDirSpec = optionParser
                .accepts("buildDir")
                .withRequiredArg()
                .ofType(File.class);

        OptionSet options = optionParser.parse(args);
        String lang = options.valueOf(langSpec);
        String assetIndex = options.valueOf(assetIndexSpec);
        Path assetsDir = options.valueOf(assetsDirSpec).toPath();
        Path modAssetsDir = options.valueOf(modAssetsDirSpec).toPath();
        String modId = options.valueOf(modIdSpec);
        Path buildDir = options.valueOf(buildDirSpec).toPath();

        printUnrecognised(options, unrecognisedArgsSpec);
        System.out.println("Asset Index: " + assetIndex);
        System.out.println("Assets Dir: " + assetsDir.toAbsolutePath());
        System.out.println("Mod Assets Dir: " + modAssetsDir.toAbsolutePath());
        System.out.println("Mod ID: " + modId);
        System.out.println("Build Dir: " + buildDir.toAbsolutePath());

        // Validate we output dir
        validateOutputDir(buildDir);

        System.out.println("Bootstrapping Minecraft");
        bootstrapMinecraft();


        System.out.println("Creating LanguageManager");
        LanguageManager languageManager = new LanguageManager(lang);

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

        System.out.println("Setting Language selection");
        languageManager.setSelected(lang);

        System.out.println("Getting Language instance");
        Language language = Language.getInstance();
        System.out.println("Language instance: " + language);

        System.out.println("%s: %s".formatted("gui.done", language.getOrDefault("gui.done", "<failed>")));
        System.out.println("%s: %s".formatted("key.freecam.toggle", language.getOrDefault("key.freecam.toggle", "<failed>")));


        Files.createDirectories(buildDir);
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

    private static void printUnrecognised(OptionSet options, NonOptionArgumentSpec<?> unrecognised) {
        if (!options.has(unrecognised)) {
            return;
        }
        List<?> args = options.valuesOf(unrecognised);
        if (args.isEmpty()) {
            return;
        }

        System.err.println("Ignored arguments:");
        args.forEach(arg -> System.err.println("    " + arg));
        System.err.println();
    }

    private static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
