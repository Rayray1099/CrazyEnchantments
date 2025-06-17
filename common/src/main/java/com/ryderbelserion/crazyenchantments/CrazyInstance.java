package com.ryderbelserion.crazyenchantments;

import com.ryderbelserion.crazyenchantments.interfaces.ICrazyEnchantments;
import com.ryderbelserion.crazyenchantments.objects.ConfigOptions;
import com.ryderbelserion.fusion.core.files.FileManager;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.ArrayList;

public class CrazyInstance implements ICrazyEnchantments {

    private final FileManager fileManager;
    private final Path path;

    public CrazyInstance(@NotNull final FileManager fileManager, @NotNull final Path path) {
        this.fileManager = fileManager;
        this.path = path;
    }

    private ConfigOptions options;

    @Override
    public void init() {
        this.fileManager.addFile(this.path.resolve("config.yml"), new ArrayList<>(), null);

        this.options = new ConfigOptions();
        this.options.init();
    }

    @Override
    public final FileManager getFileManager() {
        return this.fileManager;
    }

    @Override
    public final ConfigOptions getOptions() {
        return this.options;
    }

    @Override
    public final Path getPath() {
        return this.path;
    }
}