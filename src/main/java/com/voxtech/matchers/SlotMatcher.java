package com.voxtech.matchers;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;

import javax.annotation.Nonnull;

public class SlotMatcher extends ItemCondition.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<SlotMatcher> CODEC = BuilderCodec
        .builder(SlotMatcher.class, SlotMatcher::new, BASE_CODEC)
        .documentation("This matcher succeeds if the target item slot matches any of the provided slot data conditions")
        .append(new KeyedCodec<>("Slots", new ArrayCodec<>(Slot.CODEC, Slot[]::new)),
            (object, slot) -> object.slots = slot,
            object -> object.slots)
            .documentation("The slot data to compare against the target item's slot")
            .addValidator(Validators.nonNull())
            .add()
        .build();

    private Slot[] slots;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, ItemStack itemInHand, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        for (Slot slot : slots) {
            if (slot.test(user, commandBuffer, itemInHand, targetContainer, targetSlot, targetItem)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean failEmptyItem() {
        return false;
    }

    public abstract static class Slot {
        public static final CodecMapCodec<SlotMatcher.Slot> CODEC = new CodecMapCodec<>("Type");

        public abstract boolean test(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, ItemStack itemInHand, ItemContainer targetContainer, int targetSlot, ItemStack targetItem);
    }
}
