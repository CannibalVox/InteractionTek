package com.voxtech.matchers;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;

import javax.annotation.Nonnull;

public class DropOnDeathMatcher extends ItemCondition.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<DropOnDeathMatcher> CODEC = BuilderCodec
        .builder(DropOnDeathMatcher.class, DropOnDeathMatcher::new, BASE_CODEC)
        .documentation("This matcher will succeed if the target item drops on death")
        .build();

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        return targetItem.getItem().dropsOnDeath();
    }
}
