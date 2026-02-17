package com.voxtech;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.voxtech.interactions.ItemCondition;
import com.voxtech.matchers.DurabilityMatcher;
import com.voxtech.matchers.EmptySlotMatcher;
import com.voxtech.matchers.QuantityMatcher;

import javax.annotation.Nonnull;

public class InteractionTekPlugin extends JavaPlugin {

    public InteractionTekPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Interaction.CODEC.register("ItemCondition", ItemCondition.class, ItemCondition.CODEC);

        ItemCondition.ItemMatcher.CODEC.register("Durability", DurabilityMatcher.class, DurabilityMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("EmptySlot", EmptySlotMatcher.class, EmptySlotMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Quantity", QuantityMatcher.class, QuantityMatcher.CODEC);
    }

    @Override
    protected void shutdown() {
        ItemCondition.ItemMatcher.CODEC.remove(DurabilityMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(EmptySlotMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(QuantityMatcher.class);

        Interaction.CODEC.remove(ItemCondition.class);
    }
}