package com.badbones69.crazyenchantments.paper.tasks.processors;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

public class ArmorProcessor extends PoolProcessor {

    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final Server server = this.plugin.getServer();

    private final Starter starter = this.plugin.getStarter();

    private final Methods methods = this.starter.getMethods();

    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    public ArmorProcessor() {}

    public void add(final UUID id){
        add(() -> process(id));
    }

    public void process(final UUID playerId) {
        final Player player = this.server.getPlayer(playerId);

        if (player == null) return;

        for (final ItemStack armor : Objects.requireNonNull(player.getEquipment()).getArmorContents()) {
            final Map<CEnchantment, Integer> enchantments = this.enchantmentBookSettings.getEnchantments(armor);

            if (enchantments.isEmpty()) continue;

            int heal = 1;
            // Uses getValue as if the player has health boost it is modifying the base so the value after the modifier is needed.
            final double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue(); //todo() yes

            if (maxHealth > player.getHealth() && player.getHealth() > 0) {
                checkNursery(armor, player, enchantments, heal, maxHealth);
            }

            if (player.getFoodLevel() < 20) {
                checkImplants(armor, player, enchantments);
            }

            checkCommander(armor, player, enchantments);

            if (PluginSupport.SupportedPlugins.FACTIONS_UUID.isPluginLoaded()) {
                final int radius = 4 + enchantments.get(CEnchantments.ANGEL.getEnchantment());

                checkAngel(armor, player, enchantments, radius);
            }

            useHellForge(player, armor, enchantments);
        }

        final PlayerInventory inventory = player.getInventory();

        useHellForge(player, inventory.getItemInMainHand(), this.enchantmentBookSettings.getEnchantments(inventory.getItemInMainHand()));
        useHellForge(player, inventory.getItemInOffHand(), this.enchantmentBookSettings.getEnchantments(inventory.getItemInOffHand()));
    }

    private void checkCommander(final ItemStack armor, final Player player, final Map<CEnchantment, Integer> enchantments) {
        if (!EnchantUtils.isMoveEventActive(CEnchantments.COMMANDER, player, enchantments)) return;

        int radius = 4 + enchantments.get(CEnchantments.COMMANDER.getEnchantment());

        player.getScheduler().run(this.plugin, playerTask -> { //todo() fusion api
            if (EnchantUtils.normalEnchantEvent(CEnchantments.COMMANDER, player, armor)) {
                final PotionEffect fastDigging = new PotionEffect(PotionEffectType.HASTE, 3 * 20, 1);

                for (final Entity e : player.getNearbyEntities(radius, radius, radius)) {
                    e.getScheduler().run(plugin, task -> { //todo() use fusion api
                        if (e instanceof Player otherPlayer && this.pluginSupport.isFriendly(player, otherPlayer)) {
                            otherPlayer.addPotionEffect(fastDigging);
                        }
                    }, null);
                }
            }
        }, null);
    }

    private void checkAngel(final ItemStack armor, final Player player, final Map<CEnchantment, Integer> enchantments, final int radius) {
        if (!EnchantUtils.isMoveEventActive(CEnchantments.ANGEL, player, enchantments)) return;

        player.getScheduler().run(this.plugin, playerTask -> { //todo() fusion api\
            if (!EnchantUtils.normalEnchantEvent(CEnchantments.ANGEL, player, armor)) return;

            final List<Player> players = new ArrayList<>();

            for (final Entity entity : player.getNearbyEntities(radius, radius, radius)) { //todo() fusion api
                entity.getScheduler().run(plugin, (task) -> {
                    if (entity instanceof Player otherPlayer && this.pluginSupport.isFriendly(player, otherPlayer)) {
                        players.add(otherPlayer);
                    }
                }, null);
            }


            if (players.isEmpty()) return;

            final PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 3 * 20, 0);

            for (Player p : players) {
                p.getScheduler().run(plugin, task -> p.addPotionEffect(regeneration), null); //todo() fusion api
            }
        }, null);
    }

    private void checkImplants(final ItemStack armor, final Player player, final Map<CEnchantment, Integer> enchantments) {
        if (!EnchantUtils.isMoveEventActive(CEnchantments.IMPLANTS, player, enchantments)) return;

        player.getScheduler().run(this.plugin, playerTask -> { //todo() fusion api
            if (EnchantUtils.normalEnchantEvent(CEnchantments.IMPLANTS, player, armor)) {
                player.setFoodLevel(Math.min(20, player.getFoodLevel() + enchantments.get(CEnchantments.IMPLANTS.getEnchantment())));
            }
        }, null);
    }

    private void checkNursery(final ItemStack armor, final Player player, final Map<CEnchantment, Integer> enchantments, final int heal, final double maxHealth) {
        if (!EnchantUtils.isMoveEventActive(CEnchantments.NURSERY, player, enchantments)) return;

        player.getScheduler().run(this.plugin, playerTask -> { //todo() fusion api
            if (EnchantUtils.normalEnchantEvent(CEnchantments.NURSERY, player, armor)) {
                if (player.getHealth() + heal <= maxHealth) player.setHealth(player.getHealth() + heal);
                if (player.getHealth() + heal >= maxHealth) player.setHealth(maxHealth);
            }
        }, null);
    }

    private void useHellForge(final Player player, final ItemStack item, final Map<CEnchantment, Integer> enchantments) {
        if (!EnchantUtils.isMoveEventActive(CEnchantments.HELLFORGED, player, enchantments)) return;

        player.getScheduler().run(this.plugin, playerTask -> { //todo() fusion api
            if (!EnchantUtils.normalEnchantEvent(CEnchantments.HELLFORGED, player, item)) return;

            final int armorDurability = this.methods.getDurability(item);

            if (armorDurability <= 0) return;

            int finalArmorDurability = armorDurability;

            finalArmorDurability -= enchantments.get(CEnchantments.HELLFORGED.getEnchantment());

            this.methods.setDurability(item, finalArmorDurability);
        }, null);
    }
}