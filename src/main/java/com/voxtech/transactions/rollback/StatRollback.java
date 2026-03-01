package com.voxtech.transactions.rollback;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class StatRollback implements RollbackEntry {
    private final Ref<EntityStore> ref;
    private final int statIndex;
    private final float statAmount;

    public StatRollback(Ref<EntityStore> ref, int statIndex, float statAmount) {
        this.ref = ref;
        this.statIndex = statIndex;
        this.statAmount = statAmount;
    }

    @Override
    public void rollback(CommandBuffer<EntityStore> buffer, InteractionContext context, CooldownHandler cooldown) {
        EntityStatMap stats = buffer.getComponent(ref, EntityStatMap.getComponentType());
        if (stats == null) {
            return;
        }

        stats.setStatValue(EntityStatMap.Predictable.SELF, statIndex, statAmount);
    }
}
