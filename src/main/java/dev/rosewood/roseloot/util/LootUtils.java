package dev.rosewood.roseloot.util;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseloot.RoseLoot;
import dev.rosewood.roseloot.loot.ExplosionType;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;

public final class LootUtils {

    private LootUtils() {

    }

    public static final Random RANDOM = new Random();
    private static final String SPAWN_REASON_METADATA_NAME = "spawn_reason";
    private static final String REGEX_DECOLORIZE_HEX = "&x&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])";
    public static final Map<String, Color> FIREWORK_COLORS = new HashMap<String, Color>() {{
        this.put("WHITE", Color.WHITE);
        this.put("SILVER", Color.SILVER);
        this.put("GRAY", Color.GRAY);
        this.put("BLACK", Color.BLACK);
        this.put("RED", Color.RED);
        this.put("MAROON", Color.MAROON);
        this.put("YELLOW", Color.YELLOW);
        this.put("OLIVE", Color.OLIVE);
        this.put("LIME", Color.LIME);
        this.put("GREEN", Color.GREEN);
        this.put("AQUA", Color.AQUA);
        this.put("TEAL", Color.TEAL);
        this.put("BLUE", Color.BLUE);
        this.put("NAVY", Color.NAVY);
        this.put("FUCHSIA", Color.FUCHSIA);
        this.put("PURPLE", Color.PURPLE);
        this.put("ORANGE", Color.ORANGE);
    }};

    /**
     * Checks if a chance between 0 and 100 passes
     *
     * @param chance The chance
     * @return true if the chance passed, otherwise false
     */
    public static boolean checkChance(double chance) {
        return RANDOM.nextDouble() <= chance;
    }

    /**
     * Gets a random value between the given range, inclusively
     *
     * @param min The minimum value
     * @param max The maximum value
     * @return A value between the min and max, inclusively
     */
    public static int randomInRange(int min, int max) {
        if (min == max)
            return min;

        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * Gets a random value between the given range, inclusively
     *
     * @param min The minimum value
     * @param max The maximum value
     * @return A value between the min and max, inclusively
     */
    public static double randomInRange(double min, double max) {
        if (min == max)
            return min;

        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return RANDOM.nextDouble() * (max - min) + min;
    }

    /**
     * Reduces a tool's durability by 1 if applicable
     *
     * @param itemStack The tool
     */
    public static void damageTool(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(itemMeta instanceof Damageable))
            return;

        Damageable damageable = (Damageable) itemMeta;
        damageable.setDamage(damageable.getDamage() + 1);
        itemStack.setItemMeta((ItemMeta) damageable);
    }

