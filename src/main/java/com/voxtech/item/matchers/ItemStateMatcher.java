package com.voxtech.item.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;

import javax.annotation.Nonnull;

public class ItemStateMatcher extends ItemConditionInteraction.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<ItemStateMatcher> CODEC = BuilderCodec.builder(
        ItemStateMatcher.class, ItemStateMatcher::new, BASE_CODEC
    )
        .documentation("Matcher succeeds if the target item is in any of the provided item states. 'default' means no state")
        .append(new KeyedCodec<>("ItemStates", new ArrayCodec<>(Codec.STRING, String[]::new)),
            (object, states) -> object.itemStates = states,
            object -> object.itemStates)
            .documentation("The item states to test the target item against")
            .addValidator(Validators.nonNull())
            .add()
        .build();

    private String[] itemStates;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        if (itemStates.length == 0) {
            return false;
        }

        String state = targetItem.getItem().getStateForItem(targetItem.getItemId());
        if (state == null) {
            state = "default";
        }

        for (String itemState : itemStates) {
            if (itemState.equals(state)) {
                return true;
            }
        }

        return false;
    }
}
