package com.voxtech.transactions.rollback;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public interface RollbackEntry {
    void rollback(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context);
}
