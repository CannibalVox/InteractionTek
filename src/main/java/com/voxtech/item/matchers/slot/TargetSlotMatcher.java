package com.voxtech.item.matchers.slot;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.ItemTargetHelper;
import com.voxtech.item.matchers.SlotMatcher;

import javax.annotation.Nonnull;

public class TargetSlotMatcher extends SlotMatcher.Slot {

    @Nonnull
    public static final BuilderCodec<TargetSlotMatcher> CODEC = BuilderCodec
            .builder(TargetSlotMatcher.class, TargetSlotMatcher::new, BASE_CODEC)
            .documentation("This matcher will pass if the target item is in the interaction chain's currently-targeted slot")
            .build();

    @Override
    public boolean test(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        ItemTargetHelper.TargetItemData data = ItemTargetHelper.getTargetItem(context);
        return data.getContainer() == targetContainer && data.getSlot() == targetSlot;
    }
}
