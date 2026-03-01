package com.voxtech.transactions.steps;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.TransactionInteraction;
import com.voxtech.transactions.TransactionState;

import javax.annotation.Nonnull;

public class AlwaysFailStep extends TransactionInteraction.TransactionStep {

    @Nonnull
    public static final BuilderCodec<AlwaysFailStep> CODEC = BuilderCodec
        .builder(AlwaysFailStep.class, AlwaysFailStep::new, BASE_CODEC)
        .documentation("Always fail.")
        .build();

    @Override
    public boolean execute(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, TransactionState transaction, InteractionContext context, CooldownHandler cooldownHandler) {
        return false;
    }
}
