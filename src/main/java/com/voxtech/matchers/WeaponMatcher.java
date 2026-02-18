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

public class WeaponMatcher extends ItemCondition.ItemMatcher {
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
