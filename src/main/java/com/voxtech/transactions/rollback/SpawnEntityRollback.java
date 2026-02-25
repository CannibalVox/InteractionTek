package com.voxtech.transactions.rollback;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SpawnEntityRollback implements RollbackEntry {
    private final Ref<EntityStore> spawnedEntity;

    public SpawnEntityRollback(Ref<EntityStore> spawnedEntity) {
        this.spawnedEntity = spawnedEntity;
    }

    @Override
    public void rollback(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context) {
        buffer.removeEntity(spawnedEntity, RemoveReason.REMOVE);
    }
}
