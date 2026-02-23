package com.voxtech.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class InterruptSelfInteraction extends SimpleInstantInteraction {

    @Nonnull
    public static final BuilderCodec<InterruptSelfInteraction> CODEC = BuilderCodec
        .builder(InterruptSelfInteraction.class, InterruptSelfInteraction::new, SimpleInstantInteraction.CODEC)
        .documentation("Cancels the currently-running interaction chain.")
        .build();

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = interactionContext.getOwningEntity();
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        InteractionManager interactionManager = commandBuffer.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
        if (interactionManager == null) {
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }

        InteractionChain chain = interactionContext.getChain();
        assert chain != null;

        interactionManager.cancelChains(chain);
        throw new InteractionManager.ChainCancelledException(interactionContext.getState().state);
    }
}
