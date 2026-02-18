package com.voxtech.matchers.slot;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.matchers.SlotMatcher;

import javax.annotation.Nonnull;

public class IndexedSlotMatcher extends SlotMatcher.Slot {

    @Nonnull
    public static final BuilderCodec<IndexedSlotMatcher> CODEC = BuilderCodec
        .builder(IndexedSlotMatcher.class, IndexedSlotMatcher::new)
        .documentation("This matcher will pass if the target item occupies a slot in its inventory section with the provided index")
        .append(new KeyedCodec<>("SlotIndex", Codec.INTEGER),
            (object, slotIndex) -> object.slotIndex = slotIndex,
            object -> object.slotIndex)
            .documentation("The index to compare against the target item's slot index")
            .addValidator(Validators.greaterThanOrEqual(0))
            .add()
        .build();

    private int slotIndex;

    @Override
    public boolean test(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, ItemStack itemInHand, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        return slotIndex == targetSlot;
    }
}
