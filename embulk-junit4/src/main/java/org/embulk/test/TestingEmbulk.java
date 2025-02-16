package org.embulk.test;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;
import static java.util.Locale.ENGLISH;
import static org.embulk.test.EmbulkTests.copyResource;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.embulk.EmbulkEmbed;
import org.embulk.EmbulkSystemProperties;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigLoader;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.exec.PreviewResult;
import org.embulk.spi.ColumnConfig;
import org.embulk.spi.DecoderPlugin;
import org.embulk.spi.EncoderPlugin;
import org.embulk.spi.ExecutorPlugin;
import org.embulk.spi.FileInputPlugin;
import org.embulk.spi.FileOutputPlugin;
import org.embulk.spi.FilterPlugin;
import org.embulk.spi.FormatterPlugin;
import org.embulk.spi.GuessPlugin;
import org.embulk.spi.InputPlugin;
import org.embulk.spi.OutputPlugin;
import org.embulk.spi.ParserPlugin;
import org.embulk.spi.Schema;
import org.embulk.spi.SchemaConfig;
import org.embulk.spi.TempFileException;
import org.embulk.spi.TempFileSpace;
import org.embulk.spi.TempFileSpaceImpl;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class TestingEmbulk implements TestRule {
    public static class Builder {
        private Properties embulkSystemProperties = null;
        private LinkedHashMap<Class<?>, LinkedHashMap<String, Class<?>>> builtinPlugins;

        Builder() {
            this.builtinPlugins = new LinkedHashMap<>();
            this.builtinPlugins.put(DecoderPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(EncoderPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(ExecutorPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(FileInputPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(FileOutputPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(FilterPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(FormatterPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(GuessPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(InputPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(OutputPlugin.class, new LinkedHashMap<>());
            this.builtinPlugins.put(ParserPlugin.class, new LinkedHashMap<>());
        }

        public <T> Builder registerPlugin(final Class<T> iface, final String name, final Class<?> impl) {
            this.builtinPlugins.get(iface).put(name, impl);
            return this;
        }

        public <T> Builder setEmbulkSystemProperties(final Properties properties) {
            this.embulkSystemProperties = properties;
            return this;
        }

        public TestingEmbulk build() {
            return new TestingEmbulk(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final EmbulkSystemProperties embulkSystemProperties;
    private final LinkedHashMap<Class<?>, LinkedHashMap<String, Class<?>>> builtinPlugins;

    private EmbulkEmbed embed;
    private TempFileSpace tempFiles;

    TestingEmbulk(Builder builder) {
        this.builtinPlugins = builder.builtinPlugins;

        if (builder.embulkSystemProperties != null) {
            this.embulkSystemProperties = EmbulkSystemProperties.of(builder.embulkSystemProperties);
        } else {
            this.embulkSystemProperties = EmbulkSystemProperties.of(new Properties());
        }
        reset();
    }

    public void reset() {
        destroy();

        final EmbulkEmbed.Bootstrap bootstrap = new EmbulkEmbed.Bootstrap();
        bootstrap.setEmbulkSystemProperties(this.embulkSystemProperties);
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(DecoderPlugin.class).entrySet()) {
            bootstrap.builtinDecoderPlugin(plugin.getKey(), (Class<DecoderPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(EncoderPlugin.class).entrySet()) {
            bootstrap.builtinEncoderPlugin(plugin.getKey(), (Class<EncoderPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(ExecutorPlugin.class).entrySet()) {
            bootstrap.builtinExecutorPlugin(plugin.getKey(), (Class<ExecutorPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(FileInputPlugin.class).entrySet()) {
            bootstrap.builtinFileInputPlugin(plugin.getKey(), (Class<FileInputPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(FileOutputPlugin.class).entrySet()) {
            bootstrap.builtinFileOutputPlugin(plugin.getKey(), (Class<FileOutputPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(FilterPlugin.class).entrySet()) {
            bootstrap.builtinFilterPlugin(plugin.getKey(), (Class<FilterPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(FormatterPlugin.class).entrySet()) {
            bootstrap.builtinFormatterPlugin(plugin.getKey(), (Class<FormatterPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(GuessPlugin.class).entrySet()) {
            bootstrap.builtinGuessPlugin(plugin.getKey(), (Class<GuessPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(InputPlugin.class).entrySet()) {
            bootstrap.builtinInputPlugin(plugin.getKey(), (Class<InputPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(OutputPlugin.class).entrySet()) {
            bootstrap.builtinOutputPlugin(plugin.getKey(), (Class<OutputPlugin>) plugin.getValue());
        }
        for (final Map.Entry<String, Class<?>> plugin : this.builtinPlugins.get(ParserPlugin.class).entrySet()) {
            bootstrap.builtinParserPlugin(plugin.getKey(), (Class<ParserPlugin>) plugin.getValue());
        }
        this.embed = bootstrap
                .builtinInputPlugin("preview_result", PreviewResultInputPlugin.class)
                .setAlternativeBulkLoader(new TestingBulkLoader(this.embulkSystemProperties))
                .initializeCloseable();

        try {
            this.tempFiles = TempFileSpaceImpl.with(Files.createTempDirectory("embulk-test-temp-"), "foo");
        } catch (IOException ex) {
            throw new TempFileException(ex);
        }
    }

    public void destroy() {
        if (embed != null) {
            // EmbulkEmbed#destroy had been called here.
            // It was removed as JSR-250 lifecycle support is to be unsupported.
            embed = null;
        }
        if (tempFiles != null) {
            tempFiles.cleanup();
            tempFiles = null;
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new EmbulkTestingEmbedWatcher().apply(base, description);
    }

    private class EmbulkTestingEmbedWatcher extends TestWatcher {
        @Override
        protected void starting(Description description) {
            reset();
        }

        // EmbulkEmbed#destroy had been called in TestWatcher#finished.
        // It was removed as JSR-250 lifecycle support is to be unsupported.
    }

    public Path createTempFile(String suffix) {
        return tempFiles.createTempFile(suffix).toPath();
    }

    public ConfigLoader configLoader() {
        return embed.newConfigLoader();
    }

    public ConfigSource newConfig() {
        return configLoader().newConfigSource();
    }

    public ConfigSource loadYamlResource(String name) {
        return configLoader()
                .fromYamlString(EmbulkTests.readResource(name));
    }

    private static final List<String> SUPPORTED_TYPES = ImmutableList.of(
            "boolean", "long", "double", "string", "timestamp", "json"
    );

    public static interface RunResult {
        ConfigDiff getConfigDiff();

        List<Throwable> getIgnoredExceptions();

        Schema getInputSchema();

        Schema getOutputSchema();

        List<TaskReport> getInputTaskReports();

        List<TaskReport> getOutputTaskReports();
    }

    public class InputBuilder {
        private ConfigSource inConfig = null;
        private List<ConfigSource> filtersConfig = ImmutableList.of();
        private ConfigSource execConfig = newConfig();
        private Path outputPath = null;

        private InputBuilder() {}

        public InputBuilder in(ConfigSource inConfig) {
            checkNotNull(inConfig, "inConfig");
            this.inConfig = inConfig.deepCopy();
            return this;
        }

        public InputBuilder filters(List<ConfigSource> filtersConfig) {
            checkNotNull(filtersConfig, "filtersConfig");
            ImmutableList.Builder<ConfigSource> builder = ImmutableList.builder();
            for (ConfigSource filter : filtersConfig) {
                builder.add(filter.deepCopy());
            }
            this.filtersConfig = builder.build();
            return this;
        }

        public InputBuilder exec(ConfigSource execConfig) {
            checkNotNull(execConfig, "execConfig");
            this.execConfig = execConfig.deepCopy();
            return this;
        }

        public InputBuilder outputPath(Path outputPath) {
            checkNotNull(outputPath, "outputPath");
            this.outputPath = outputPath;
            return this;
        }

        public ConfigDiff guess() {
            checkState(inConfig != null, "in config must be set");

            // config = {exec: execConfig, in: inConfig}
            ConfigSource config = newConfig()
                    .set("exec", execConfig)
                    .set("in", inConfig)
                    .set("filters", filtersConfig);

            // embed.guess returns GuessExecutor.ConfigDiff
            return embed.guess(config).getNested("in");
        }

        /**
         * This method returns PreviewResult.
         *
         * @return PreviewResult returns the result by PreviewExecutor
         * @throws IOException in case of failures in file operations
         */
        public PreviewResult preview() throws IOException {
            checkState(inConfig != null, "inputPath must be set");
            checkState(outputPath != null, "outputPath must be set");

            // Execute preview to get PreviewResult
            ConfigSource previewConfig = newConfig()
                    .set("exec", execConfig.set("min_output_tasks", 1)) // exec: config
                    .set("in", inConfig)
                    .set("filters", filtersConfig);
            PreviewResult result = embed.preview(previewConfig);
            PreviewResultInputPlugin.setPreviewResult(result);

            String fileName = outputPath.getFileName().toString();
            checkArgument(fileName.endsWith(".csv"), "outputPath must end with .csv");
            Path dir = outputPath.getParent().resolve(fileName.substring(0, fileName.length() - 4));
            Files.createDirectories(dir);

            // Execute run to write PreviewResult's Page objects to output files
            ConfigSource runConfig = newConfig()
                    .set("in", newConfig().set("type", "preview_result")) // in: config
                    .set("out", newConfig() // out: config
                            .set("type", "file")
                            .set("path_prefix", dir.resolve("fragments_").toString())
                            .set("file_ext", "csv")
                            .set("formatter", newConfig()
                                    .set("type", "csv")
                                    .set("header_line", false)
                                    .set("newline", "LF")));
            embed.run(runConfig);

            return buildPreviewResultWithOutput(result, dir, outputPath);
        }

        public RunResult run() throws IOException {
            checkState(inConfig != null, "in config must be set");
            checkState(outputPath != null, "outputPath must be set");

            String fileName = outputPath.getFileName().toString();
            checkArgument(fileName.endsWith(".csv"), "outputPath must end with .csv");
            Path dir = outputPath.getParent().resolve(fileName.substring(0, fileName.length() - 4));

            Files.createDirectories(dir);

            // exec: config
            execConfig.set("min_output_tasks", 1);

            // out: config
            ConfigSource outConfig = newConfig()
                    .set("type", "file")
                    .set("path_prefix", dir.resolve("fragments_").toString())
                    .set("file_ext", "csv")
                    .set("formatter", newConfig()
                            .set("type", "csv")
                            .set("header_line", false)
                            .set("newline", "LF"));

            // combine exec:, out: and in:
            ConfigSource config = newConfig()
                    .set("exec", execConfig)
                    .set("in", inConfig)
                    .set("filters", filtersConfig)
                    .set("out", outConfig);

            // embed.run returns TestingBulkLoader.TestingExecutionResult because
            // LoaderState.buildExecuteResultWithWarningException is overridden.
            RunResult result = (RunResult) embed.run(config);

            return buildRunResultWithOutput(result, dir, outputPath);
        }
    }

    public class ParserBuilder {
        private ConfigSource parserConfig = newConfig();
        private ConfigSource execConfig = newConfig();
        private Path inputPath = null;
        private Path outputPath = null;

        private ParserBuilder() {}

        public ParserBuilder parser(ConfigSource parserConfig) {
            checkNotNull(parserConfig, "parserConfig");
            this.parserConfig = parserConfig.deepCopy();
            return this;
        }

        public ParserBuilder exec(ConfigSource execConfig) {
            checkNotNull(execConfig, "execConfig");
            this.execConfig = execConfig.deepCopy();
            return this;
        }

        public ParserBuilder inputPath(Path inputPath) {
            checkNotNull(inputPath, "inputPath");
            this.inputPath = inputPath;
            return this;
        }

        public ParserBuilder inputResource(String resourceName) throws IOException {
            checkNotNull(resourceName, "resourceName");
            Path path = createTempFile("csv");
            copyResource(resourceName, path);
            return inputPath(path);
        }

        public ParserBuilder outputPath(Path outputPath) {
            checkNotNull(outputPath, "outputPath");
            this.outputPath = outputPath;
            return this;
        }

        public ConfigDiff guess() {
            checkState(inputPath != null, "inputPath must be set");

            // in: config
            ConfigSource inConfig = newConfig()
                    .set("type", "file")
                    .set("path_prefix", inputPath.toAbsolutePath().toString());
            inConfig.set("parser", parserConfig);

            // config = {exec: execConfig, in: inConfig}
            ConfigSource config = newConfig()
                    .set("exec", execConfig)
                    .set("in", inConfig);

            // embed.guess calls GuessExecutor and returns ConfigDiff
            return embed.guess(config).getNested("in").getNested("parser");
        }

        public RunResult run() throws IOException {
            checkState(parserConfig != null, "parser config must be set");
            checkState(inputPath != null, "inputPath must be set");
            checkState(outputPath != null, "outputPath must be set");

            String fileName = outputPath.getFileName().toString();
            checkArgument(fileName.endsWith(".csv"), "outputPath must end with .csv");
            Path dir = outputPath.getParent().resolve(fileName.substring(0, fileName.length() - 4));

            Files.createDirectories(dir);

            // in: config
            ConfigSource inConfig = newConfig()
                    .set("type", "file")
                    .set("path_prefix", inputPath.toAbsolutePath().toString());
            inConfig.set("parser", parserConfig);

            // exec: config
            execConfig.set("min_output_tasks", 1);

            // out: config
            ConfigSource outConfig = newConfig()
                    .set("type", "file")
                    .set("path_prefix", dir.resolve("fragments_").toString())
                    .set("file_ext", "csv")
                    .set("formatter", newConfig()
                            .set("type", "csv")
                            .set("header_line", false)
                            .set("newline", "LF"));

            // config = {exec: execConfig, in: inConfig, out: outConfig}
            ConfigSource config = newConfig()
                    .set("exec", execConfig)
                    .set("in", inConfig)
                    .set("out", outConfig);

            // embed.run returns TestingBulkLoader.TestingExecutionResult because
            // LoaderState.buildExecuteResultWithWarningException is overridden.
            RunResult result = (RunResult) embed.run(config);

            return buildRunResultWithOutput(result, dir, outputPath);
        }
    }

    public class OutputBuilder {
        private ConfigSource outConfig = null;
        private ConfigSource execConfig = newConfig();
        private Path inputPath;
        private SchemaConfig inputSchema;

        public OutputBuilder() {}

        public OutputBuilder out(ConfigSource outConfig) {
            checkNotNull(outConfig, "outConfig");
            this.outConfig = outConfig;
            return this;
        }

        public OutputBuilder exec(ConfigSource execConfig) {
            checkNotNull(execConfig, "execConfig");
            this.execConfig = execConfig;
            return this;
        }

        public OutputBuilder inputPath(Path inputPath) {
            checkNotNull(inputPath, "inputPath");
            this.inputPath = inputPath;
            return this;
        }

        public OutputBuilder inputResource(String resourceName) throws IOException {
            checkNotNull(resourceName, "resourceName");
            Path path = createTempFile("csv");
            copyResource(resourceName, path);
            return inputPath(path);
        }

        public OutputBuilder inputSchema(SchemaConfig inputSchema) {
            checkNotNull(inputSchema, "inputSchema");
            this.inputSchema = inputSchema;
            return this;
        }

        public RunResult run() throws IOException {
            checkState(outConfig != null, "out config must be set");
            checkState(inputPath != null, "inputPath must be set");

            String fileName = inputPath.toAbsolutePath().toString();
            checkArgument(fileName.endsWith(".csv"), "inputPath must end with .csv");

            // exec: config
            execConfig.set("min_output_tasks", 1);

            // in: config
            ConfigSource inConfig = newConfig()
                    .set("type", "file")
                    .set("path_prefix", fileName)
                    .set("parser", newParserConfig());

            // config = {exec: execConfig, in: inConfig, out: outConfig}
            ConfigSource config = newConfig()
                    .set("exec", execConfig)
                    .set("in", inConfig)
                    .set("out", outConfig);

            // embed.run returns TestingBulkLoader.TestingExecutionResult because
            // LoaderState.buildExecuteResultWithWarningException is overridden.
            return (RunResult) embed.run(config);
        }

        private ConfigSource newParserConfig() {
            return newConfig()
                    .set("charset", "UTF-8")
                    .set("newline", "LF")
                    .set("type", "csv")
                    .set("delimiter", ",")
                    .set("quote", "\"")
                    .set("escape", "\"")
                    .set("skip_header_lines", 1)  // Skip the header because the input csv must have "columnName:columnType" defined in the header.
                    .set("columns", newSchemaConfig());
        }

        private SchemaConfig newSchemaConfig() {
            ImmutableList.Builder<ColumnConfig> schema = ImmutableList.builder();
            try (BufferedReader reader = newBufferedReader(inputPath, UTF_8)) {
                for (String column : reader.readLine().split(",")) {
                    ColumnConfig columnConfig = newColumnConfig(column);
                    if (columnConfig != null) {
                        schema.add(columnConfig);
                    }
                }
                return new SchemaConfig(schema.build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private ColumnConfig newColumnConfig(String column) {
            String[] tuple = column.split(":", 2);
            checkArgument(tuple.length == 2, "tuple must be a pair of column name and type");
            String type = tuple[1];
            if (!SUPPORTED_TYPES.contains(type)) {
                throw new IllegalArgumentException(String.format(ENGLISH,
                            "Unknown column type %s. Supported types are boolean, long, double, string, timestamp and json: %s",
                            tuple[1], column));
            }
            return new ColumnConfig(newConfig()
                    .set("name", tuple[0])
                    .set("type", type));
        }
    }

    private PreviewResult buildPreviewResultWithOutput(PreviewResult result, Path outputDir, Path outputPath)
            throws IOException {
        copyToPath(outputDir, outputPath);
        return result;
    }

    private RunResult buildRunResultWithOutput(RunResult result, Path outputDir, Path outputPath)
            throws IOException {
        copyToPath(outputDir, outputPath);
        return result;
    }

    private void copyToPath(Path outputDir, Path outputPath)
            throws IOException {
        try (OutputStream out = Files.newOutputStream(outputPath)) {
            List<Path> fragments = new ArrayList<Path>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDir, "fragments_*.csv")) {
                for (Path fragment : stream) {
                    fragments.add(fragment);
                }
            }
            Collections.sort(fragments);
            for (Path fragment : fragments) {
                try (InputStream in = Files.newInputStream(fragment)) {
                    ByteStreams.copy(in, out);
                }
            }
        }
    }

    public InputBuilder inputBuilder() {
        return new InputBuilder();
    }

    public ParserBuilder parserBuilder() {
        return new ParserBuilder();
    }

    public OutputBuilder outputBuilder() {
        return new OutputBuilder();
    }

    public RunResult runParser(ConfigSource parserConfig, Path inputPath, Path outputPath)
            throws IOException {
        return parserBuilder()
                .parser(parserConfig)
                .inputPath(inputPath)
                .outputPath(outputPath)
                .run();
    }

    public RunResult runParser(ConfigSource parserConfig, Path inputPath, Path outputPath, ConfigSource execConfig)
            throws IOException {
        return parserBuilder()
                .parser(parserConfig)
                .inputPath(inputPath)
                .outputPath(outputPath)
                .exec(execConfig)
                .run();
    }

    public RunResult runInput(ConfigSource inConfig, Path outputPath) throws IOException {
        return inputBuilder()
                .in(inConfig)
                .outputPath(outputPath)
                .run();
    }

    public RunResult runInput(ConfigSource inConfig, Path outputPath, ConfigSource execConfig)
            throws IOException {
        return inputBuilder()
                .exec(execConfig)
                .in(inConfig)
                .outputPath(outputPath)
                .run();
    }

    public RunResult runOutput(ConfigSource outConfig, Path inputPath)
            throws IOException {
        return outputBuilder()
                .out(outConfig)
                .inputPath(inputPath)
                .run();
    }

    public RunResult runOutput(ConfigSource outConfig, Path inputPath, ConfigSource execConfig)
            throws IOException {
        return outputBuilder()
                .exec(execConfig)
                .out(outConfig)
                .inputPath(inputPath)
                .run();
    }

    public ConfigDiff guessInput(ConfigSource inSeedConfig) {
        return inputBuilder()
                .in(inSeedConfig)
                .guess();
    }

    public ConfigDiff guessInput(ConfigSource inSeedConfig, ConfigSource execConfig) {
        return inputBuilder()
                .exec(execConfig)
                .in(inSeedConfig)
                .guess();
    }

    public ConfigDiff guessParser(Path inputPath) {
        return parserBuilder()
                .inputPath(inputPath)
                .guess();
    }

    public ConfigDiff guessParser(ConfigSource parserSeedConfig, Path inputPath) {
        return parserBuilder()
                .parser(parserSeedConfig)
                .inputPath(inputPath)
                .guess();
    }

    public ConfigDiff guessParser(ConfigSource parserSeedConfig, Path inputPath, ConfigSource execConfig) {
        return parserBuilder()
                .parser(parserSeedConfig)
                .inputPath(inputPath)
                .exec(execConfig)
                .guess();
    }

    // TODO add runFilter(ConfigSource filterConfig, Path inputPath, Path outputPath) where inputPath is a path to
    // a CSV file whose column types can be naturally guessed using csv guess plugin.
}
