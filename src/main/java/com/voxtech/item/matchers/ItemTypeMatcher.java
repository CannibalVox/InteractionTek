package com.voxtech.item.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;

import javax.annotation.Nonnull;

public class ItemTypeMatcher extends ItemConditionInteraction.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<ItemTypeMatcher> CODEC = BuilderCodec.<ItemTypeMatcher>builder(
        ItemTypeMatcher.class, ItemTypeMatcher::new, BASE_CODEC
    )
        .documentation("Matcher succeeds if the target item is of any of the provided item types")
        .appendInherited(new KeyedCodec<>("ItemTypes", new ArrayCodec<>(Codec.STRING, String[]::new)),
            (object, types) -> object.itemTypes = types,
            object -> object.itemTypes,
            (object, parent) -> object.itemTypes = parent.itemTypes)
            .documentation("List of item types to allow")
            .addValidator(Item.VALIDATOR_CACHE.getArrayValidator().late())
            .addValidator(Validators.nonNull())
            .add()
        .build();

    private String[] itemTypes;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        if (itemTypes.length == 0) {
            return false;
        }

        String actualItemId = targetItem.getItemId();
        for (String expectedItemId : itemTypes) {
            if (actualItemId.equals(expectedItemId)) {
                return true;
            }
        }
        return false;
    }
}
