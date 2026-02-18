package com.voxtech.matchers;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;

import javax.annotation.Nonnull;
import java.util.Map;

public class InteractionMatcher extends ItemCondition.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<InteractionMatcher> CODEC = BuilderCodec
            .builder(InteractionMatcher.class, InteractionMatcher::new, BASE_CODEC)
            .documentation("This matcher will succeed if the target item has any interactions")
            .append(new KeyedCodec<>("InteractionTypes", new ArrayCodec<>(new EnumCodec<>(InteractionType.class), InteractionType[]::new)),
                (object, interactionTypes) -> object.interactionTypes = interactionTypes,
                object -> object.interactionTypes)
                .documentation("If provided, the matcher will only succeed if the item has an interaction that matches one of the InteractionTypes provided")
                .add()
            .build();

    private InteractionType[] interactionTypes;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, ItemStack itemInHand, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        Map<InteractionType, String> itemInteractions = targetItem.getItem().getInteractions();

        if (itemInteractions == null || itemInteractions.isEmpty()) {
            return false;
        }

        if (interactionTypes == null || interactionTypes.length == 0) {
            return true;
        }

        for (InteractionType interactionType : interactionTypes) {
            if (itemInteractions.containsKey(interactionType)) {
                return true;
            }
        }

        return false;
    }
}
