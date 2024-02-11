package net.xolt.freecam.docgen;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String... args) throws IOException {
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        NonOptionArgumentSpec<String> unrecognised = optionParser.nonOptions();
        ArgumentAcceptingOptionSpec<String> lang = optionParser
                .accepts("lang")
                .withRequiredArg()
                .defaultsTo("en_us");
        ArgumentAcceptingOptionSpec<String> assetIndex = optionParser
                .accepts("assetIndex")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec<File> assetsDir = optionParser
                .accepts("assetsDir")
                .withRequiredArg()
                .ofType(File.class);
        ArgumentAcceptingOptionSpec<File> modAssetsDir = optionParser
                .accepts("modAssetsDir")
                .withRequiredArg()
                .ofType(File.class);
        ArgumentAcceptingOptionSpec<String> modId = optionParser
                .accepts("modId")
                .withRequiredArg();
        ArgumentAcceptingOptionSpec<File> buildDir = optionParser
                .accepts("buildDir")
                .withRequiredArg()
                .ofType(File.class);

        OptionSet options = optionParser.parse(args);
        printUnrecognised(options, unrecognised);

        Application.builder()
                .withLang(options.valueOf(lang))
                .withAssetIndex(options.valueOf(assetIndex))
                .withAssetsDir(options.valueOf(assetsDir).toPath())
                .withModAssetsDir(options.valueOf(modAssetsDir).toPath())
                .withModId(options.valueOf(modId))
                .withBuildDir(options.valueOf(buildDir).toPath())
                .build()
                .run();
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
}
