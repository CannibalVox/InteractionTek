package com.voxtech.interactions;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.CollectorTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.StringTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Label;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.protocol.TransactionStep;
import com.voxtech.transactions.TransactionState;
import joptsimple.internal.Strings;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TransactionInteraction extends Interaction {

    @Nonnull
    public static final BuilderCodec<TransactionInteraction> CODEC = BuilderCodec
        .builder(TransactionInteraction.class, TransactionInteraction::new, BuilderCodec.abstractBuilder(Interaction.class).build())
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
        .appendInherited(new KeyedCodec<>("Next", Interaction.CHILD_ASSET_CODEC),
            (object, next) -> object.next = next,
            object -> object.next,
            (object, parent) -> object.next = parent.next)
            .documentation("The interaction to run next if this interaction succeeds.")
            .addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late())
            .add()
        .appendInherited(new KeyedCodec<>("Failed", Interaction.CHILD_ASSET_CODEC),
            (object, failed) -> object.failed = failed,
            object -> object.failed,
            (object, parent) -> object.failed = parent.failed)
            .documentation("The interaction to run if the interaction failed and the failed step does not specify its own failure interaction")
            .addValidatorLate(() -> VALIDATOR_CACHE.getValidator().late())
            .add()
        .afterDecode(object -> {
            if (object.steps != null) {
                for (int i = 0; i < object.steps.length; i++) {
                    if (!Strings.isNullOrEmpty(object.steps[i].getFailed())) {
                        object.stepsToFailureInteraction.add(object.failureInteractions.size());
                        object.failureInteractions.add(object.steps[i].getFailed());
                    } else {
                        object.stepsToFailureInteraction.add(null);
                    }
                }
            }
        })
        .build();

    private TransactionStep[] steps;
    @Nullable
    private String next;
    @Nullable
    private String failed;

    private final List<Integer> stepsToFailureInteraction = new ArrayList<>();
    private final List<String> failureInteractions = new ArrayList<>();

    private static final StringTag TAG_NEXT = StringTag.of("Next");
    private static final StringTag TAG_FAILED = StringTag.of("Failed");

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        TransactionState transaction = new TransactionState();
        Ref<EntityStore> ref = context.getEntity();
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        for (int i = 0; i < steps.length; i++) {
            TransactionStep step = steps[i];

            if (!step.execute(ref, commandBuffer, transaction, context, cooldownHandler)) {
                transaction.executeRollback(commandBuffer, context, cooldownHandler);

                Integer failureInteractionIndex = stepsToFailureInteraction.get(i);
                if (failureInteractionIndex == null && this.failed != null) {
                    failureInteractionIndex = failureInteractions.size();
                }

                if (failureInteractionIndex != null) {
                    context.jump(context.getLabel(failureInteractionIndex));
                }

                context.getState().state = InteractionState.Failed;
                return;
            }
        }

        context.getState().state = InteractionState.Finished;
        transaction.executePostCommit(commandBuffer, context, cooldownHandler);
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        this.tick0(firstRun, time, interactionType, interactionContext, cooldownHandler);
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

    @Override
    public void compile(@Nonnull OperationsBuilder builder) {

        int failedInteractionCount = this.failureInteractions.size();
        if (this.failed != null) {
            failedInteractionCount++;
        }

        Label[] failLabels = new Label[failedInteractionCount];
        for (int i = 0; i < failLabels.length; i++) {
            failLabels[i] = builder.createUnresolvedLabel();
        }
        Label endLabel = builder.createUnresolvedLabel();

        builder.addOperation(this, failLabels);
        if (this.next != null) {
             Interaction nextInteraction = Interaction.getInteractionOrUnknown(this.next);
             assert nextInteraction != null;

             nextInteraction.compile(builder);
        }

        if (failLabels.length > 0) {
            builder.jump(endLabel);
        }

        for (int i = 0; i < this.failureInteractions.size(); i++) {
            builder.resolveLabel(failLabels[i]);
            Interaction failInteraction = Interaction.getInteractionOrUnknown(this.failureInteractions.get(i));
            assert failInteraction != null;

            failInteraction.compile(builder);
            builder.jump(endLabel);
        }

        if (this.failed != null) {
            builder.resolveLabel(failLabels[failLabels.length-1]);

            Interaction failInteraction = Interaction.getInteractionOrUnknown(this.failed);
            assert failInteraction != null;

            failInteraction.compile(builder);
            builder.jump(endLabel);
        }

        builder.resolveLabel(endLabel);
    }

    @Override
    public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
        if (InteractionManager.walkInteraction(collector, context, TAG_NEXT, this.next)) {
            return true;
        }

        if (InteractionManager.walkInteraction(collector, context, TAG_FAILED, this.failed)) {
            return true;
        }

        for (int i = 0; i < this.steps.length; i++) {
            if (InteractionManager.walkInteraction(collector, context, StepTag.of(i), steps[i].getFailed())) {
                return true;
            }
        }

        return false;
    }

    @Nonnull
    @Override
    protected com.hypixel.hytale.protocol.Interaction generatePacket() {
        return new com.hypixel.hytale.protocol.SimpleInteraction();
    }

    private static class StepTag implements CollectorTag {
        private final int index;

        private StepTag(int index) {
            this.index = index;
        }

        @Nonnull
        public static StepTag of(int index) {
            return new StepTag(index);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                StepTag other = (StepTag) o;
                return this.index == other.index;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.index;
        }

        @Override
        @Nonnull
        public String toString() {
            return "StepTag{index=" + this.index + "}";
        }
    }
}
