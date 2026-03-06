package com.voxtech.item.matchers.slot;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.item.matchers.SlotMatcher;

import javax.annotation.Nonnull;

public class ActiveUtilityMatcher extends SlotMatcher.Slot {

    @Nonnull
    public static final BuilderCodec<ActiveUtilityMatcher> CODEC = BuilderCodec
            .builder(ActiveUtilityMatcher.class, ActiveUtilityMatcher::new, BASE_CODEC)
            .documentation("This matcher will succeed if the current target item is in the User entity's active utility slot")
            .build();

    @Override
    public boolean test(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        InventoryComponent.Utility utility = commandBuffer.getComponent(user, InventoryComponent.Utility.getComponentType());
        if (utility == null) {
            return false;
        }

        return (targetContainer == utility.getInventory() && targetSlot == utility.getActiveSlot());
    }
}
