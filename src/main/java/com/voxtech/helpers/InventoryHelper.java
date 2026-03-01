package com.voxtech.helpers;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;
import com.voxtech.interactions.ModifyItemInteraction;
import com.voxtech.protocol.ItemMatchType;
import com.voxtech.transactions.TransactionState;

import static com.hypixel.hytale.server.core.inventory.Inventory.*;

public class InventoryHelper {
    public static short getActiveSlot(Inventory inventory, int inventorySectionId) {
        return switch (inventorySectionId) {
            case HOTBAR_SECTION_ID -> inventory.getActiveHotbarSlot();
            case UTILITY_SECTION_ID -> inventory.getActiveUtilitySlot();
            case TOOLS_SECTION_ID -> inventory.getActiveToolsSlot();
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

    public static boolean executeModifications(ModifyItemInteraction.ItemModification[] modifications, World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, TransactionState transaction, InteractionContext context, Inventory inventory, ItemContainer container, short slot, ItemStack itemStack, boolean continueOnFailure) {
        boolean failed = false;
        for (ModifyItemInteraction.ItemModification modification : modifications) {
            boolean success = modification.modifyItemStack(world, ref, buffer, transaction, context, inventory, container, slot, itemStack);

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
}
