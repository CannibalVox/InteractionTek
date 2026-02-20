package com.voxtech.item.matchers;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;

import javax.annotation.Nonnull;

public class WeaponMatcher extends ItemConditionInteraction.ItemMatcher {
    @Nonnull
    public static final BuilderCodec<WeaponMatcher> CODEC = BuilderCodec.builder(
        WeaponMatcher.class, WeaponMatcher::new, BASE_CODEC
    )
        .documentation("This matcher succeeds if the target item is a weapon.")
        .build();

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        return targetItem.getItem().getWeapon() != null;
    }
}
