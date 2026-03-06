package com.voxtech.item.modifications;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.InventoryHelper;
import com.voxtech.helpers.ItemTargetHelper;
import com.voxtech.interactions.ModifyItemInteraction;
import com.voxtech.transactions.TransactionState;

import javax.annotation.Nonnull;

public class GroupModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<GroupModification> CODEC = BuilderCodec
        .builder(GroupModification.class, GroupModification::new, BASE_CODEC)
        .documentation("Execute a list of item modifications. Great to use with other modifications such as Singulate and Conditional.")
        .appendInherited(new KeyedCodec<>("ItemModifications", new ArrayCodec<>(ModifyItemInteraction.ItemModification.CODEC, ModifyItemInteraction.ItemModification[]::new)),
            (object, itemModifications) -> object.itemModifications = itemModifications,
            object -> object.itemModifications,
            (object, parent) -> object.itemModifications = parent.itemModifications)
            .documentation("The modifications to execute. They will be executed in the order they are provided.")
            .addValidator(Validators.nonNull())
            .add()
        .append(new KeyedCodec<>("ContinueOnFailure", Codec.BOOLEAN),
            (object, continueOnFailure) -> object.continueOnFailure = continueOnFailure,
            object -> object.continueOnFailure)
            .documentation("If true, all modifications will be attempted, even if one fails. If false, execution will immediately halt if a modification fails.")
            .add()
        .build();

    private ModifyItemInteraction.ItemModification[] itemModifications;
    private boolean continueOnFailure;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, TransactionState transaction, InteractionContext context, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        return InventoryHelper.executeModifications(itemModifications, world, ref, buffer, transaction, context, targetContainer, targetSlot, targetItem, continueOnFailure);
    }
}
