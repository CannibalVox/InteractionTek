package com.voxtech.item.modifications;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ModifyItemInteraction;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ChangeStateModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<ChangeStateModification> CODEC = BuilderCodec
        .builder(ChangeStateModification.class, ChangeStateModification::new, BASE_CODEC)
        .documentation("This modification will transform the target item between item states using a set of provided transformations.  This modification will fail if the item cannot transform to one of the states in the map for any reason.")
        .append(new KeyedCodec<>("Changes", new MapCodec<>(Codec.STRING, HashMap::new)),
            (object, stateKeys) -> object.stateKeys = stateKeys,
    object -> object.stateKeys)
            .documentation("Map of item state names to item state names. Unlike the ChangeState interaction, 'default' is not an option, and it is recommended that you reference the main Item as a state if you would like to transform from or to that state.")
            .add()
        .build();

    private Map<String, String> stateKeys;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {

        String stateItemId = targetItem.getItem().getStateForItem(targetItem.getItemId());
        if (stateItemId == null) {
            // Weird id
            return false;
        }

        if (!stateKeys.containsKey(stateItemId)) {
            return false;
        }

        String newState = stateKeys.get(stateItemId);
        String newItemId = targetItem.getItem().getStateForItem(newState);
        if (newItemId == null) {
            return false;
        }
        ItemStack newItem = new ItemStack(newItemId, targetItem.getQuantity(), targetItem.getMetadata());
        newItem = newItem.withDurability(targetItem.getDurability());

        ItemStackSlotTransaction slot = targetContainer.setItemStackForSlot(targetSlot, newItem);
        return slot.succeeded();
    }
}
