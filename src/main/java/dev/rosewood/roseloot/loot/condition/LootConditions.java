package dev.rosewood.roseloot.loot.condition;

import dev.rosewood.roseloot.loot.condition.tags.BiomeCondition;
import dev.rosewood.roseloot.loot.condition.tags.BlockTypeCondition;
import dev.rosewood.roseloot.loot.condition.tags.ChanceCondition;
import dev.rosewood.roseloot.loot.condition.tags.ChargedExplosionCondition;
import dev.rosewood.roseloot.loot.condition.tags.DeathCauseCondition;
import dev.rosewood.roseloot.loot.condition.tags.EnchantmentChanceCondition;
import dev.rosewood.roseloot.loot.condition.tags.EnchantmentCondition;
import dev.rosewood.roseloot.loot.condition.tags.EntityTypeCondition;
import dev.rosewood.roseloot.loot.condition.tags.FullyGrownCondition;
import dev.rosewood.roseloot.loot.condition.tags.KilledByCondition;
import dev.rosewood.roseloot.loot.condition.tags.RequiredToolCondition;
import dev.rosewood.roseloot.loot.condition.tags.RequiredToolTypeCondition;
import dev.rosewood.roseloot.loot.condition.tags.SpawnReasonCondition;
import dev.rosewood.roseloot.loot.condition.tags.WorldCondition;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LootConditions {

    private static final Map<String, Constructor<? extends LootCondition>> tagPrefixMap = new HashMap<>();
    private static final Map<String, String> tagDescriptionMap = new LinkedHashMap<>();

    static {
        registerTag("biome", BiomeCondition.class, "Biome the loot must be generated in");
        registerTag("block-type", BlockTypeCondition.class, "Type the looted block must be");
        registerTag("chance", ChanceCondition.class, "A random chance");
        registerTag("charged-explosion", ChargedExplosionCondition.class, "Must be caused by a charged creeper");
        registerTag("death-cause", DeathCauseCondition.class, "Type of damage cause that caused the death");
        registerTag("enchantment-chance", EnchantmentChanceCondition.class, "A random chance with additional chance based on enchantment level");
        registerTag("enchantment", EnchantmentCondition.class, "Type and level of enchantment required");
        registerTag("entity-type", EntityTypeCondition.class, "Type the looted entity must be");
        registerTag("fully-grown", FullyGrownCondition.class, "The animal or crop must be fully grown");
        registerTag("killed-by", KilledByCondition.class, "Type of entity that killed the entity");
        registerTag("required-tool", RequiredToolCondition.class, "Type and quality of tool required");
        registerTag("required-tool-type", RequiredToolTypeCondition.class, "Exact type of tool required");
        registerTag("spawn-reason", SpawnReasonCondition.class, "Spawn reason the entity must have");
        registerTag("world", WorldCondition.class, "Loot must be generated in a specific world");
    }

    public static Map<String, String> getTagDescriptionMap() {
        return Collections.unmodifiableMap(tagDescriptionMap);
    }

    private static <T extends LootCondition> void registerTag(String prefix, Class<T> tagClass, String description) {
        try {
            tagPrefixMap.put(prefix, tagClass.getConstructor(String.class));
            if (description != null)
                tagDescriptionMap.put(prefix, description);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    public static LootCondition parse(String tag) {
        try {
            String parsed = (tag.startsWith("!") ? tag.substring(1) : tag).toLowerCase();
            int index = parsed.indexOf(":");
            if (index == -1) {
                return tagPrefixMap.get(parsed).newInstance(tag);
            } else {
                return tagPrefixMap.get(parsed.substring(0, index)).newInstance(tag);
            }
        } catch (Exception e) {
            return null;
        }
    }

}
