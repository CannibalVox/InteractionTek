package com.voxtech.item.modifications;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.protocol.ItemModification;
import com.voxtech.transactions.TransactionState;
import com.voxtech.transactions.rollback.ItemSlotRollback;
import com.voxtech.transactions.rollback.ItemStackRollback;
import com.voxtech.transactions.rollback.SpawnEntityRollback;

import javax.annotation.Nonnull;

public class AdjustQuantityModification extends ItemModification {

    @Nonnull
    public static final BuilderCodec<AdjustQuantityModification> CODEC = BuilderCodec
        .builder(AdjustQuantityModification.class, AdjustQuantityModification::new, BASE_CODEC)
        .documentation("This modification will increase or reduce the quantity of the target item. When reducing the quantity of the target item, this modification will fail if there are not enough in the target slot.")
        .appendInherited(new KeyedCodec<>("Delta", Codec.INTEGER),
            (object, delta) -> object.delta = delta,
            object -> object.delta,
            (object, parent) -> object.delta = parent.delta)
            .documentation("The amount to increase or decrease quantity by.")
            .add()
        .appendInherited(new KeyedCodec<>("DontSpillOverExtra", Codec.BOOLEAN),
            (object, dontSpillOverExtra) -> object.dontSpillOverExtra = dontSpillOverExtra,
            object -> object.dontSpillOverExtra,
            (object, parent) -> object.dontSpillOverExtra = parent.dontSpillOverExtra)
            .documentation("If true when increasing quantity, do not place the excess that cannot fit in the slot elsewhere in the entity's inventory")
            .add()
        .appendInherited(new KeyedCodec<>("DontDropExtra", Codec.BOOLEAN),
            (object, dontDropExtra) -> object.dontDropExtra = dontDropExtra,
            object -> object.dontDropExtra,
            (object, parent) -> object.dontDropExtra = parent.dontDropExtra)
            .documentation("If true when increasing quantity, do not drop any excess that can't fit into the slot and/or inventory on the ground. Instead, mark this modification as failed.")
            .add()
        .build();

    private int delta;
    private boolean dontDropExtra;
    private boolean dontSpillOverExtra;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, TransactionState transaction, InteractionContext context, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        if (delta > 0) {
            ItemStackSlotTransaction itemTransaction = targetContainer.addItemStackToSlot(targetSlot, targetItem.withQuantity(delta));
            transaction.queueRollback(new ItemSlotRollback(targetContainer, itemTransaction));
            ItemStack remainder = itemTransaction.getRemainder();

            if (!ItemStack.isEmpty(remainder) && !dontSpillOverExtra) {
                ItemContainer combined = InventoryComponent.getCombined(buffer, ref, InventoryComponent.HOTBAR_FIRST);
                if (combined.getCapacity() == 0) {
                    return false;
                }

                ItemStackTransaction stackTransaction = combined.addItemStack(remainder);
                transaction.queueRollback(new ItemStackRollback(combined, stackTransaction));
                remainder = stackTransaction.getRemainder();
            }

            if (!ItemStack.isEmpty(remainder)) {
                if (dontDropExtra) {
                    return false;
                }

                Ref<EntityStore> spawned = ItemUtils.dropItem(ref, remainder, buffer);
                if (spawned != null) {
                    transaction.queueRollback(new SpawnEntityRollback(spawned));
                }
            }

            return true;
        }

        int toRemove = -delta;

        ItemStackSlotTransaction itemTransaction = targetContainer.removeItemStackFromSlot(targetSlot, targetItem, toRemove);
        if (!itemTransaction.succeeded()) {
            return false;
        }
        transaction.queueRollback(new ItemSlotRollback(targetContainer, itemTransaction));
        return true;
    }
}