    /**
     * Sets the spawn reason for the given LivingEntity.
     * Does not overwrite an existing spawn reason.
     *
     * @param entity The entity to set the spawn reason of
     * @param spawnReason The spawn reason to set
     */
    public static void setEntitySpawnReason(LivingEntity entity, SpawnReason spawnReason) {
        RosePlugin rosePlugin = RoseLoot.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME);
            dataContainer.set(key, PersistentDataType.STRING, spawnReason.name());
        } else {
            entity.setMetadata(SPAWN_REASON_METADATA_NAME, new FixedMetadataValue(rosePlugin, spawnReason.name()));
        }
    }

    /**
     * Gets the spawn reason of the given LivingEntity
     *
     * @param entity The entity to get the spawn reason of
     * @return The SpawnReason, or SpawnReason.CUSTOM if none is saved
     */
    public static SpawnReason getEntitySpawnReason(LivingEntity entity) {
        RosePlugin rosePlugin = RoseLoot.getInstance();
        if (NMSUtil.getVersionNumber() > 13) {
            String reason = entity.getPersistentDataContainer().get(new NamespacedKey(rosePlugin, SPAWN_REASON_METADATA_NAME), PersistentDataType.STRING);
            SpawnReason spawnReason;
            if (reason != null) {
                try {
                    spawnReason = SpawnReason.valueOf(reason);
                } catch (Exception ex) {
                    spawnReason = SpawnReason.CUSTOM;
                }
            } else {
                spawnReason = SpawnReason.CUSTOM;
            }
            return spawnReason;
        } else {
            List<MetadataValue> metaValues = entity.getMetadata(SPAWN_REASON_METADATA_NAME);
            SpawnReason spawnReason = null;
            for (MetadataValue meta : metaValues) {
                try {
                    spawnReason = SpawnReason.valueOf(meta.asString());
                    break;
                } catch (Exception ignored) { }
            }
            return spawnReason != null ? spawnReason : SpawnReason.CUSTOM;
        }
    }

    public static List<File> listFiles(File current, List<String> excludedDirectories, List<String> extensions) {
        List<File> listedFiles = new ArrayList<>();
        File[] files = current.listFiles();
        if (files == null)
            return listedFiles;

        for (File file : files) {
            if (file.isDirectory() && !excludedDirectories.contains(file.getName())) {
                listedFiles.addAll(listFiles(file, excludedDirectories, extensions));
            } else if (file.isFile() && extensions.stream().anyMatch(x -> file.getName().endsWith(x))) {
                listedFiles.add(file);
            }
        }

        return listedFiles;
    }

    public static String getFileName(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        return index == -1 ? name : name.substring(0, index);
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Propagates the killer up the stack until we find the ultimate cause of an Entity's death
     *
     * @param entity The Entity to propagate up from
     * @return The Entity that ultimated caused the death
     */
    public static Entity propagateKiller(Entity entity) {
        if (entity instanceof LivingEntity && ((LivingEntity) entity).getKiller() != null)
            return ((LivingEntity) entity).getKiller();

        if (entity instanceof TNTPrimed) {
            // Propagate the igniter of the tnt up the stack
            TNTPrimed tntPrimed = (TNTPrimed) entity;
            Entity tntSource = tntPrimed.getSource();
            if (tntSource != null)
                entity = tntSource;
        }

        if (entity instanceof Projectile) {
            // Check for the projectile type first, if not fall back to the shooter
            Projectile projectile = (Projectile) entity;
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Entity)
                entity = (Entity) source;
        }

        if (entity instanceof Tameable) {
            // Propagate to the tamed entity's owner (if they're online)
            Tameable tameable = (Tameable) entity;
            AnimalTamer tamer = tameable.getOwner();
            if (tamer != null) {
                Player player = Bukkit.getPlayer(tamer.getUniqueId());
                if (player != null)
                    entity = player;
            }
        }

        return entity;
    }

    /**
     * Converts Minecraft color code characters into ampersands
     *
     * @param string The string to convert
     * @return a copy of the String with Minecraft color code characters replaced as ampersands
     */
    public static String decolorize(String string) {
        if (string == null || string.isEmpty())
            return string;
        return string.replace(ChatColor.COLOR_CHAR, '&').replaceAll(REGEX_DECOLORIZE_HEX, "#$1$2$3$4$5$6");
    }

    public static String getToMaximumDecimals(double value, int decimals) {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');

        DecimalFormat decimalFormat = new DecimalFormat("0." + new String(new char[decimals]).replace('\0', '#'), decimalFormatSymbols);
        decimalFormat.setGroupingUsed(false);
        return decimalFormat.format(value);
    }

    public static <T extends Keyed> Set<T> getTags(String value, Class<T> clazz, String registry) {
        NamespacedKey key = NamespacedKey.fromString(value);
        if (key != null) {
            Tag<T> tag = Bukkit.getTag(registry, key, clazz);
            if (tag != null)
                return tag.getValues();
        }
        return null;
    }

    /**
     * Gets an Entity's luck level
     *
     * @param entity The Entity to get the luck level of
     * @param includeFishingLuck Whether to include fishing luck
     * @return The luck level of the LivingEntity
     */
    public static double getEntityLuck(Entity entity, boolean includeFishingLuck) {
        if (!(entity instanceof LivingEntity))
            return 0;

        LivingEntity livingEntity = (LivingEntity) entity;

        double luck = 0;
        AttributeInstance attribute = livingEntity.getAttribute(Attribute.GENERIC_LUCK);
        if (attribute != null)
            luck += attribute.getValue();

        if (includeFishingLuck) {
            EntityEquipment equipment = livingEntity.getEquipment();
            if (equipment != null) {
                if (equipment.getItemInMainHand().getType() == Material.FISHING_ROD && equipment.getItemInMainHand().getItemMeta() != null) {
                    luck += equipment.getItemInMainHand().getItemMeta().getEnchantLevel(Enchantment.LUCK);
                } else if (equipment.getItemInOffHand().getType() == Material.FISHING_ROD && equipment.getItemInOffHand().getItemMeta() != null) {
                    luck += equipment.getItemInOffHand().getItemMeta().getEnchantLevel(Enchantment.LUCK);
                }
            }
        }

        return luck;
    }

    /**
     * Gets an Entity's luck level
     *
     * @param entity The Entity to get the luck level of
     * @return The luck level of the Entity
     */
    public static double getEntityLuck(Entity entity) {
        return getEntityLuck(entity, false);
    }

    /**
     * Gets the explosion type from a killed entity
     *
     * @param entity The killed entity
     * @return The explosion type
     */
    public static ExplosionType getDeathExplosionType(LivingEntity entity) {
        EntityDamageEvent event = entity.getLastDamageCause();
        if (event == null)
            return null;

        switch (event.getCause()) {
            case BLOCK_EXPLOSION:
                return ExplosionType.BLOCK;
            case ENTITY_EXPLOSION:
                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
                    Entity damager = entityDamageByEntityEvent.getDamager();
                    if (damager.getType() == EntityType.CREEPER)
                        return ((Creeper) damager).isPowered() ? ExplosionType.CHARGED_ENTITY : ExplosionType.ENTITY;
                }
                return ExplosionType.ENTITY;
            default:
                return null;
        }
    }
}
