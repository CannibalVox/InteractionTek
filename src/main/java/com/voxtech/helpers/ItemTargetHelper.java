package com.voxtech.helpers;

import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.meta.DynamicMetaStore;
import com.hypixel.hytale.server.core.meta.MetaKey;

import javax.annotation.Nonnull;

import static com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction.CONTEXT_META_REGISTRY;

public class ItemTargetHelper {
    @Nonnull
    public static final MetaKey<ItemContext> TARGET_ITEM = CONTEXT_META_REGISTRY.registerMetaObject(InteractionContext::createHeldItemContext);

    public static ItemContext getTargetItem(InteractionContext context) {
        // Other interactions might change the currently held item without updating us, so always refer to the held item
        // when possible
        DynamicMetaStore<InteractionContext> metaStore = context.getMetaStore();
        if (!metaStore.hasMetaObject(TARGET_ITEM)) {
            return context.createHeldItemContext();
        }

        ItemContext itemContext = metaStore.getMetaObject(TARGET_ITEM);

        if (itemContext == null) {
            return context.createHeldItemContext();
        }

        if (itemContext.getContainer() == context.getHeldItemContainer() && itemContext.getSlot() == context.getHeldItemSlot()) {
            // We have a target but it's just pointing at the held item, so let's not have one anymore
            itemContext = context.createHeldItemContext();
            metaStore.putMetaObject(TARGET_ITEM, null);
        }

        return itemContext;
    }

    public static void putTargetItem(InteractionContext context, ItemContext newItemContext) {
        DynamicMetaStore<InteractionContext> metaStore = context.getMetaStore();

        if (newItemContext.getContainer() == context.getHeldItemContainer() && newItemContext.getSlot() == context.getHeldItemSlot()) {
            // New target is held item so no need to store
            if (metaStore.hasMetaObject(TARGET_ITEM)) {
                metaStore.putMetaObject(TARGET_ITEM, null);
            }

            // Update held item
            context.setHeldItem(newItemContext.getItemStack());
            return;
        }

        metaStore.putMetaObject(TARGET_ITEM, newItemContext);
    }

    public static void replaceTargetItem(InteractionContext context, ItemStack replacement) {
        DynamicMetaStore<InteractionContext> metaStore = context.getMetaStore();

        if (!metaStore.hasMetaObject(TARGET_ITEM)) {
            context.setHeldItem(replacement);
            return;
        }

        ItemContext targetItem = metaStore.getMetaObject(TARGET_ITEM);
        if (targetItem == null) {
            context.setHeldItem(replacement);
            return;
        }

        if (targetItem.getContainer() == context.getHeldItemContainer() && targetItem.getSlot() == context.getHeldItemSlot()) {
            // The target item is just the held item so we shouldn't have it
            metaStore.putMetaObject(TARGET_ITEM, null);
            context.setHeldItem(replacement);
            return;
        }

        metaStore.putMetaObject(TARGET_ITEM, new ItemContext(targetItem.getContainer(), targetItem.getSlot(), replacement));
    }
}
