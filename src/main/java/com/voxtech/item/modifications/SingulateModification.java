package com.voxtech.item.modifications;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ModifyItemInteraction;

import javax.annotation.Nonnull;

public class SingulateModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<SingulateModification> CODEC = BuilderCodec
        .builder(SingulateModification.class, SingulateModification::new, BASE_CODEC)
        .documentation("Runs another item modification, but removes all but one of the target item's quantity first. Afterwards, the excess is added to the entity's inventory.  This may result in the item re-stacking with the target item if they are still compatible after the modification.")
        .append(new KeyedCodec<>("Modification", ModifyItemInteraction.ItemModification.CODEC),
            (object, modification) -> object.modification = modification,
object -> object.modification)
            .documentation("The modification to run after singulating the item")
            .add()
        .build();

    private ModifyItemInteraction.ItemModification modification;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        ItemStack excess = null;
        int quantity = targetItem.getQuantity();

        if (quantity > 1) {
            excess = targetItem.withQuantity(quantity-1);
            targetItem = targetItem.withQuantity(1);
        }

        if (!this.modification.modifyItemStack(world, ref, buffer, context, inventory, targetContainer, targetSlot, targetItem)) {
            return false;
        }

        if (excess != null) {
            SimpleItemContainer.addOrDropItemStack(buffer, ref, inventory.getCombinedHotbarFirst(), excess);
        }

        return true;
    }
}
