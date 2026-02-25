package com.voxtech.transactions.rollback;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.ItemTargetHelper;

public class ItemTargetRollback implements RollbackEntry{

    private final ItemTargetHelper.TargetItemData oldTarget;

    public ItemTargetRollback(ItemTargetHelper.TargetItemData oldTarget) {
        this.oldTarget = oldTarget;
    }

    @Override
    public void rollback(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context) {
        ItemTargetHelper.putTargetItem(context, oldTarget);
    }
}
