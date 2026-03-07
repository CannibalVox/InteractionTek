package com.voxtech.item.modifications;

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

public class SingulateModification extends ItemModification {

    @Nonnull
    public static final BuilderCodec<SingulateModification> CODEC = BuilderCodec
        .builder(SingulateModification.class, SingulateModification::new, BASE_CODEC)
        .documentation("Runs another item modification, but removes all but one of the target item's quantity first. Afterwards, the excess is added to the entity's inventory.  This may result in the item re-stacking with the target item if they are still compatible after the modification.")
        .appendInherited(new KeyedCodec<>("Modification", ItemModification.CODEC),
            (object, modification) -> object.modification = modification,
            object -> object.modification,
            (object, parent) -> object.modification = parent.modification)
            .documentation("The modification to run after singulating the item")
            .add()
        .build();

    private ItemModification modification;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, TransactionState transaction, InteractionContext context, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        ItemStack excess = null;
        int quantity = targetItem.getQuantity();

        if (quantity > 1) {
            excess = targetItem.withQuantity(quantity-1);
            targetItem = targetItem.withQuantity(1);
            ItemStackSlotTransaction itemTransaction = targetContainer.setItemStackForSlot(targetSlot, targetItem);
            if (!itemTransaction.succeeded()) {
                return false;
            }

            transaction.queueRollback(new ItemSlotRollback(targetContainer, itemTransaction));
        }

        ItemContainer combined = InventoryComponent.getCombined(buffer, ref, InventoryComponent.HOTBAR_FIRST);
        if (!ItemStack.isEmpty(excess) && combined.getCapacity() == 0) {
            return false;
        }

        if (!this.modification.modifyItemStack(world, ref, buffer, transaction, context, targetContainer, targetSlot, targetItem)) {
            return false;
        }

        if (!ItemStack.isEmpty(excess)) {
            ItemStackTransaction stackTransaction = combined.addItemStack(excess);
            transaction.queueRollback(new ItemStackRollback(combined, stackTransaction));
            excess = stackTransaction.getRemainder();
        }

        if (!ItemStack.isEmpty(excess)) {
            Ref<EntityStore> spawned = ItemUtils.dropItem(ref, excess, buffer);

            if (spawned != null) {
                transaction.queueRollback(new SpawnEntityRollback(spawned));
            }
        }

        return true;
    }
}
