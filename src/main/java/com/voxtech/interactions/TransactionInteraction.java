package com.voxtech.interactions;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.transactions.TransactionState;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class TransactionInteraction extends SimpleInstantInteraction {

    @Nonnull
    public static final BuilderCodec<TransactionInteraction> CODEC = BuilderCodec
        .builder(TransactionInteraction.class, TransactionInteraction::new, SimpleInstantInteraction.CODEC)
        .documentation("Executes a number of steps in order. If any steps fail, then all steps are rolled back and this interaction is marked as failed.")
        .appendInherited(new KeyedCodec<>("Steps", new ArrayCodec<>(TransactionStep.CODEC, TransactionStep[]::new)),
            (object, steps) -> object.steps = steps,
            object -> object.steps,
            (object, parent) -> object.steps = parent.steps)
            .documentation("The steps to execute as part of this transaction.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonEmptyArray())
            .addValidator(Validators.nonNullArrayElements())
            .add()
        .build();

    private TransactionStep[] steps;

    @Override
    protected void firstRun(@NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        TransactionState transaction = new TransactionState();
        Ref<EntityStore> ref = context.getEntity();
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

        for (TransactionStep step : steps) {
            if (!step.execute(ref, commandBuffer, transaction, context, cooldownHandler)) {
                transaction.executeRollback(commandBuffer, context, cooldownHandler);
                context.getState().state = InteractionState.Failed;
                return;
            }
        }

        transaction.executePostCommit(commandBuffer, context, cooldownHandler);
    }

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    public boolean needsRemoteSync() {
        return true;
    }

    public static abstract class TransactionStep {
        @Nonnull
        public static final CodecMapCodec<TransactionStep> CODEC = new CodecMapCodec<>("Type");

        @Nonnull
        public static final BuilderCodec<TransactionStep> BASE_CODEC = BuilderCodec
                .abstractBuilder(TransactionStep.class)
                .build();

        public abstract boolean execute(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, TransactionState transaction, InteractionContext context, CooldownHandler cooldownHandler);
    }
}
