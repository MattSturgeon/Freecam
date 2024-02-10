package net.xolt.freecam.docgen;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.locale.Language;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String... args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: genDocs [lang] [output_dir]");
            System.exit(1);
        }

        String lang = args[0].toLowerCase().replace('-', '_');
        Path outputDir = Paths.get(args[1]);

        // Verbose logging
        System.out.println("Using language code: " + lang);
        System.out.println("Using output directory: "+ outputDir.toAbsolutePath());

        // Validate we output dir
        validateOutputDir(outputDir);

        System.out.println("Bootstrapping Minecraft");
        bootstrapMinecraft();

        System.out.println("Creating ResourceManager");
        try (ReloadableResourceManager resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES)) {
            LanguageManager languageManager = new LanguageManager(lang);
            resourceManager.registerReloadListener(languageManager);

            // FIXME need to add mc & freecam assets to resourceManager, then reload


            List<PackResources> packs = Collections.emptyList();
            ReloadInstance reloadInstance = resourceManager.createReload(
                    Util.backgroundExecutor(),
                    command -> System.out.println("Reload command: ".formatted(command)),
                    CompletableFuture.completedFuture(Unit.INSTANCE),
                    packs);

            while (!reloadInstance.isDone()) {
                System.out.printf("Reload progress: %01.2f%%%n", reloadInstance.getActualProgress() * 100f);
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Getting Language instance");
        Language language = Language.getInstance();
        System.out.println("Language instance: " + language);

        System.out.println("%s: %s".formatted("gui.done", language.getOrDefault("gui.done", "<failed>")));


        Files.createDirectories(outputDir);
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

    private static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
