package com.voxtech.item.modifications;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ModifyItemInteraction;
import com.voxtech.transactions.TransactionState;

import javax.annotation.Nonnull;

public class AlwaysFailModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<AlwaysFailModification> CODEC = BuilderCodec
        .builder(AlwaysFailModification.class, AlwaysFailModification::new, BASE_CODEC)
        .documentation("Does nothing. Always fails.")
        .build();

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, TransactionState transaction, InteractionContext context, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        return false;
    }
}
