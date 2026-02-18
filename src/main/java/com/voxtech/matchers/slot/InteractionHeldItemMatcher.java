package com.voxtech.matchers.slot;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.matchers.SlotMatcher;

import javax.annotation.Nonnull;

public class InteractionHeldItemMatcher extends SlotMatcher.Slot {

    @Nonnull
    public static final BuilderCodec<InteractionHeldItemMatcher> CODEC = BuilderCodec
            .builder(InteractionHeldItemMatcher.class, InteractionHeldItemMatcher::new)
            .documentation("This matcher will pass if the target item is the current interaction chain's held item. The held item is the target item's default value.")
            .build();

    @Override
    public boolean test(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        return (targetContainer == context.getHeldItemContainer() && targetSlot == context.getHeldItemSlot());
    }
}
