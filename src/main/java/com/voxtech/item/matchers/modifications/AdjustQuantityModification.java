package com.voxtech.item.matchers.modifications;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ModifyItemInteraction;

import javax.annotation.Nonnull;

public class AdjustQuantityModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<AdjustQuantityModification> CODEC = BuilderCodec
        .builder(AdjustQuantityModification.class, AdjustQuantityModification::new, BASE_CODEC)
        .documentation("This modification will increase or reduce the quantity of the target item. When reducing the quantity of the target item, this modification will fail if there are not enough in the target slot.")
        .append(new KeyedCodec<>("Delta", Codec.INTEGER),
            (object, delta) -> object.delta = delta,
            object -> object.delta)
            .documentation("The amount to increase or decrease quantity by.")
            .add()
        .append(new KeyedCodec<>("DontSpillOverExtra", Codec.BOOLEAN),
            (object, dontSpillOverExtra) -> object.dontSpillOverExtra = dontSpillOverExtra,
            object -> object.dontSpillOverExtra)
            .documentation("If true when increasing quantity, do not place the excess that cannot fit in the slot elsewhere in the entity's inventory")
            .add()
        .append(new KeyedCodec<>("DontDropExtra", Codec.BOOLEAN),
            (object, dontDropExtra) -> object.dontDropExtra = dontDropExtra,
            object -> object.dontDropExtra)
            .documentation("If true when increasing quantity, do not drop any excess that can't fit into the slot and/or inventory on the ground. Instead, mark this modification as failed.")
            .add()
        .build();

    private int delta;
    private boolean dontDropExtra;
    private boolean dontSpillOverExtra;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        if (delta > 0) {
            ItemStackSlotTransaction transaction = targetContainer.addItemStackToSlot(targetSlot, targetItem.withQuantity(delta));
            ItemStack remainder = transaction.getRemainder();

            if (!ItemStack.isEmpty(remainder) && !dontDropExtra) {
                ItemStackTransaction stackTransaction = inventory.getCombinedHotbarFirst().addItemStack(remainder);
                remainder = stackTransaction.getRemainder();
            }

            if (!ItemStack.isEmpty(remainder)) {
                if (dontSpillOverExtra) {
                    return false;
                }

                ItemUtils.dropItem(ref, remainder, buffer);
            }

            return true;
        }

        int toRemove = -delta;

            ItemStackSlotTransaction transaction = targetContainer.removeItemStackFromSlot(targetSlot, targetItem, toRemove);
            return transaction.succeeded();
    }
}
