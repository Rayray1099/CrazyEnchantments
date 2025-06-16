package com.badbones69.crazyenchantments.paper.tasks.processors;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class RunnableSyncProcessor extends ArmorProcessor {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final Server server = this.plugin.getServer();
    
    void process(final Runnable process) {
        this.server.getGlobalRegionScheduler().run(this.plugin, task -> process.run());
    }
}