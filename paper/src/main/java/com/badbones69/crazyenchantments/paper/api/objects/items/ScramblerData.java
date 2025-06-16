package com.badbones69.crazyenchantments.paper.api.objects.items;

import com.badbones69.crazyenchantments.paper.api.FileManager;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DataKeys;
import com.badbones69.crazyenchantments.paper.api.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ScramblerData {

    private ItemBuilder scramblerItem;
    private ItemBuilder pointer;
    private boolean animationToggle;
    private String guiName;

    public void loadScrambler() {
        final FileConfiguration config = FileManager.Files.CONFIG.getFile();

        this.scramblerItem = new ItemBuilder().setMaterial(config.getString("Settings.Scrambler.Item", "SUNFLOWER"))
                .setName(config.getString("Settings.Scrambler.Name", "&e&lThe Grand Scrambler"))
                .setLore(config.getStringList("Settings.Scrambler.Lore"))
                .setGlow(config.getBoolean("Settings.Scrambler.Glowing", false));

        this.pointer = new ItemBuilder().setMaterial(config.getString("Settings.Scrambler.GUI.Pointer.Item", "REDSTONE_TORCH"))
                .setName(config.getString("Settings.Scrambler.GUI.Pointer.Name", "&c&lPointer"))
                .setLore(config.getStringList("Settings.Scrambler.GUI.Pointer.Lore"));

        this.animationToggle = config.getBoolean("Settings.Scrambler.GUI.Toggle", true);

        final String name = config.getString("Settings.Scrambler.GUI.Name", "&8Rolling the &eScrambler");

        this.guiName = name.isEmpty() ? name : ColorUtils.color(name); // only color if not empty.
    }

    /**
     * Get the scrambler item stack.
     * @return The scramblers.
     */
    public ItemStack getScramblers() {
        return getScramblers(1);
    }

    /**
     * Get the scrambler item stack.
     * @param amount The amount you want.
     * @return The scramblers.
     */
    public ItemStack getScramblers(final int amount) {
        final ItemStack item = this.scramblerItem.setAmount(amount).build();

        item.editPersistentDataContainer(container -> {
            container.set(DataKeys.scrambler.getNamespacedKey(), PersistentDataType.BOOLEAN, true);
        });

        return item;
    }

    public boolean isScrambler(final ItemStack item) {
        return item.getPersistentDataContainer().has(DataKeys.scrambler.getNamespacedKey());
    }

    public String getGuiName() {
        return this.guiName;
    }

    public ItemBuilder getPointer() {
        return this.pointer;
    }

    public boolean isAnimationToggle() {
        return this.animationToggle;
    }
}