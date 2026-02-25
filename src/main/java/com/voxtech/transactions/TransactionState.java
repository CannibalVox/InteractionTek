package com.voxtech.transactions;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
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

    public void executeRollback(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context) {
        for (int i = rollbackEntries.size() -1; i >= 0; i--) {
            rollbackEntries.get(i).rollback(ref, buffer, context);
        }
    }

    public void executePostCommit(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer) {
        for (PostCommitEntry entry : postCommitEntries) {
            entry.postCommit(ref, buffer);
        }
    }
}
