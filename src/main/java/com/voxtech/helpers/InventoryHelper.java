package com.voxtech.helpers;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;
import com.voxtech.interactions.ModifyItemInteraction;
import com.voxtech.protocol.ItemMatchType;
import com.voxtech.transactions.TransactionState;

public class InventoryHelper {
    public static short getActiveSlot(InventoryComponent inventory) {
        return switch (inventory) {
            case InventoryComponent.Hotbar hotbar -> hotbar.getActiveSlot();
            case InventoryComponent.Utility utility -> utility.getActiveSlot();
            case InventoryComponent.Tool tool -> tool.getActiveSlot();
            default -> -1;
        };
    }

    public static boolean executeMatchers(ItemConditionInteraction.ItemMatcher[] matchers, ItemMatchType matchType, Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer container, short slot, ItemStack item) {
        for (ItemConditionInteraction.ItemMatcher matcher : matchers) {
            boolean success = matcher.test(ref, commandBuffer, context, container, slot, item);
            if (!success && matchType == ItemMatchType.All) {
                return false;
            }

            if (success && matchType == ItemMatchType.Any) {
                return true;
            }
        }

        return matchType == ItemMatchType.All;
    }

    public static boolean executeModifications(ModifyItemInteraction.ItemModification[] modifications, World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, TransactionState transaction, InteractionContext context, ItemContainer container, short slot, ItemStack itemStack, boolean continueOnFailure) {
        boolean failed = false;
        for (ModifyItemInteraction.ItemModification modification : modifications) {
            boolean success = modification.modifyItemStack(world, ref, buffer, transaction, context,  container, slot, itemStack);

            ItemTargetHelper.TargetItemData refreshed = ItemTargetHelper.refreshTargetItem(context);
            container = refreshed.getContainer();
            slot = refreshed.getSlot();
            itemStack = refreshed.getItemStack();

            if (!success) {
                failed = true;

                if (!continueOnFailure) {
                    break;
                }
            }
        }

        return !failed;
    }

    public static ItemStack getItemInHand(ComponentAccessor<EntityStore> store, Ref<EntityStore> ref) {
        InventoryComponent.Tool tool = store.getComponent(ref, InventoryComponent.Tool.getComponentType());
        if (tool != null && tool.isUsingToolsItem()) {
            return tool.getActiveItem();
        }

        InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
        if (hotbar != null) {
            return hotbar.getActiveItem();
        }

        return null;
    }
}
