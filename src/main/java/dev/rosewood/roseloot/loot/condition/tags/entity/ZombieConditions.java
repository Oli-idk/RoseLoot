package dev.rosewood.roseloot.loot.condition.tags.entity;

import dev.rosewood.roseloot.event.LootConditionRegistrationEvent;
import dev.rosewood.roseloot.loot.condition.EntityConditions;
import org.bukkit.entity.Zombie;

public class ZombieConditions extends EntityConditions {

    public ZombieConditions(LootConditionRegistrationEvent event) {
        event.registerLootCondition("zombie-converting", context -> context.getLootedEntity() instanceof Zombie && ((Zombie) context.getLootedEntity()).isConverting());
    }

}
