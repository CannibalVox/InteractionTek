package com.voxtech.matchers.slot;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.matchers.SlotMatcher;

import javax.annotation.Nonnull;

public class ActiveHotbarMatcher extends SlotMatcher.Slot {

    @Nonnull
    public static final BuilderCodec<ActiveHotbarMatcher> CODEC = BuilderCodec
        .builder(ActiveHotbarMatcher.class, ActiveHotbarMatcher::new)
        .documentation("This matcher will succeed if the current target item is in the User entity's active hotbar slot")
        .build();

    @Override
    public boolean test(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        Entity entity = EntityUtils.getEntity(user, commandBuffer);

        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        Inventory inventory = livingEntity.getInventory();
        if (inventory == null) {
            return false;
        }

        return (targetContainer == inventory.getHotbar() && targetSlot == inventory.getActiveHotbarSlot());
    }
}
