package com.voxtech.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;

public class EmptySlotMatcher extends ItemCondition.ItemMatcher {
    public static final BuilderCodec<EmptySlotMatcher> CODEC = BuilderCodec.builder(EmptySlotMatcher.class, EmptySlotMatcher::new, BASE_CODEC)
            .documentation("This matcher only succeeds if a null item is passed to it.")
            .build();

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, ItemStack itemInHand, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        return targetItem == null;
    }

    @Override
    public boolean failEmptyItem() {
        return false;
    }
}
