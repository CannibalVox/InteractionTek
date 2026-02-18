package com.voxtech.matchers;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;

import javax.annotation.Nonnull;

public class GliderMatcher extends ItemCondition.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<GliderMatcher> CODEC = BuilderCodec
        .builder(GliderMatcher.class, GliderMatcher::new, BASE_CODEC)
        .documentation("this matcher succeeds if the target item is a glider")
        .build();

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, ItemStack itemInHand, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        return targetItem.getItem().getGlider() != null;
    }
}
