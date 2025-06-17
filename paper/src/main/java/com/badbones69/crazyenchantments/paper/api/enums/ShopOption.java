package com.badbones69.crazyenchantments.paper.api.enums;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.api.economy.Currency;
import com.badbones69.crazyenchantments.paper.api.builders.ItemBuilder;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import java.util.HashMap;
import java.util.Map;

public enum ShopOption {
    
    GKITZ("GKitz", "GKitz", "Name", "Lore", false),
    BLACKSMITH("BlackSmith", "BlackSmith", "Name", "Lore", false),
    TINKER("Tinker", "Tinker", "Name", "Lore", false),
    INFO("Info", "Info", "Name", "Lore", false),
    
    PROTECTION_CRYSTAL("ProtectionCrystal", "ProtectionCrystal", "GUIName", "GUILore", true),
    SUCCESS_DUST("SuccessDust", "Dust.SuccessDust", "GUIName", "GUILore", true),
    DESTROY_DUST("DestroyDust", "Dust.DestroyDust", "GUIName", "GUILore", true),
    SCRAMBLER("Scrambler", "Scrambler", "GUIName", "GUILore", true),
    
    BLACK_SCROLL("BlackScroll", "BlackScroll", "GUIName", "Lore", true),
    WHITE_SCROLL("WhiteScroll", "WhiteScroll", "GUIName", "Lore", true),
    TRANSMOG_SCROLL("TransmogScroll", "TransmogScroll", "GUIName", "Lore", true),
    SLOT_CRYSTAL("Slot_Crystal", "Slot_Crystal", "GUIName", "GUILore", true);
    
    private static final Map<ShopOption, Option> shopOptions = new HashMap<>();
    private final String optionPath;
    private final String path;
    private final String namePath;
    private final String lorePath;
    private final boolean buyable;
    
    ShopOption(final String optionPath, final String path, final String namePath, final String lorePath, final boolean buyable) {
        this.optionPath = optionPath;
        this.path = path;
        this.namePath = namePath;
        this.lorePath = lorePath;
        this.buyable = buyable;
    }

    private final static CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final static ComponentLogger logger = plugin.getComponentLogger();
    
    public static void loadShopOptions(final CommentedConfigurationNode config) {
        shopOptions.clear();

        for (final ShopOption shopOption : values()) {
            final CommentedConfigurationNode itemNode = config.node("Settings", shopOption.getPath());
            final CommentedConfigurationNode costNode = config.node("Settings", "Costs", shopOption.getPath());

            try {
                final Option option = new Option(
                        new ItemBuilder().setMaterial(itemNode.node("Item").getString("CHEST")).setName(itemNode.node(shopOption.getNamePath()).getString(""))
                                .setLore(Methods.getStringList(itemNode, shopOption.getLorePath()))
                                .setPlayerName(itemNode.node("Player").getString(""))
                                .setGlow(itemNode.node("Glowing").getBoolean(false)),
                        itemNode.node("Slot").getInt(1),
                        itemNode.node("InGUI").getBoolean(true),
                        costNode.node("Cost").getInt(100),
                        Currency.getCurrency(costNode.node("Currency").getString("Vault"))
                );

                shopOptions.put(shopOption, option);
            } catch (final Exception exception) {
                logger.error("The option {} has failed to load.", shopOption.getOptionPath(), exception);
            }
        }
    }
    
    public ItemStack getItem() {
        return getItemBuilder().build();
    }
    
    public ItemBuilder getItemBuilder() {
        return shopOptions.get(this).itemBuilder();
    }
    
    public int getSlot() {
        return shopOptions.get(this).slot();
    }
    
    public boolean isInGUI() {
        return shopOptions.get(this).inGUI();
    }
    
    public int getCost() {
        return shopOptions.get(this).cost();
    }
    
    public Currency getCurrency() {
        return shopOptions.get(this).currency();
    }
    
    private String getOptionPath() {
        return this.optionPath;
    }
    
    private String getPath() {
        return this.path;
    }
    
    private String getNamePath() {
        return this.namePath;
    }
    
    private String getLorePath() {
        return this.lorePath;
    }
    
    public boolean isBuyable() {
        return this.buyable;
    }

    private record Option(ItemBuilder itemBuilder, int slot, boolean inGUI, int cost, Currency currency) {}
}