package com.voxtech.item.matchers;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;

import javax.annotation.Nonnull;

public class SlotMatcher extends ItemConditionInteraction.ItemMatcher {

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
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        for (Slot slot : slots) {
            if (slot.test(user, commandBuffer, context, targetContainer, targetSlot, targetItem)) {
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
        public static final BuilderCodec<Slot> BASE_CODEC = BuilderCodec
            .abstractBuilder(Slot.class)
            .build();

        public abstract boolean test(Ref<EntityStore> user,  CommandBuffer<EntityStore> commandBuffer, InteractionContext context,ItemContainer targetContainer, int targetSlot, ItemStack targetItem);
    }
}
