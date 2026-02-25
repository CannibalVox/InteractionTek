package com.voxtech.transactions.postcommit;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public interface PostCommitEntry {
    void postCommit(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer);
}
