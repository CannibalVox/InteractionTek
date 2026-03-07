package com.voxtech.protocol;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.transactions.TransactionState;

import javax.annotation.Nonnull;

public abstract class TransactionStep {
    private String failed;

    @Nonnull
    public static final CodecMapCodec<TransactionStep> CODEC = new CodecMapCodec<>("Type");

    @Nonnull
    public static final BuilderCodec<TransactionStep> BASE_CODEC = BuilderCodec
            .abstractBuilder(TransactionStep.class)
            .appendInherited(new KeyedCodec<>("Failed", Interaction.CHILD_ASSET_CODEC),
                    (object, failed) -> object.failed = failed,
                    object -> object.failed,
                    (object, parent) -> object.failed = parent.failed)
            .documentation("The interaction to run if this transaction step fails.")
            .addValidator(Interaction.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    public abstract boolean execute(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, TransactionState transaction, InteractionContext context, CooldownHandler cooldownHandler);

    public String getFailed() {
        return this.failed;
    }
}
