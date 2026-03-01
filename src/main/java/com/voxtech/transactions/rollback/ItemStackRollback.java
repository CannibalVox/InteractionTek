package com.voxtech.transactions.rollback;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ItemStackRollback implements RollbackEntry {

    private final ItemContainer container;
    private final ItemStackTransaction change;

    public ItemStackRollback(ItemContainer container, ItemStackTransaction change) {
        this.container = container;
        this.change = change;
    }

    @Override
    public void rollback(CommandBuffer<EntityStore> buffer, InteractionContext context, CooldownHandler cooldown) {
        if (!change.succeeded()) {
            return;
        }

        for (ItemStackSlotTransaction inner : change.getSlotTransactions()) {
            if (!inner.succeeded()) {
                continue;
            }

            container.setItemStackForSlot(inner.getSlot(), inner.getSlotBefore(), false);
        }
    }
}
