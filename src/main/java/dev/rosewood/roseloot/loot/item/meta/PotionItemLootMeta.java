package dev.rosewood.roseloot.loot.item.meta;

import dev.rosewood.roseloot.loot.LootContext;
import dev.rosewood.roseloot.util.NumberProvider;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PotionItemLootMeta extends ItemLootMeta {

    private Color color;
    private final PotionType potionType;
    private final boolean extended;
    private final boolean upgraded;
    private Map<PotionEffectData, Boolean> customEffects;

    public PotionItemLootMeta(ConfigurationSection section) {
        super(section);

        String colorString = section.getString("color");
        if (colorString != null) {
            try {
                java.awt.Color awtColor = java.awt.Color.decode(colorString);
                this.color = Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
            } catch (NumberFormatException ignored) { }
        }

        String potionTypeString = section.getString("potion-type");
        PotionType potionType = PotionType.WATER;
        for (PotionType value : PotionType.values()) {
            if (value.name().equalsIgnoreCase(potionTypeString)) {
                potionType = value;
                break;
            }
        }

        this.potionType = potionType;
        this.extended = section.getBoolean("extended", false);
        this.upgraded = section.getBoolean("upgraded", false);

        ConfigurationSection customEffectsSection = section.getConfigurationSection("custom-effects");
        if (customEffectsSection != null) {
            this.customEffects = new LinkedHashMap<>();
            for (String key : customEffectsSection.getKeys(false)) {
                ConfigurationSection customEffectSection = customEffectsSection.getConfigurationSection(key);
                if (customEffectSection == null)
                    continue;

                String effectString = customEffectSection.getString("effect");
                if (effectString == null)
                    continue;

                PotionEffectType effect = PotionEffectType.getByName(effectString);
                if (effect == null)
                    continue;

                NumberProvider duration = NumberProvider.fromSection(customEffectSection, "duration", 200);
                NumberProvider amplifier = NumberProvider.fromSection(customEffectSection, "amplifier", 0);
                boolean ambient = customEffectSection.getBoolean("ambient", false);
                boolean particles = customEffectSection.getBoolean("particles", true);
                boolean icon = customEffectSection.getBoolean("icon", true);
                boolean overwrite = customEffectSection.getBoolean("overwrite", true);

                this.customEffects.put(new PotionEffectData(effect, duration, amplifier, ambient, particles, icon), overwrite);
            }
        }
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext context) {
        super.apply(itemStack, context);

        PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;

        if (this.color != null) itemMeta.setColor(this.color);
        itemMeta.setBasePotionData(new PotionData(this.potionType, this.extended && this.potionType.isExtendable(), this.upgraded && this.potionType.isUpgradeable()));
        if (this.customEffects != null) this.customEffects.forEach((x, y) -> itemMeta.addCustomEffect(x.toPotionEffect(), y));

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public static class PotionEffectData {

        private final PotionEffectType potionEffectType;
        private final NumberProvider duration, amplifier;
        private final boolean ambient, particles, icon;

        public PotionEffectData(PotionEffectType potionEffectType, NumberProvider duration, NumberProvider amplifier, boolean ambient, boolean particles, boolean icon) {
            this.potionEffectType = potionEffectType;
            this.duration = duration;
            this.amplifier = amplifier;
            this.ambient = ambient;
            this.particles = particles;
            this.icon = icon;
        }

        public PotionEffect toPotionEffect() {
            return new PotionEffect(this.potionEffectType, this.duration.getInteger(), this.amplifier.getInteger(), this.ambient, this.particles, this.icon);
        }

    }

}
