package com.badbones69.crazyenchantments.paper.enchantments;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.managers.BowEnchantmentManager;
import com.badbones69.crazyenchantments.paper.api.objects.BowEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.EnchantedArrow;
import com.badbones69.crazyenchantments.paper.api.utils.BowUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EnchantUtils;
import com.badbones69.crazyenchantments.paper.api.utils.EventUtils;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import com.badbones69.crazyenchantments.paper.support.PluginSupport;
import com.ryderbelserion.crazyenchantments.objects.ConfigOptions;
import com.ryderbelserion.fusion.paper.api.scheduler.FoliaScheduler;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import java.util.Map;

public class BowEnchantments implements Listener {

    private final CrazyEnchantments plugin = JavaPlugin.getPlugin(CrazyEnchantments.class);

    private final ConfigOptions options = this.plugin.getOptions();

    private final Server server = this.plugin.getServer();

    private final PluginManager pluginManager = this.server.getPluginManager();

    private final Starter starter = this.plugin.getStarter();

    private final Methods methods = this.starter.getMethods();

    private final EnchantmentBookSettings enchantmentBookSettings = this.starter.getEnchantmentBookSettings();

    private final PluginSupport pluginSupport = this.starter.getPluginSupport();

    private final BowEnchantmentManager bowEnchantmentManager = this.starter.getBowEnchantmentManager();

    private final BowUtils bowUtils = this.starter.getBowUtils();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBowShoot(final EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (EventUtils.isIgnoredEvent(event) || EventUtils.isIgnoredUUID(event.getEntity().getUniqueId())) return;
        if (!(event.getProjectile() instanceof Arrow arrow)) return;

        final ItemStack bow = event.getBow();

        if (!this.bowUtils.allowsCombat(player)) return;

        final Map<CEnchantment, Integer> enchants = this.enchantmentBookSettings.getEnchantments(bow);

        if (enchants.isEmpty()) return;

        // Add the arrow to the list.
        this.bowUtils.addArrow(arrow, bow, enchants);

        // MultiArrow only code below.
        if (EnchantUtils.isEventActive(CEnchantments.MULTIARROW, player, bow, enchants)) {
            final int power = enchants.get(CEnchantments.MULTIARROW.getEnchantment());

            for (int i = 1; i <= power; i++) this.bowUtils.spawnArrows(player, arrow, bow);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLand(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;
        if (!(event.getEntity() instanceof Arrow entityArrow)) return;
        if (!this.bowUtils.allowsCombat(event.getEntity())) return;

        final EnchantedArrow enchantedArrow = this.bowUtils.getEnchantedArrow(entityArrow);

        if (enchantedArrow == null) return;

        // Spawn webs related to STICKY_SHOT.
        this.bowUtils.spawnWebs(event.getHitEntity(), enchantedArrow);

        if (EnchantUtils.isEventActive(CEnchantments.BOOM, shooter, enchantedArrow.bow(), enchantedArrow.enchantments())) {
            this.methods.explode(enchantedArrow.getShooter(), enchantedArrow.arrow());

            enchantedArrow.arrow().remove();
        }

        if (EnchantUtils.isEventActive(CEnchantments.LIGHTNING, shooter, enchantedArrow.bow(), enchantedArrow.enchantments())) {
            final Location location = enchantedArrow.arrow().getLocation();

            final Entity lightning = location.getWorld().strikeLightningEffect(location);

            final int lightningSoundRange = this.options.getLightningSoundRange();

            try {
                location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, (float) lightningSoundRange / 16f, 1);
            } catch (Exception ignore) {}

            for (LivingEntity entity : this.methods.getNearbyLivingEntities(2D, enchantedArrow.arrow())) {
                final EntityDamageEvent damageByEntityEvent = new EntityDamageEvent(entity, DamageCause.LIGHTNING, DamageSource.builder(DamageType.LIGHTNING_BOLT).withCausingEntity(shooter).withDirectEntity(lightning).build(), 5D);

                EventUtils.addIgnoredEvent(damageByEntityEvent);
                EventUtils.addIgnoredUUID(shooter.getUniqueId());

                this.pluginManager.callEvent(damageByEntityEvent);

                if (!damageByEntityEvent.isCancelled() && !this.pluginSupport.isFriendly(enchantedArrow.getShooter(), entity) && !enchantedArrow.getShooter().getUniqueId().equals(entity.getUniqueId())) entity.damage(5D);

                EventUtils.removeIgnoredEvent(damageByEntityEvent);
                EventUtils.removeIgnoredUUID(shooter.getUniqueId());
            }
        }

        // Removes the arrow from the list after 5 ticks. This is done because the onArrowDamage event needs the arrow in the list, so it can check.
        new FoliaScheduler(this.plugin, null, entityArrow) {
            @Override
            public void run() {
                bowUtils.removeArrow(enchantedArrow);
            }
        }.runDelayed(5);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (EventUtils.isIgnoredEvent(event)) return;
        if (!(event.getDamager() instanceof Arrow entityArrow)) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        final EnchantedArrow enchantedArrow = this.bowUtils.getEnchantedArrow(entityArrow);

        if (enchantedArrow == null) return;

        if (!this.pluginSupport.allowCombat(enchantedArrow.arrow().getLocation())) return;
        // Damaged player is friendly.

        if (EnchantUtils.isEventActive(CEnchantments.DOCTOR, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments()) && this.pluginSupport.isFriendly(enchantedArrow.getShooter(), event.getEntity())) {
            final int heal = 1 + enchantedArrow.getLevel(CEnchantments.DOCTOR);
            // Uses getValue as if the player has health boost it is modifying the base so the value after the modifier is needed.
            final double maxHealth = entity.getAttribute(Attribute.MAX_HEALTH).getValue(); //todo() deprecated

            if (entity.getHealth() < maxHealth) {
                if (entity.getHealth() + heal < maxHealth) entity.setHealth(entity.getHealth() + heal);
                if (entity.getHealth() + heal >= maxHealth) entity.setHealth(maxHealth);
            }
        }

        // Damaged player is an enemy.
        if (this.pluginSupport.isFriendly(enchantedArrow.getShooter(), entity)) return;

        this.bowUtils.spawnWebs(event.getEntity(), enchantedArrow);

        if (EnchantUtils.isEventActive(CEnchantments.PULL, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) {
            final Vector v = enchantedArrow.getShooter().getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(3);

            entity.setVelocity(v);
        }

        for (final BowEnchantment bowEnchantment : this.bowEnchantmentManager.getBowEnchantments()) {
            final CEnchantments enchantment = bowEnchantment.getEnchantment();

            if (!EnchantUtils.isEventActive(enchantment, enchantedArrow.getShooter(), enchantedArrow.bow(), enchantedArrow.enchantments())) continue;

            if (bowEnchantment.isPotionEnchantment()) {
                bowEnchantment.getPotionEffects().forEach(effect -> entity.addPotionEffect(new PotionEffect(effect.potionEffect(), effect.duration(),
                        (bowEnchantment.isLevelAddedToAmplifier() ? enchantedArrow.getLevel(enchantment) : 0) + effect.amplifier())));
            } else {
                event.setDamage(event.getDamage() * ((bowEnchantment.isLevelAddedToAmplifier() ? enchantedArrow.getLevel(enchantment) : 0) + bowEnchantment.getDamageAmplifier()));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onWebBreak(BlockBreakEvent event) {
        if (!EventUtils.isIgnoredEvent(event) && this.bowUtils.getWebBlocks().contains(event.getBlock())) event.setCancelled(true);
    }
}