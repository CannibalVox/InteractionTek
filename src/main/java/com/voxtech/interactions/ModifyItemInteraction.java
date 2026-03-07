package com.voxtech.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.InventoryHelper;
import com.voxtech.helpers.ItemTargetHelper;
import com.voxtech.protocol.ItemModification;
import com.voxtech.transactions.TransactionState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModifyItemInteraction extends SimpleItemInteraction {

    @Nonnull
    public static final BuilderCodec<ModifyItemInteraction> CODEC = BuilderCodec
        .builder(ModifyItemInteraction.class, ModifyItemInteraction::new, SimpleInteraction.CODEC)
        .documentation("This interaction executes a series of modifications against the target item. This interaction will fail if any of the modifications fail.")
        .appendInherited(new KeyedCodec<>("ItemModifications", new ArrayCodec<>(ItemModification.CODEC, ItemModification[]::new)),
            (object, itemModifications) -> object.itemModifications = itemModifications,
            object -> object.itemModifications,
            (object, parent) -> object.itemModifications = parent.itemModifications)
            .documentation("The modifications to execute. They will be executed in the order they are provided")
            .addValidator(Validators.nonNull())
            .add()
        .append(new KeyedCodec<>("ContinueOnFailure", Codec.BOOLEAN),
            (object, continueOnFailure) -> object.continueOnFailure = continueOnFailure,
            object -> object.continueOnFailure)
            .documentation("If true, all modifications will be attempted, even if one fails. If false, execution will immediately halt if a modification fails.")
            .add()
        .append(new KeyedCodec<>("RequiredGameMode", new EnumCodec<>(GameMode.class)),
            (object, requiredGameMode) -> object.requiredGameMode = requiredGameMode,
            object -> object.requiredGameMode)
            .documentation("If the User entity is a player and they are not in this game mode, this interaction will do nothing but succeed. This setting is ignored for non-players.")
            .add()
        .append(new KeyedCodec<>("RollbackOnFailure", Codec.BOOLEAN),
            (object, rollbackOnFailure) -> object.rollbackOnFailure = rollbackOnFailure,
            object -> object.rollbackOnFailure)
            .documentation("If true, all changes made by modifications will be reversed before marking this interaction as failed in the event that a modification fails.")
            .add()
        .build();

    private ItemModification[] itemModifications;
    private boolean continueOnFailure;
    private boolean rollbackOnFailure;
    private GameMode requiredGameMode;

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
    protected void interactWithItem(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> buffer, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nullable ItemContainer targetContainer, int targetSlot, @Nullable ItemStack targetItemStack, @Nonnull CooldownHandler cooldownHandler) {
        if (targetContainer == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        Ref<EntityStore> ref = context.getEntity();

        if (requiredGameMode != null) {
            Player player = buffer.getComponent(ref, Player.getComponentType());
            if (player != null && player.getGameMode() != requiredGameMode) {
                return;
            }
        }

        TransactionState transaction = new TransactionState();

        if (!InventoryHelper.executeModifications(itemModifications, world, context.getEntity(), buffer, transaction, context, targetContainer, (short)targetSlot, targetItemStack, continueOnFailure)) {
            context.getState().state = InteractionState.Failed;

            if (rollbackOnFailure) {
                transaction.executeRollback(buffer, context, cooldownHandler);
                ItemTargetHelper.refreshTargetItem(context);
            }
        } else {
            transaction.executePostCommit(buffer, context, cooldownHandler);
        }
    }

    @Override
    protected void simulateInteractWithItem(@Nonnull World world, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nullable ItemContainer targetContainer, int targetSlot, @Nullable ItemStack targetItemStack) {

    }
}
