package com.voxtech.transactions;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.transactions.postcommit.PostCommitEntry;
import com.voxtech.transactions.rollback.RollbackEntry;

import java.util.ArrayList;
import java.util.List;

public class TransactionState {
    private final List<RollbackEntry> rollbackEntries = new ArrayList<>();
    private final List<PostCommitEntry> postCommitEntries = new ArrayList<>();

    public void queueRollback(RollbackEntry entry) {
        rollbackEntries.add(entry);
    }

    public void queuePostCommit(PostCommitEntry entry) {
        postCommitEntries.add(entry);
    }

    public void executeRollback(CommandBuffer<EntityStore> buffer, InteractionContext context, CooldownHandler cooldown) {
        for (int i = rollbackEntries.size() -1; i >= 0; i--) {
            rollbackEntries.get(i).rollback(buffer, context, cooldown);
        }
    }

    public void executePostCommit(CommandBuffer<EntityStore> buffer, InteractionContext context, CooldownHandler cooldown) {
        for (PostCommitEntry entry : postCommitEntries) {
            entry.postCommit(buffer, context, cooldown);
        }
    }
}
