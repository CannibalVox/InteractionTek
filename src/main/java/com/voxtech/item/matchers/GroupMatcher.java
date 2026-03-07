package com.voxtech.item.matchers;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.InventoryHelper;
import com.voxtech.protocol.ItemMatchType;
import com.voxtech.protocol.ItemMatcher;

import javax.annotation.Nonnull;

public class GroupMatcher extends ItemMatcher {
    @Nonnull
    public static final BuilderCodec<GroupMatcher> CODEC = BuilderCodec.builder(
            GroupMatcher.class, GroupMatcher::new, BASE_CODEC
        )
        .documentation("This matcher will succeed based on its sub-matchers, allowing complex logic to be built")
        .append(new KeyedCodec<>("ItemMatchType", new EnumCodec<>(ItemMatchType.class)),
            (interaction, matchType) -> interaction.itemMatchType = matchType,
            interaction -> interaction.itemMatchType)
        .documentation("Whether all, any, or no matchers need to match for this interaction to succeed")
        .add()
        .appendInherited(new KeyedCodec<>("Matchers", new ArrayCodec<>(ItemMatcher.CODEC, ItemMatcher[]::new)),
            (object, matchers) -> object.itemMatchers = matchers,
            object -> object.itemMatchers,
            (object, parent) -> object.itemMatchers = parent.itemMatchers)
        .documentation("These matchers test the target item to decide if the interaction fails")
        .add()
        .build();

    private ItemMatchType itemMatchType;
    private ItemMatcher[] itemMatchers;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        return InventoryHelper.executeMatchers(itemMatchers, itemMatchType, user, commandBuffer, context, targetContainer, (short)targetSlot, targetItem);
    }
}
