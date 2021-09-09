package dev.rosewood.roseloot.loot;

import dev.rosewood.roseloot.loot.condition.LootCondition;
import dev.rosewood.roseloot.loot.item.LootItem;
import dev.rosewood.roseloot.util.NumberProvider;
import java.util.Collections;
import java.util.List;

public class LootEntry implements LootItemGenerator {

    private final List<LootCondition> conditions;
    private final NumberProvider weight;
    private final NumberProvider quality;
    private final List<LootItem<?>> lootItems;

    public LootEntry(List<LootCondition> conditions, NumberProvider weight, NumberProvider quality, List<LootItem<?>> lootItems) {
        this.conditions = conditions;
        this.weight = weight;
        this.quality = quality;
        this.lootItems = lootItems;
    }

    @Override
    public List<LootItem<?>> generate(LootContext context) {
        if (!this.check(context))
            return Collections.emptyList();
        return this.lootItems;
    }

    @Override
    public boolean check(LootContext context) {
        return this.conditions.stream().allMatch(x -> x.check(context));
    }

    /**
     * Gets the weight of this entry taking the quality into account
     *
     * @param context The LootContext
     * @return the weight of this entry
     */
    public int getWeight(LootContext context) {
        return (int) Math.floor(this.weight.getInteger() + this.quality.getInteger() * context.getLuckLevel());
    }

    /**
     * @return true if this entry is weighted
     */
    public boolean isWeighted() {
        return this.weight != null;
    }

}
