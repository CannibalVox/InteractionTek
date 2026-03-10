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
import com.voxtech.commands.Token;
import com.voxtech.helpers.CommandTokenHelper;
import com.voxtech.helpers.ExtraPermissionWrapper;
import com.voxtech.helpers.OpPermissionWrapper;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RunProxiedCommandInteraction extends SimpleInteraction {

    @Nonnull
    public static final BuilderCodec<RunProxiedCommandInteraction> CODEC = BuilderCodec
        .builder(RunProxiedCommandInteraction.class, RunProxiedCommandInteraction::new, SimpleInteraction.CODEC)
        .documentation("Runs specified command text as the specified entity, proxied through a . Will fail if the entity cannot run commands (mostly, only players can). Otherwise, this interaction will succeed even if the command fails to run.")
        .appendInherited(new KeyedCodec<>("RunFor", InteractionTarget.CODEC),
            (object, runAs) -> object.runFor = runAs,
            object -> object.runFor,
            (object, parent) -> object.runFor = parent.runFor)
            .documentation("The entity to run the command as.")
            .add()
        .appendInherited(new KeyedCodec<>("CommandText", Codec.STRING),
            (object, commandText) -> object.commandText = commandText,
            object -> object.commandText,
            (object, parent) -> object.commandText = parent.commandText)
            .documentation("The text of the command to execute.  Do not include the leading slash. The commands will not consider themselves to have run as a player, so the --player argument will sometimes be necessary.  See the readme for @-variables permitted in this command text.")
            .addValidator(Validators.nonEmptyString())
            .addValidator(Validators.nonNull())
            .add()
        .appendInherited(new KeyedCodec<>("WithPermissions", new SetCodec<>(Codec.STRING, HashSet<String>::new, true)),
            (object, withPermissions) -> object.withPermissions = withPermissions,
            object -> object.withPermissions,
            (object, parent) -> object.withPermissions = parent.withPermissions)
            .documentation("A set of permissions to add to the user for the scope of this command. When this field is used, the command runner will no longer be considered a player, which may prevent some commands from running.")
            .add()
        .build();


    private InteractionTarget runFor = InteractionTarget.USER;
    private String commandText;
    private Set<String> withPermissions;

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
            Ref<EntityStore> runEntity = runFor.getEntity(context, context.getEntity());
            CommandBuffer<EntityStore> buffer = context.getCommandBuffer();

            assert buffer != null;

            Entity entity = EntityUtils.getEntity(runEntity, buffer);
            if (commandText == null || !(entity instanceof CommandSender sender)) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            List<Token> tokens = CommandTokenHelper.tokenizeCommand(commandText);
            StringBuilder builder = new StringBuilder();

            for (Token token : tokens) {
                String value = token.value(runEntity, buffer, context);
                if (value == null) {
                    context.getState().state = InteractionState.Failed;
                    super.tick0(firstRun, time, type, context, cooldownHandler);
                    return;
                }

                builder.append(value);
            }

            if (withPermissions == null || withPermissions.isEmpty()) {
                sender = new OpPermissionWrapper(sender);
            } else {
                sender = new ExtraPermissionWrapper(sender, withPermissions);
            }

            future = CommandManager.get().handleCommand(sender, builder.toString());
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
