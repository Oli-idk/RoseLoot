package dev.rosewood.roseloot.event;

import dev.rosewood.roseloot.loot.condition.LootCondition;
import dev.rosewood.roseloot.loot.context.LootContext;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event that gets called during the RoseLoot LootCondition registration
 */
public class LootConditionRegistrationEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Map<String, Constructor<? extends LootCondition>> registeredConditionConstructors;
    private final Map<String, Predicate<LootContext>> registeredConditionPredicates;
    private final Map<String, BiPredicate<LootContext, List<String>>> registeredConditionStringPredicates;

    public LootConditionRegistrationEvent() {
        this.registeredConditionConstructors = new HashMap<>();
        this.registeredConditionPredicates = new HashMap<>();
        this.registeredConditionStringPredicates = new HashMap<>();
    }

    /**
     * @return an unmodifiable map of registered LootCondition constructors
     */
    @NotNull
    public Map<String, Constructor<? extends LootCondition>> getRegisteredLootConditionConstructors() {
        return Collections.unmodifiableMap(this.registeredConditionConstructors);
    }

    /**
     * @return an unmodifiable map of registered LootCondition predicates
     */
    @NotNull
    public Map<String, Predicate<LootContext>> getRegisteredConditionPredicates() {
        return Collections.unmodifiableMap(this.registeredConditionPredicates);
    }

    /**
     * @return an unmodifiable map of registered LootCondition string predicates
     */
    @NotNull
    public Map<String, BiPredicate<LootContext, List<String>>> getRegisteredConditionStringPredicates() {
        return Collections.unmodifiableMap(this.registeredConditionStringPredicates);
    }

    /**
     * Registers a LootCondtion by its Class, overwriting any existing LootConditions with the same name.
     * The given class must have a constructor accepting a single String.
     *
     * @param name The name of the LootCondition to register
     * @param lootConditionClass The class of the LootCondition
     * @return true if registering the new LootCondition overwrote a different LootCondition with the same name, false otherwise
     * @throws IllegalArgumentException if a valid constructor is not found in the given class
     */
    public boolean registerLootCondition(@NotNull String name, @NotNull Class<? extends LootCondition> lootConditionClass) {
        String tagName = name.toLowerCase();
        boolean overwrote = this.registeredConditionPredicates.remove(tagName) != null || this.registeredConditionStringPredicates.remove(tagName) != null;
        try {
            overwrote |= this.registeredConditionConstructors.put(tagName, lootConditionClass.getConstructor(String.class)) != null;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Missing constructor with a single String parameter", ex);
        }
        return overwrote;
    }

    /**
     * Registers a LootCondition from a Predicate, overwriting any existing LootConditions with the same name.
     * This LootCondition does not have any parameters.
     *
     * @param name The name of the LootCondition to register
     * @param predicate The predicate of the LootCondition
     * @return true if registering the new LootCondition overwrote a different LootCondition with the same name, false otherwise
     */
    public boolean registerLootCondition(@NotNull String name, @NotNull Predicate<LootContext> predicate) {
        String tagName = name.toLowerCase();
        return this.registeredConditionPredicates.put(tagName, predicate) != null
                || this.registeredConditionStringPredicates.remove(tagName) != null
                || this.registeredConditionConstructors.remove(tagName) != null;
    }

    /**
     * Registers a LootCondition from a BiPredicate, overwriting any existing LootConditions with the same name.
     * This LootCondition has a single String parameter.
     *
     * @param name The name of the LootCondition to register
     * @param predicate The predicate of the LootCondition
     * @return true if registering the new LootCondition overwrote a different LootCondition with the same name, false otherwise
     */
    public boolean registerLootCondition(@NotNull String name, @NotNull BiPredicate<LootContext, @NotNull List<String>> predicate) {
        String tagName = name.toLowerCase();
        return this.registeredConditionStringPredicates.put(tagName, predicate) != null
                || this.registeredConditionPredicates.remove(tagName) != null
                || this.registeredConditionConstructors.remove(tagName) != null;
    }

    /**
     * Unregisters a LootCondition
     *
     * @param name The name of the LootCondition to unregister
     * @return true if a LootCondition was unregistered, false otherwise
     */
    public boolean unregisterLootCondition(@NotNull String name) {
        String tagName = name.toLowerCase();
        return this.registeredConditionConstructors.remove(tagName) != null
                || this.registeredConditionPredicates.remove(tagName) != null
                || this.registeredConditionStringPredicates.remove(tagName) != null;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
