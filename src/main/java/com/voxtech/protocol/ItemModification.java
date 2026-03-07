package com.voxtech.protocol;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.transactions.TransactionState;

import javax.annotation.Nonnull;

public abstract class ItemModification {
    @Nonnull
    public static final CodecMapCodec<ItemModification> CODEC = new CodecMapCodec<>("Type");

    @Nonnull
    public static final BuilderCodec<ItemModification> BASE_CODEC = BuilderCodec
            .abstractBuilder(ItemModification.class)
            .appendInherited(new KeyedCodec<>("FailEmptyItem", Codec.BOOLEAN),
                    (object, failEmptyItem) -> object.failEmptyItem = failEmptyItem,
                    object -> object.failEmptyItem,
                    (object, parent) -> object.failEmptyItem = parent.failEmptyItem)
            .documentation("If true, this modification will automatically fail if an empty slot is passed to it.  If false, empty slots will not fail, even if it means that the modification can take no action.")
            .add()
            .build();

    private boolean failEmptyItem;

    public boolean modifyItemStack(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, TransactionState transaction, InteractionContext context, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        if (targetItem == null && failEmptyItem) {
            return false;
        } else if (targetItem == null && !canOperateOnEmpty()) {
            return true;
        }

        return modify0(world, ref, buffer, transaction, context, targetContainer, targetSlot, targetItem);
    }

    public abstract boolean modify0(World world, Ref<EntityStore> ref,  CommandBuffer<EntityStore> buffer, TransactionState transaction, InteractionContext context, ItemContainer targetContainer, short targetSlot, ItemStack targetItem);

    public boolean canOperateOnEmpty() {
        return false;
    }
}
