package com.badbones69.crazyenchantments.paper.api.objects.items;

import com.badbones69.crazyenchantments.paper.api.FileManager;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DataKeys;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ScrollData {

    private String suffix;
    private boolean countVanillaEnchantments;
    private boolean useSuffix;
    private boolean blackScrollChanceToggle;
    private int blackScrollChance;

    public void loadScrollControl() {
        final FileConfiguration config = FileManager.Files.CONFIG.getFile();
        this.suffix = config.getString("Settings.TransmogScroll.Amount-of-Enchantments", " &7[&6&n%amount%&7]");
        this.countVanillaEnchantments = config.getBoolean("Settings.TransmogScroll.Count-Vanilla-Enchantments", true);
        this.useSuffix = config.getBoolean("Settings.TransmogScroll.Amount-Toggle", true);
        this.blackScrollChance = config.getInt("Settings.BlackScroll.Chance", 75);
        this.blackScrollChanceToggle = config.getBoolean("Settings.BlackScroll.Chance-Toggle", false);
    }

    public String getSuffix() {
        return this.suffix;
    }

    public boolean isCountVanillaEnchantments() {
        return this.countVanillaEnchantments;
    }

    public boolean isUseSuffix() {
        return this.useSuffix;
    }

    public boolean isBlackScrollChanceToggle() {
        return this.blackScrollChanceToggle;
    }

    public int getBlackScrollChance() {
        return this.blackScrollChance;
    }
}