package com.voxtech.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.meta.MetaKey;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.OpPermissionWrapper;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RunCommandInteraction extends SimpleInteraction {

    @Nonnull
    public static final BuilderCodec<RunCommandInteraction> CODEC = BuilderCodec
        .builder(RunCommandInteraction.class, RunCommandInteraction::new, SimpleInteraction.CODEC)
        .documentation("Runs specified command text as the specified entity. Will fail if the entity cannot run commands (mostly, only players can). Otherwise, this interaction will succeed even if the command fails to run.")
        .append(new KeyedCodec<>("RunAs", InteractionTarget.CODEC),
            (object, runAs) -> object.runAs = runAs,
            object -> object.runAs)
            .documentation("The entity to run the command as.")
            .add()
        .append(new KeyedCodec<>("CommandText", Codec.STRING),
            (object, commandText) -> object.commandText = commandText,
            object -> object.commandText)
            .documentation("The text of the command to execute.  Do not include the leading slash.")
            .addValidator(Validators.nonEmptyString())
            .addValidator(Validators.nonNull())
            .add()
        .build();


    private InteractionTarget runAs = InteractionTarget.USER;
    private String commandText;

    private static final MetaKey<CompletableFuture<Void>> COMMAND_FUTURE = Interaction.META_REGISTRY.registerMetaObject(i -> null);

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
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        CompletableFuture<Void> future;

        if (firstRun) {
            Ref<EntityStore> runEntity = runAs.getEntity(context, context.getEntity());
            CommandBuffer<EntityStore> buffer = context.getCommandBuffer();

            assert buffer != null;

            Entity entity = EntityUtils.getEntity(runEntity, buffer);
            if (commandText == null || !(entity instanceof CommandSender sender)) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            future = CommandManager.get().handleCommand(sender, commandText);
            context.getInstanceStore().putMetaObject(COMMAND_FUTURE, future);
        } else {
            future = context.getInstanceStore().getMetaObject(COMMAND_FUTURE);
        }

        if (future != null && !future.isDone()) {
            context.getState().state = InteractionState.NotFinished;
            return;
        }

        context.getState().state = InteractionState.Finished;
        super.tick0(firstRun, time, type, context, cooldownHandler);
    }
}
