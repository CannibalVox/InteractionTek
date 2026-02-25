package com.voxtech.transactions.rollback;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ItemSlotRollback implements RollbackEntry {
    private final ItemContainer container;
    private final SlotTransaction change;

    public ItemSlotRollback(ItemContainer container, SlotTransaction change) {
        this.container = container;
        this.change = change;
    }

    @Override
    public void rollback(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context) {
        if (!change.succeeded()) {
            return;
        }

        container.setItemStackForSlot(change.getSlot(), change.getSlotBefore(), false);
    }
}
