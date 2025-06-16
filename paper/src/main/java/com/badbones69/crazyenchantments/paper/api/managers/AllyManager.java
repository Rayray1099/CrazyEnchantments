package com.badbones69.crazyenchantments.paper.api.managers;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.FileManager.Files;
import com.badbones69.crazyenchantments.paper.api.objects.AllyMob;
import com.badbones69.crazyenchantments.paper.api.objects.AllyMob.AllyType;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AllyManager {

    @NotNull
    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);
    private final List<AllyMob> allyMobs = new ArrayList<>();
    private final Map<UUID, List<AllyMob>> allyOwners = new HashMap<>();
    private final Map<AllyType, String> allyTypeNameCache = new HashMap<>();
    
    public void load() {
        final FileConfiguration config = Files.CONFIG.getFile();
        final String allyTypePath = "Settings.EnchantmentOptions.Ally-Mobs.";

        for (final AllyType type : AllyType.values()) {
            this.allyTypeNameCache.put(type, ColorUtils.color(config.getString(allyTypePath + type.getConfigName(), type.getDefaultName())));
        }
    }
    
    public List<AllyMob> getAllyMobs() {
        return this.allyMobs;
    }
    
    public void addAllyMob(final AllyMob allyMob) {
        if (allyMob != null) {
            this.allyMobs.add(allyMob);

            final UUID owner = allyMob.getOwner().getUniqueId();

            if (this.allyOwners.containsKey(owner)) {
                this.allyOwners.get(owner).add(allyMob);
            } else {
                final List<AllyMob> allies = new ArrayList<>();

                allies.add(allyMob);

                this.allyOwners.put(owner, allies);
            }
        }
    }
    
    public void removeAllyMob(final AllyMob allyMob) {
        if (allyMob != null) {
            this.allyMobs.remove(allyMob);

            final UUID owner = allyMob.getOwner().getUniqueId();

            if (this.allyOwners.containsKey(owner)) {
                this.allyOwners.get(owner).add(allyMob);

                if (this.allyOwners.get(owner).isEmpty()) this.allyOwners.remove(owner);
            }
        }
    }
    
    public void forceRemoveAllies() {
        if (!this.allyMobs.isEmpty()) {
            for (final AllyMob ally : this.allyMobs) {
                final LivingEntity allyLE = ally.getAlly();

                allyLE.getScheduler().run(plugin, task -> allyLE.remove(), null);
            }

            this.allyMobs.clear();
            this.allyOwners.clear();
        }
    }
    
    public void forceRemoveAllies(final Player owner) {
        for (final AllyMob ally : this.allyOwners.getOrDefault(owner.getUniqueId(), new ArrayList<>())) {
            final LivingEntity allyLE = ally.getAlly();

            allyLE.getScheduler().run(plugin, task -> { //todo() use folia runnable from fusion
                allyLE.remove();
                this.allyMobs.remove(ally);
            }, null);
        }

        this.allyOwners.remove(owner.getUniqueId());
    }
    
    public void setEnemy(final Player owner, final Entity enemy) {
        //todo() folia runnable
        this.allyOwners.getOrDefault(owner.getUniqueId(), new ArrayList<>()).forEach(ally ->
            ally.getAlly().getScheduler().run(plugin, task -> ally.attackEnemy((LivingEntity) enemy), null));
    }
    
    public Map<AllyType, String> getAllyTypeNameCache() {
        return this.allyTypeNameCache;
    }
    
    public boolean isAlly(final Player player, final Entity livingEntity) {
        if (isAllyMob(livingEntity)) return isAlly(player, getAllyMob(livingEntity));

        return false;
    }
    
    public boolean isAlly(final Player player, final AllyMob ally) {
        return ally.getOwner().getUniqueId() == player.getUniqueId();
    }
    
    public boolean isAllyMob(final Entity livingEntity) {
        for (final AllyMob ally : this.allyMobs) {
            if (ally.getAlly().getUniqueId() == livingEntity.getUniqueId()) return true;
        }

        return false;
    }
    
    public AllyMob getAllyMob(final Entity livingEntity) {
        for (final AllyMob ally : this.allyMobs) {
            if (ally.getAlly().getUniqueId() == livingEntity.getUniqueId()) return ally;
        }

        return null;
    }
}