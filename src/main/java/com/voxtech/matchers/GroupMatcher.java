package com.voxtech.matchers;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;
import com.voxtech.protocol.ItemMatchType;

import javax.annotation.Nonnull;

public class GroupMatcher extends ItemCondition.ItemMatcher {
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
        .append(new KeyedCodec<>("Matchers", new ArrayCodec<>(ItemCondition.ItemMatcher.CODEC, ItemCondition.ItemMatcher[]::new)),
            (object, matchers) -> object.itemMatchers = matchers,
            object -> object.itemMatchers)
        .documentation("These matchers test the target item to decide if the interaction fails")
        .add()
        .build();

    private ItemMatchType itemMatchType;
    private ItemCondition.ItemMatcher[] itemMatchers;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, ItemStack itemInHand, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        for (ItemCondition.ItemMatcher matcher : itemMatchers) {
            boolean result = matcher.test(user, commandBuffer, itemInHand, targetContainer, targetSlot, targetItem);

            if (result && itemMatchType == ItemMatchType.Any) {
                return true;
            }

            if (!result && itemMatchType == ItemMatchType.All) {
                return false;
            }
        }

        return (itemMatchType != ItemMatchType.Any);
    }
}
