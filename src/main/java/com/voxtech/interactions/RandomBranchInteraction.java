package com.voxtech.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SimpleInteraction;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.CollectorTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Label;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBranchInteraction extends Interaction {

    @Nonnull
    public static final BuilderCodec<RandomBranchInteraction> CODEC = BuilderCodec
        .builder(RandomBranchInteraction.class, RandomBranchInteraction::new, BuilderCodec.abstractBuilder(Interaction.class).build())
        .documentation("Runs a randomly-chosen entry in 'Branches' using a weighted random selection.")
        .appendInherited(new KeyedCodec<>("Branches", new ArrayCodec<>(Branch.CODEC, Branch[]::new)),
            (object, branches) -> object.branches = branches,
            object -> object.branches,
            (object, parent) -> object.branches = parent.branches)
            .documentation("The branches to randomly select between")
            .addValidator(Validators.nonNull())
            .add()
        .afterDecode(object -> {
            if (object.branches == null) {
                return;
            }

            for(Branch branch : object.branches) {
                object.totalWeight += branch.weight;
            }
        })
        .build();

    protected Branch[] branches;
    private int totalWeight;

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        if (branches == null || branches.length == 0 || totalWeight == 0) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int value = random.nextInt(totalWeight);

        for (int i = 0; i < branches.length; i++) {
            Branch branch = branches[i];
            if (value < branch.weight) {
                interactionContext.jump(interactionContext.getLabel(i));
                return;
            }

            value -= branch.weight;
        }
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        this.tick0(firstRun, time, interactionType, interactionContext, cooldownHandler);
    }

    @Override
    public void compile(@Nonnull OperationsBuilder builder) {
        Label end = builder.createUnresolvedLabel();
        Label[] labels = new Label[this.branches != null ? this.branches.length : 0];

        for (int i = 0; i < labels.length; i++) {
            labels[i] = builder.createUnresolvedLabel();
        }

        builder.addOperation(this, labels);
        builder.jump(end);

        if (this.branches != null) {
            for (int i = 0; i < this.branches.length; i++) {
                builder.resolveLabel(labels[i]);
                Interaction interaction = Interaction.getInteractionOrUnknown(this.branches[i].interaction);
                if (interaction != null) {
                    interaction.compile(builder);
                }

                builder.jump(end);
            }
        }

        builder.resolveLabel(end);
    }

    @Override
    public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
        for (int i = 0; i < this.branches.length; i++) {
            if (InteractionManager.walkInteraction(collector, context, BranchTag.of(i), branches[i].interaction)) {
                return true;
            }
        }

        return false;
    }

    @Nonnull
    @Override
    protected com.hypixel.hytale.protocol.Interaction generatePacket() {
        return new SimpleInteraction();
    }


    @Override
    public boolean needsRemoteSync() {
        return false;
    }

    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.None;
    }

    public static class Branch {

        @Nonnull
        public static final BuilderCodec<Branch> CODEC = BuilderCodec
            .builder(Branch.class, Branch::new)
            .documentation("A possible interaction to execute next.")
            .appendInherited(new KeyedCodec<>("Interaction", Interaction.CHILD_ASSET_CODEC),
                (object, interaction) -> object.interaction = interaction,
                object -> object.interaction,
                (object, parent) -> object.interaction = parent.interaction)
                .documentation("The interaction to execute")
                .addValidator(Interaction.VALIDATOR_CACHE.getValidator().late())
                .addValidator(Validators.nonNull())
                .add()
            .appendInherited(new KeyedCodec<>("Weight", Codec.INTEGER),
                (object, weight) -> object.weight = weight,
                object -> object.weight,
                (object, parent) -> object.weight = parent.weight)
                .documentation("Random weight. Defaults to 1. Defines how likely this branch is to be chosen.")
                .addValidator(Validators.greaterThanOrEqual(1))
                .add()
            .build();

        private String interaction;
        private int weight = 1;
    }

    private static class BranchTag implements CollectorTag {
        private final int index;

        private BranchTag(int index) {
            this.index = index;
        }

        @Nonnull
        public static BranchTag of(int index) {
            return new BranchTag(index);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                BranchTag other = (BranchTag)o;
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
            return "BranchTag{index=" + this.index + "}";
        }
    }
}
